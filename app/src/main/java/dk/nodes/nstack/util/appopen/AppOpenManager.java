package dk.nodes.nstack.util.appopen;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

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
import dk.nodes.nstack.util.translation.TranslationManager;
import dk.nodes.nstack.util.translation.TranslationOptions;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by joso on 17/11/15.
 */
public class AppOpenManager {
    private final static String TAG = AppOpenManager.class.getSimpleName();
    private final static String KEY_SETTINGS = "APP_OPEN_SETTINGS";
    private final static String BASE_URL = "https://nstack.io/api/v1/open";

    private RateCallbacks rateListener;
    private AppOpenCallbacks translationsListener;
    private MessagesCallbacks messagesListener;

    private VersionControlCallbacks versionControlListener;
    private AppOpen appOpen;
    private AppOpenSettings settings = new AppOpenSettings();
    //private TranslationOptions translationOptions = new TranslationOptions();

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

    public void checkMessages(final Activity activity, @Nullable MessagesCallbacks messagesListener) {
        this.messagesListener = messagesListener;

        handleMessages(activity);
    }

    public void openApp() {
        openApp(null);
    }

    public void openApp(@Nullable AppOpenCallbacks translationsListener) {
        this.translationsListener = translationsListener;

        try {
            updateTranslationsFromCache();
        } catch(Exception e) {
            // Since we probably didnt have anything cached, handle it as a failure
            handleAppOpenFailure();
            Logger.e(e);
        }

        BackendManager.getInstance().getAppOpen(BASE_URL, settings, TranslationManager.getInstance().options().getLanguageHeader(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleAppOpenFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);

                    appOpen = AppOpen.parseFromJson(root);
                    JSONObject jo = root.getJSONObject("data");
                    if (jo != null) {
                        if (jo.has("last_updated")) {
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

                } catch (Exception e) {
                    Logger.e(e);

                    handleAppOpenFailure();
                }
            }
        });
    }

    private void handleAppOpenFailure() {
        // Fail with proper settings
        AppOpenManager.this.appOpen = AppOpen.parseFromJson(new JSONObject());
        AppOpenManager.this.appOpen.rateRequestAvailable = false;
        AppOpenManager.this.appOpen.updateAvailable = false;
        AppOpenManager.this.appOpen.changelogAvailable = false;
        AppOpenManager.this.appOpen.forcedUpdate = false;
        AppOpenManager.this.appOpen.messageAvailable = false;

        // Try updating translations with cached if they exist
        if (PrefsManager.with(NStack.getStack().getApplicationContext()).contains(PrefsManager.Key.TRANSLATIONS)) {
            try {
                updateTranslationsFromCache();
                if (AppOpenManager.this.translationsListener != null) {
                    AppOpenManager.this.translationsListener.onUpdated(true);
                }
            } catch (Exception ex) {
                if (AppOpenManager.this.translationsListener != null) {
                    AppOpenManager.this.translationsListener.onFailure();
                }
            }
        }

        // Fail if we dont have any cached translations
        else if (AppOpenManager.this.translationsListener != null) {
            AppOpenManager.this.translationsListener.onFailure();
        }
    }

    private void updateTranslationsFromCache() throws Exception {
        String translations = PrefsManager.with(NStack.getStack().getApplicationContext()).getString(PrefsManager.Key.TRANSLATIONS);
        JSONObject jsonTranslations = new JSONObject(translations);
        NStack.getStack().getTranslationManager().updateTranslationsFromAppOpen(jsonTranslations);
        Logger.d("Updated translations from cache...");
    }

    private void handleTranslations() {
        if (appOpen == null) {
            Logger.e("handleTranslations()", "App open object is null, parsing failed or response timed out.");
            return;
        }

        if( appOpen.translationRoot == null ) {
            //No new translations - load translations from cache
            if (PrefsManager.with(NStack.getStack().getApplicationContext()).contains(PrefsManager.Key.TRANSLATIONS)) {
                try {
                    updateTranslationsFromCache();
                    if (AppOpenManager.this.translationsListener != null) {
                        AppOpenManager.this.translationsListener.onUpdated(true);
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        } else {
            //New translations - save new translations into cache
            PrefsManager.with(NStack.getStack().getApplicationContext()).putString(PrefsManager.Key.TRANSLATIONS, appOpen.translationRoot.toString());
            settings.lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
            Logger.d("Saved translations to cache...");

            NStack.getStack().getTranslationManager().updateTranslationsFromAppOpen(appOpen.translationRoot);
            if (AppOpenManager.this.translationsListener != null) {
                AppOpenManager.this.translationsListener.onUpdated(false);
            }
        }

    }

    private void handleRateRequest(final Activity activity) {
        if (appOpen == null) {
            Logger.e("HandleRateRequestControl", "App open object is null, parsing failed or response timed out.");
            return;
        }

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

    private void handleMessages(final Activity activity) {
        if (appOpen == null) {
            Logger.e("handleMessages", "App open object is null, parsing failed or response timed out.");
            return;
        }

        boolean showMessage = activity.getSharedPreferences("message", Context.MODE_PRIVATE).getBoolean("showMessage", true);

        if (appOpen.messageAvailable && showMessage) {

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

            builder.setMessage(appOpen.message.message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                markMessageViewed();
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                try {
                                    markMessageViewed();
                                } catch (Exception e) {
                                    Logger.e(e);
                                }
                            }
                        });

            if (messagesListener != null) {
                messagesListener.onMessage(builder.create());
            } else {
                builder.create().show();
            }

            //update showMessage bool from sharedPrefs depending on message showSettings
            activity.getSharedPreferences("message", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("showMessage", appOpen.message.showSetting.equals("show_always"))
                    .commit();

        }
    }

    public void markMessageViewed() {
        try {
            BackendManager.getInstance().viewMessage(settings, appOpen.message.id, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.e(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void handleVersionControl(Activity activity) {
        if(appOpen == null) {
            Logger.e("HandleVersionControl", "App open object is null, parsing failed or response timed out.");
            return;
        }

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

    public interface MessagesCallbacks {
        void onMessage(Dialog dialog);
    }

    public interface AppOpenCallbacks {
        void onUpdated(boolean cached);
        void onFailure();
    }

}
