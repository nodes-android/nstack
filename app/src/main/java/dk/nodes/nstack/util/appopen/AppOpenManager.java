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

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.log.Logger;
/**
 * Created by joso on 17/11/15.
 */
public class AppOpenManager {

    private final static String KEY_SETTINGS = "APP_OPEN_SETTINGS";
    private final static String BASE_URL = "https://nstack.io/api/v1/open";

    private AppOpenCallbacks listener;
    private AppOpen appOpen;
    private AppOpenSettings settings = new AppOpenSettings();

    private boolean updateTranslationsFromAppOpen = true;

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

    public void openApp(final Activity activity, @Nullable AppOpenCallbacks listener) {
        this.listener = listener;

        BackendManager.getInstance().getAppOpen(BASE_URL, settings, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if( AppOpenManager.this.listener != null ) {
                    AppOpenManager.this.listener.onFailure();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);

                    appOpen = AppOpen.parseFromJson(root);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            handleTranslations();
                            handleVersionControl(activity);
                            handleRateRequest(activity);
                        }
                    });



                } catch(Exception e) {
                    Logger.e(e);

                    if( AppOpenManager.this.listener != null ) {
                        AppOpenManager.this.listener.onFailure();
                    }
                }
            }
        });
    }

    private void handleTranslations() {
        if( appOpen.translationRoot == null ) {
            return;
        }

        if( updateTranslationsFromAppOpen ) {
            NStack.getStack().getTranslationManager().updateTranslationsFromAppOpen(appOpen.translationRoot);
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
                    builder = new AlertDialog.Builder(
                           activity
                    );
                }
            } else{
                builder = new AlertDialog.Builder(
                        activity
                );
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

            if (listener != null) {
                listener.onRateReminder(builder.create());
            } else {
                builder.create().show();
            }
        }
    }

    private void handleVersionControl(Activity activity) {
        // Forced update
        if( appOpen.updateAvailable && appOpen.forcedUpdate ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    activity instanceof AppCompatActivity ? ((AppCompatActivity) activity).getSupportActionBar().getThemedContext() : activity
                    //, R.style.myDialog
            );

            builder .setTitle(appOpen.update.title)
                    .setMessage(appOpen.update.message)
                    .setPositiveButton(appOpen.update.positiveBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                NStack.getStack().getApplicationContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.storeLink)));
                            } catch( Exception e ) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setCancelable(false);

            if( listener != null ) {
                listener.onForcedUpdate(builder.create());
            } else {
                builder.create().show();
            }
        }

        // Normal update
        else if( appOpen.updateAvailable ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    activity instanceof AppCompatActivity ? ((AppCompatActivity) activity).getSupportActionBar().getThemedContext() : activity
                    //, R.style.myDialog
            );

            builder .setTitle(appOpen.update.title)
                    .setMessage(appOpen.update.message)
                    .setPositiveButton(appOpen.update.positiveBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                NStack.getStack().getApplicationContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.storeLink)));
                            } catch( Exception e ) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setNegativeButton(appOpen.versionControl.negativeBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setCancelable(true);

            if( listener != null ) {
                listener.onUpdate(builder.create());
            } else {
                builder.create().show();
            }
        }

        // Updated, show change log
        else if( appOpen.changelogAvailable ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    activity instanceof AppCompatActivity ? ((AppCompatActivity) activity).getSupportActionBar().getThemedContext() : activity
                    //, R.style.myDialog
            );

            builder .setTitle(appOpen.versionControl.newInVersionHeader)
                    .setMessage(appOpen.update.message)
                    .setPositiveButton(appOpen.versionControl.okBtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(true);

            if( listener != null ) {
                listener.onChangelog(builder.create());
            } else {
                builder.create().show();
            }
        }
    }

    public interface AppOpenCallbacks {
        void onForcedUpdate(Dialog dialog);
        void onUpdate(Dialog dialog);
        void onChangelog(Dialog dialog);
        void onRateReminder(Dialog dialog);
        void onFailure();
    }

    public boolean updateTranslationsFromAppOpen() {
        return updateTranslationsFromAppOpen;
    }

    public void setUpdateTranslationsFromAppOpen(boolean updateTranslationsFromAppOpen) {
        this.updateTranslationsFromAppOpen = updateTranslationsFromAppOpen;
    }
}
