package dk.nodes.nstack.util.appopen;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.cache.PrefsManager;
import dk.nodes.nstack.util.log.Logger;
/**
 * Created by joso on 17/11/15.
 */
public class AppOpenManager {
    private final static String TAG = AppOpenManager.class.getSimpleName();
    private final static String KEY_SETTINGS = "APP_OPEN_SETTINGS";
    private final static String BASE_URL = "https://nstack.io/api/v1/open";

    private RateCallbacks rateListener;
    private AppOpenCallbacks translationsListener;

    private VersionControlCallbacks versionControlListener;
    private AppOpen appOpen;
    private AppOpenSettings settings = new AppOpenSettings();

    public AppOpenManager(  ) {
        checkSettings();
    }

    private void checkSettings() {
        try {
            settings = (AppOpenSettings) CacheManager.loadObject(NStack.getStack().getApplicationContext(), KEY_SETTINGS);

            if( settings == null ) {
                settings = new AppOpenSettings();
            }
        } catch( Exception e ) {
            settings = new AppOpenSettings();
        }

        if( settings.guid == null ) {
            settings.guid = UUID.randomUUID().toString();
        }
    }

    public void checkVersionControl(final Activity activity,
                                    @Nullable VersionControlCallbacks versionControlListener) {
        this.versionControlListener = versionControlListener;

        handleVersionControl(activity);
    }

    public void checkRateReminder(final Activity activity, @Nullable RateCallbacks rateListener) {
        this.rateListener = rateListener;

        handleRateRequest(activity);
    }

    public void openApp() {
        openApp(null);
    }

    public void openApp(@Nullable AppOpenCallbacks translationsListener) {
        this.translationsListener = translationsListener;

        BackendManager.getInstance().getAppOpen(BASE_URL, settings, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if( AppOpenManager.this.translationsListener != null ) {
                    AppOpenManager.this.translationsListener.onFailure();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);

                    appOpen = AppOpen.parseFromJson(root);
                    JSONObject jo = root.getJSONObject("data");
                    if(jo != null)
                    {
                        if(jo.has("last_updated"))
                        {
                            settings.lastUpdatedString = jo.getString("last_updated");
                        }
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            handleTranslations();
                            settings.save();
                        }
                    });

                } catch(Exception e) {
                    Logger.e(e);

                    if( AppOpenManager.this.translationsListener != null ) {
                        AppOpenManager.this.translationsListener.onFailure();
                    }
                }
            }
        });
    }

    private void handleTranslations() {
        if( appOpen.translationRoot == null ) {
            //No new translations - load translations from cache
            if (PrefsManager.with(NStack.getStack().getApplicationContext()).contains(PrefsManager.Key.TRANSLATIONS)) {

                String translations = PrefsManager.with(NStack.getStack().getApplicationContext()).getString(PrefsManager.Key.TRANSLATIONS);
                try {

                    JSONObject jsonTranslations = new JSONObject(translations);
                    NStack.getStack().getTranslationManager().updateTranslationsFromAppOpen(jsonTranslations);
                    AppOpenManager.this.translationsListener.onUpdated();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //New translations - save new translations into cache
            PrefsManager.with(NStack.getStack().getApplicationContext()).putString(PrefsManager.Key.TRANSLATIONS, appOpen.translationRoot.toString());
            settings.lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());

            NStack.getStack().getTranslationManager().updateTranslationsFromAppOpen(appOpen.translationRoot);
            AppOpenManager.this.translationsListener.onUpdated();

        }

    }

    private void handleRateRequest(final Activity activity) {
        boolean showReminder = activity.getSharedPreferences("rated", Context.MODE_PRIVATE).getBoolean("showRateReminder", true);

        if (appOpen.rateRequestAvailable && showReminder) {

            AlertDialog.Builder builder;

            if(activity instanceof  AppCompatActivity) {
                if(((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext()
                    );
                } else{
                    builder = new AlertDialog.Builder(activity);
                }
            } else{
                builder = new AlertDialog.Builder(activity);
            }

            builder.setTitle(appOpen.rateReminder.title)
            .setMessage(appOpen.rateReminder.body)
            .setPositiveButton(appOpen.rateReminder.yesBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        NStack.getStack().getApplicationContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.storeLink)));
                    } catch (Exception e) {
                        Logger.e(e);
                    }
                }
            })
            .setNeutralButton(appOpen.rateReminder.laterBtn, null)
            .setNegativeButton(appOpen.rateReminder.noBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.getSharedPreferences("rated", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("showRateReminder", false)
                    .commit();
                }
            });

            if (rateListener != null) {
                rateListener.onRateReminder(builder.create());
            } else {
                builder.create().show();
            }
        }
    }

    private void handleVersionControl(Activity activity) {
        // Smallish naming hack
        if(appOpen.update != null) {
            if(appOpen.update.positiveBtn != null) {
                if (appOpen.update.positiveBtn.contains("AppStore")) {
                    appOpen.update.positiveBtn = appOpen.update.positiveBtn.replace("AppStore", "Play Store");
                }
            }
        }


        // Forced update
        if( appOpen.updateAvailable && appOpen.forcedUpdate ) {

            AlertDialog.Builder builder;
            if(activity instanceof  AppCompatActivity) {
                if(((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext()
                    );
                } else{
                    builder = new AlertDialog.Builder(activity);
                }
            } else{
                builder = new AlertDialog.Builder(activity);
            }

            builder
                    .setMessage(appOpen.update.title)
                    .setPositiveButton(appOpen.update.positiveBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.storeLink));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NStack.getStack().getApplicationContext().startActivity(i);
                            } catch( Exception e ) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setCancelable(false);

            if( versionControlListener != null ) {
                versionControlListener.onForcedUpdate(builder.create());
            } else {
                builder.create().show();
            }
        }

        // Normal update
        else if( appOpen.updateAvailable ) {
            AlertDialog.Builder builder;
            if(activity instanceof  AppCompatActivity) {
                if(((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext()
                    );
                } else{
                    builder = new AlertDialog.Builder(activity);
                }
            } else{
                builder = new AlertDialog.Builder(activity);
            }

            builder .setMessage(appOpen.update.title)
                    .setPositiveButton(appOpen.update.positiveBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.storeLink));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NStack.getStack().getApplicationContext().startActivity(i);
                            } catch( Exception e ) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setNegativeButton(appOpen.update.negativeBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setCancelable(true);

            if( versionControlListener != null ) {
                versionControlListener.onUpdate(builder.create());
            } else {
                builder.create().show();
            }
        }

        // Updated, show change log
        else if( appOpen.changelogAvailable ) {
            AlertDialog.Builder builder;
            if(activity instanceof  AppCompatActivity) {
                if(((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext()
                    );
                } else{
                    builder = new AlertDialog.Builder(activity);
                }
            } else{
                builder = new AlertDialog.Builder(activity);
            }

            builder .setTitle(appOpen.update.title)
                    .setMessage(Html.fromHtml(appOpen.update.message))
                    .setPositiveButton(appOpen.update.negativeBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(true);

            if( versionControlListener != null ) {
                versionControlListener.onChangelog(builder.create());
            } else {
                builder.create().show();
            }
        }
    }

    public interface VersionControlCallbacks {
        void onForcedUpdate(Dialog dialog);
        void onUpdate(Dialog dialog);
        void onChangelog(Dialog dialog);
    }

    public interface RateCallbacks {
        void onRateReminder(Dialog dialog);
    }

    public interface AppOpenCallbacks {
        void onUpdated();
        void onFailure();
    }

}
