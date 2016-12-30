package dk.nodes.nstack.util.appopen;

import org.json.JSONObject;

import dk.nodes.nstack.util.appopen.message.Message;
import dk.nodes.nstack.util.appopen.ratereminder.RateReminder;
import dk.nodes.nstack.util.appopen.update.Update;
import dk.nodes.nstack.util.appopen.versioncontrol.VersionControl;
import dk.nodes.nstack.util.log.Logger;

/**
 * Created by joso on 17/11/15.
 */
public class AppOpen {

    public final Update update = new Update();
    public final VersionControl versionControl = new VersionControl();
    public final RateReminder rateReminder = new RateReminder();
    public final Message message = new Message();

    public boolean rateRequestAvailable;
    public boolean updateAvailable;
    public boolean forcedUpdate;
    public boolean changelogAvailable;
    public boolean messageAvailable;

    JSONObject translationRoot;

    public String versionDescription;
    public String storeLink;

    private AppOpen() {

    }

    public static AppOpen parseFromJson(JSONObject json) {
        AppOpen appopen = new AppOpen();
        // Update
        try {
            JSONObject updateObject = json.getJSONObject("data").getJSONObject("update");
            JSONObject translateObject = null;

            if (updateObject.has("newer_version")) {
                JSONObject newerVersion = updateObject.getJSONObject("newer_version");
                translateObject = newerVersion.getJSONObject("translate");

                String state = newerVersion.optString("state", "no");
                appopen.storeLink = newerVersion.optString("link");
                appopen.versionDescription = newerVersion.optString("version");

                if (state.equalsIgnoreCase("force")) {
                    appopen.updateAvailable = true;
                    appopen.forcedUpdate = true;
                } else if (state.equalsIgnoreCase("yes")) {
                    appopen.updateAvailable = true;
                }
            } else if (updateObject.has("new_in_version")) {
                JSONObject newInVersion = updateObject.getJSONObject("new_in_version");
                translateObject = newInVersion.getJSONObject("translate");
                boolean state = newInVersion.optBoolean("state", false);
                appopen.versionDescription = newInVersion.optString("version");
                appopen.changelogAvailable = state;
            }

            if (translateObject != null) {
                appopen.update.setTitle(translateObject.optString("title"));
                appopen.update.setMessage(translateObject.optString("message"));
                appopen.update.setPositiveBtn(translateObject.optString("positiveBtn"));
                appopen.update.setNegativeBtn(translateObject.optString("negativeBtn"));
            }
        } catch (Exception e) {
            Logger.e(e);
        }

        // Version control
        try {
            JSONObject translateObject = json.getJSONObject("data").getJSONObject("translate");
            JSONObject versionControlObject = translateObject.getJSONObject("versionControl");

            appopen.versionControl.setForceHeader(versionControlObject.optString("forceHeader"));
            appopen.versionControl.setNegativeBtn(versionControlObject.optString("negativeBtn"));
            appopen.versionControl.setNewInVersionHeader(versionControlObject.optString("newInVersionHeader"));
            appopen.versionControl.setOkBtn(versionControlObject.optString("okBtn"));
            appopen.versionControl.setUpdateHeader(versionControlObject.optString("updateHeader"));
            appopen.versionControl.setPositiveBtn(versionControlObject.optString("positiveBtn"));
        } catch (Exception e) {
            Logger.e(e);
        }

        // Rate reminder
        try {
            JSONObject translateObject = json.getJSONObject("data").getJSONObject("translate");
            JSONObject versionControlObject = translateObject.getJSONObject("rateReminder");

            appopen.rateReminder.setBody(versionControlObject.optString("body"));
            appopen.rateReminder.setLaterBtn(versionControlObject.optString("laterBtn"));
            appopen.rateReminder.setNoBtn(versionControlObject.optString("noBtn"));
            appopen.rateReminder.setTitle(versionControlObject.optString("title"));
            appopen.rateReminder.setYesBtn(versionControlObject.optString("yesBtn"));

            appopen.rateRequestAvailable = true;
        } catch (Exception e) {
            Logger.e(e);
        }

        // Use versionControl translations if none are provided by update object
        //TODO WUT?
        if (appopen.update.getTitle() == null) {
            appopen.update.setTitle(appopen.forcedUpdate ? appopen.versionControl.getForceHeader() :
                    appopen.updateAvailable ? appopen.versionControl.getUpdateHeader() :
                            appopen.changelogAvailable ? appopen.versionControl.getNewInVersionHeader() : "");
        }

        // General Translations
        try {
            JSONObject translateObject = json.getJSONObject("data").getJSONObject("translate");

            if (translateObject.has("default") || translateObject.has("defaultSection")) {
                appopen.translationRoot = translateObject;
            }
        } catch (Exception e) {
            Logger.e(e);
        }

        // Message
        try {
            JSONObject messageObject = json.getJSONObject("data").getJSONObject("message");

            appopen.message.setId(messageObject.optInt("id"));
            appopen.message.setProjectId(messageObject.optInt("project_id"));
            appopen.message.setPlatform(messageObject.optString("platform"));
            appopen.message.setShowSetting(messageObject.optString("show_setting"));
            appopen.message.setViewCount(messageObject.optInt("view_count"));
            appopen.message.setMessage(messageObject.optString("message"));

            appopen.messageAvailable = true;

        } catch (Exception e) {
            Logger.e(e);
        }

        return appopen;
    }

}
