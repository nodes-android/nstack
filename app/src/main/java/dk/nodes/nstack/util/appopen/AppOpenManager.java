package dk.nodes.nstack.util.appopen;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.R;
import dk.nodes.nstack.util.appopen.message.MessageListener;
import dk.nodes.nstack.util.appopen.ratereminder.RateReminderListener;
import dk.nodes.nstack.util.appopen.versioncontrol.VersionControlDebug;
import dk.nodes.nstack.util.appopen.versioncontrol.VersionControlExListener;
import dk.nodes.nstack.util.appopen.versioncontrol.VersionControlListener;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.cache.CacheManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.translation.manager.TranslationManager;
import dk.nodes.nstack.util.translation.options.TranslationOptions;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by joso on 17/11/15.
 */
public class AppOpenManager {
    private final static String TAG = AppOpenManager.class.getSimpleName();
    private final static String KEY_SETTINGS = "APP_OPEN_SETTINGS";

    private RateReminderListener rateReminderListener;
    private MessageListener messageListener;

    private VersionControlListener versionControlListener;
    private VersionControlExListener versionControlExListener;
    private AppOpen appOpen;
    private AppOpenSettings settings;

    private Context context;
    private BackendManager backendManager;
    private CacheManager cacheManager;
    private TranslationManager translationManager;
    private TranslationOptions translationOptions;

    private enum VersionControlType {
        UPDATE, FORCE_UPDATE, CHANGELOG, NOTHING
    }

    public AppOpenManager(Context context, BackendManager backendManager, TranslationManager translationManager, CacheManager cacheManager, TranslationOptions translationOptions) {
        this.context = context;
        this.backendManager = backendManager;
        this.translationManager = translationManager;
        this.translationOptions = translationOptions;
        this.cacheManager = cacheManager;
        settings = new AppOpenSettings(context);
        checkSettings();
    }

    private void checkSettings() {
        try {
            settings = (AppOpenSettings) cacheManager.loadObject(NStack.getStack().getApplicationContext(), KEY_SETTINGS);

            if (settings == null) {
                settings = new AppOpenSettings(context);
            }
        } catch (Exception e) {
            settings = new AppOpenSettings(context);
        }

        if (settings.guid == null) {
            settings.guid = UUID.randomUUID().toString();
        }
    }

    public void checkVersionControl(final Activity activity,
                                    @Nullable VersionControlListener versionControlListener) {
        this.versionControlListener = versionControlListener;
        handleVersionControl(activity);
    }

    public void checkVersionControl(final Activity activity,
                                    @Nullable VersionControlExListener versionControlExListener) {
        this.versionControlExListener = versionControlExListener;
        handleVersionControlEx(activity);
    }

    public void checkRateReminder(final Activity activity, @Nullable RateReminderListener rateReminderListener) {
        this.rateReminderListener = rateReminderListener;
        handleRateRequest(activity);
    }

    public void checkMessages(final Activity activity, @Nullable MessageListener messageListener) {
        this.messageListener = messageListener;
        handleMessages(activity);
    }

    public void openApp(@NonNull final AppOpenListener appOpenListener) {
        final String languageLocale;
        if (cacheManager.getCurrentLanguageLocale() != null && !translationOptions.isForceRefreshLocale()) {
            updateTranslationsFromCache();
            languageLocale = cacheManager.getCurrentLanguageLocale();
        } else {
            languageLocale = translationOptions.getLanguageHeader();
        }

        backendManager.getAppOpen(Constants.BASE_URL, settings, languageLocale, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                appOpenListener.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    appOpenListener.onFailure();
                    return;
                }
                try {
                    JSONObject root = new JSONObject(response.body().string());
                    appOpen = AppOpen.parseFromJson(root);
                    JSONObject jo = root.getJSONObject("data");
                    String responseLanguageLocale = root.getJSONObject("meta").getJSONObject("language").getString("locale");
                    if (jo != null && jo.has("translate")) {
                        JSONObject translateJson = jo.optJSONObject("translate");
                        if (translateJson == null) {
                            appOpenListener.onFailure();
                            return;
                        }
                        translationManager.updateTranslationClass(translateJson.toString());
                        translationManager.getCacheManager().setCurrentLanguageLocale(responseLanguageLocale);
                        translationManager.saveLanguageTranslation(responseLanguageLocale, translateJson.toString());
                        translationManager.getCacheManager().clearLastUpdated();
                        settings.save();
                        appOpenListener.onUpdated(false);
                        return;
                    }
                    appOpenListener.onUpdated(true);
                } catch (Exception e) {
                    appOpenListener.onFailure();
                }
            }
        });
    }

    private void updateTranslationsFromCache() {
        if (cacheManager.getJsonTranslation(cacheManager.getCurrentLanguageLocale()) != null) {
            translationManager.updateTranslationClass(cacheManager.getJsonTranslation(cacheManager.getCurrentLanguageLocale()));
            Logger.d("Updated translations from cache... " + cacheManager.getCurrentLanguageLocale() + " "
                    + cacheManager.getJsonTranslation(cacheManager.getCurrentLanguageLocale()));
        }
    }

    private void handleRateRequest(final Activity activity) {
        if (appOpen == null) {
            Logger.e("HandleRateRequestControl", "App open object is null, parsing failed or response timed out.");
            return;
        }

        if (appOpen.isRateRequestAvailable() && cacheManager.getRateReminder()) {

            AlertDialog.Builder builder;

            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext()
                    );
                } else {
                    builder = new AlertDialog.Builder(activity);
                }
            } else {
                builder = new AlertDialog.Builder(activity);
            }

            builder.setTitle(appOpen.rateReminder.getTitle())
                    .setMessage(appOpen.rateReminder.getBody())
                    .setPositiveButton(appOpen.rateReminder.getYesBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                NStack.getStack().getApplicationContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.getStoreLink())));
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setNeutralButton(appOpen.rateReminder.getLaterBtn(), null)
                    .setNegativeButton(appOpen.rateReminder.getNoBtn(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cacheManager.setRateReminder(false);
                        }
                    });

            if (rateReminderListener != null) {
                rateReminderListener.onRateReminder(builder.create());
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

        if (appOpen.isMessageAvailable() && cacheManager.getShowMessage()) {

            AlertDialog.Builder builder;

            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext()
                    );
                } else {
                    builder = new AlertDialog.Builder(activity);
                }
            } else {
                builder = new AlertDialog.Builder(activity);
            }

            builder.setMessage(appOpen.message.getMessage())
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

            if (messageListener != null) {
                messageListener.onMessage(builder.create());
            } else {
                builder.create().show();
            }

            //update showMessage bool from sharedPrefs depending on message showSettings
            cacheManager.setShowMessage(appOpen.message.getShowSetting().equals("show_always"));
        }
    }

    public void markMessageViewed() {
        try {
            backendManager.viewMessage(settings, appOpen.message.getId(), new Callback() {
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

    private void simulateVersionUpdate(Activity activity)
    {
        AlertDialog.Builder builder;
        if (activity instanceof AppCompatActivity) {
            if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                builder = new AlertDialog.Builder(
                        ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                        R.style.znstack_DialogStyle
                );
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }
        } else {
            builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
        }

        builder.setMessage("This is a locally simulated version update meant for testing app behavior")
                .setTitle("Simulated update")
                .setPositiveButton("positiveButton", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("negativeButton", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setCancelable(true);

        runVersionControlListener(builder, VersionControlType.UPDATE);
    }


    private void simulateVersionForceUpdate(Activity activity)
    {
        AlertDialog.Builder builder;
        if (activity instanceof AppCompatActivity) {
            if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                builder = new AlertDialog.Builder(
                        ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                        R.style.znstack_DialogStyle
                );
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }
        } else {
            builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
        }

        builder.setMessage("This is a locally simulated version force update meant for testing app behavior")
                .setTitle("Simulated force update")
                .setCancelable(false)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com"));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            NStack.getStack().getApplicationContext().startActivity(i);
                        } catch (Exception e) {
                            Logger.e(e);
                        }
                    }
                });
        runVersionControlListener(builder, VersionControlType.FORCE_UPDATE);
    }

    @Deprecated
    private void handleVersionControl(Activity activity)
    {
        if(VersionControlDebug.simulateUpdate)
        {
            simulateVersionUpdate(activity);
            return;
        }

        if(VersionControlDebug.simulateForceUpdate)
        {
            simulateVersionForceUpdate(activity);
            return;
        }

        if (appOpen == null) {
            Logger.e("HandleVersionControl", "App open object is null, parsing failed or response timed out.");
            return;
        }

        // Smallish naming hack
        if (appOpen.update != null) {
            if (appOpen.update.getPositiveBtn() != null) {
                if (appOpen.update.getPositiveBtn().contains("AppStore")) {
                    appOpen.update.setPositiveBtn(appOpen.update.getPositiveBtn().replace("AppStore", "Play Store"));
                }
            }
        }


        // Forced update
        if (appOpen.isUpdateAvailable() && appOpen.isForcedUpdate()) {

            AlertDialog.Builder builder;
            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                            R.style.znstack_DialogStyle
                    );
                } else {
                    builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
                }
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }

            builder
                    .setTitle(appOpen.update.getTitle())
                    .setMessage(appOpen.update.getMessage())
                    .setPositiveButton(appOpen.update.getPositiveBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.getStoreLink()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NStack.getStack().getApplicationContext().startActivity(i);
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setCancelable(false);
            runVersionControlListener(builder, VersionControlType.FORCE_UPDATE);
        }
        // Normal update
        else if (appOpen.isUpdateAvailable())

        {
            AlertDialog.Builder builder;
            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                            R.style.znstack_DialogStyle
                    );
                } else {
                    builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
                }
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }

            builder.setMessage(appOpen.update.getMessage())
                    .setTitle(appOpen.update.getTitle())
                    .setPositiveButton(appOpen.update.getPositiveBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.getStoreLink()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NStack.getStack().getApplicationContext().startActivity(i);

                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setNegativeButton(appOpen.update.getNegativeBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setCancelable(true);

            runVersionControlListener(builder, VersionControlType.UPDATE);
        }
        // Updated, show change log
        else if (appOpen.isChangelogAvailable()) {
            AlertDialog.Builder builder;
            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                            R.style.znstack_DialogStyle
                    );
                } else {
                    builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
                }
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }

            builder.setTitle(appOpen.update.getTitle())
                    .setMessage(appOpen.update.getMessage())
                    .setPositiveButton(appOpen.update.getNegativeBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(true);

            runVersionControlListener(builder, VersionControlType.CHANGELOG);
        } else if (versionControlListener != null) {
            runVersionControlListener(null, VersionControlType.NOTHING);
        }
    }

    private void handleVersionControlEx(Activity activity) {
        if(VersionControlDebug.simulateUpdate)
        {
            simulateVersionUpdate(activity);
            return;
        }
        if(VersionControlDebug.simulateForceUpdate)
        {
            simulateVersionForceUpdate(activity);
            return;
        }

        if (appOpen == null) {
            Logger.e("HandleVersionControl", "App open object is null, parsing failed or response timed out.");
            return;
        }

        // Smallish naming hack
        if (appOpen.update != null) {
            if (appOpen.update.getPositiveBtn() != null) {
                if (appOpen.update.getPositiveBtn().contains("AppStore")) {
                    appOpen.update.setPositiveBtn(appOpen.update.getPositiveBtn().replace("AppStore", "Play Store"));
                }
            }
        }


        // Forced update
        if (appOpen.isUpdateAvailable() && appOpen.isForcedUpdate()) {

            AlertDialog.Builder builder;
            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                            R.style.znstack_DialogStyle
                    );
                } else {
                    builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
                }
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }

            builder
                    .setTitle(appOpen.update.getTitle())
                    .setMessage(appOpen.update.getMessage())
                    .setPositiveButton(appOpen.update.getPositiveBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.getStoreLink()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NStack.getStack().getApplicationContext().startActivity(i);
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setCancelable(false);

            runVersionControlListener(builder, VersionControlType.FORCE_UPDATE);
        }
        // Normal update
        else if (appOpen.isUpdateAvailable())

        {
            AlertDialog.Builder builder;
            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                            R.style.znstack_DialogStyle
                    );
                } else {
                    builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
                }
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }

            builder.setMessage(appOpen.update.getMessage())
                    .setTitle(appOpen.update.getTitle())
                    .setPositiveButton(appOpen.update.getPositiveBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appOpen.getStoreLink()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NStack.getStack().getApplicationContext().startActivity(i);
                            } catch (Exception e) {
                                Logger.e(e);
                            }
                        }
                    })
                    .setNegativeButton(appOpen.update.getNegativeBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setCancelable(true);

            runVersionControlListener(builder, VersionControlType.UPDATE);
        }
        // Updated, show change log
        else if (appOpen.isChangelogAvailable()) {
            AlertDialog.Builder builder;
            if (activity instanceof AppCompatActivity) {
                if (((AppCompatActivity) activity).getSupportActionBar() != null) {
                    builder = new AlertDialog.Builder(
                            ((AppCompatActivity) activity).getSupportActionBar().getThemedContext(),
                            R.style.znstack_DialogStyle
                    );
                } else {
                    builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
                }
            } else {
                builder = new AlertDialog.Builder(activity, R.style.znstack_DialogStyle);
            }

            builder.setTitle(appOpen.update.getTitle())
                    .setMessage(appOpen.update.getMessage())
                    .setPositiveButton(appOpen.update.getNegativeBtn(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(true);

            runVersionControlListener(builder, VersionControlType.CHANGELOG);
        } else if (versionControlExListener != null) {
            runVersionControlListener(null, VersionControlType.NOTHING);
        }
    }


    private void runVersionControlListener(final Object dialogOrBuilder, final VersionControlType type)
    {
        if(versionControlListener == null && versionControlExListener == null)
        {
            runVersionUpdateOnMainThread(null, dialogOrBuilder, type);
        }
        if(versionControlExListener != null) {
            runVersionUpdateOnMainThread(versionControlExListener, dialogOrBuilder, type);
        }
        if(versionControlListener != null) {
            runVersionUpdateOnMainThread(versionControlListener, dialogOrBuilder, type);
        }
    }

    private void runVersionUpdateOnMainThread(@Nullable final Object listener, final Object dialogOrBuilder, final VersionControlType type)
    {
        Handler mainHandler = new Handler(context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                if(listener == null) {
                    if (dialogOrBuilder instanceof AlertDialog.Builder) {
                        ((AlertDialog.Builder) dialogOrBuilder).show();
                    }
                    if (dialogOrBuilder instanceof AlertDialog) {
                        ((AlertDialog) dialogOrBuilder).show();
                    }
                }
                else
                {
                    if(listener instanceof VersionControlListener)
                    {
                        switch(type)
                        {
                            case UPDATE:
                                ((VersionControlListener) listener).onUpdate(((AlertDialog.Builder) dialogOrBuilder).create());
                                break;
                            case FORCE_UPDATE:
                                ((VersionControlListener) listener).onForcedUpdate(((AlertDialog.Builder) dialogOrBuilder).create());
                                break;
                            case CHANGELOG:
                                ((VersionControlListener) listener).onChangelog(((AlertDialog.Builder) dialogOrBuilder).create());
                                break;

                            case NOTHING:
                                ((VersionControlListener) listener).onNothing();
                                break;
                        }

                    }
                    if(listener instanceof VersionControlExListener)
                    {
                        switch(type)
                        {
                            case UPDATE:
                                ((VersionControlExListener) listener).onUpdate((AlertDialog.Builder) dialogOrBuilder);
                                break;
                            case FORCE_UPDATE:
                                ((VersionControlExListener) listener).onForcedUpdate((AlertDialog.Builder) dialogOrBuilder);
                                break;
                            case CHANGELOG:
                                ((VersionControlExListener) listener).onChangelog((AlertDialog.Builder) dialogOrBuilder);
                                break;

                            case NOTHING:
                                ((VersionControlExListener) listener).onNothing();
                                break;
                        }

                    }
                }
            }
        };
        mainHandler.post(myRunnable);
    }

}
