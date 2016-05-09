package dk.nodes.nstack.util.appopen;

import org.json.JSONObject;

import dk.nodes.nstack.util.log.Logger;
/**
 * Created by joso on 17/11/15.
 */
public class AppOpen {

    public final Update update = new Update();
    public final VersionControl versionControl = new VersionControl();
    public final RateReminder rateReminder = new RateReminder();

    public boolean updateAvailable = false;
    public boolean forcedUpdate = false;
    public boolean changelogAvailable = false;

    public JSONObject translationRoot;

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

            if( updateObject.has("newer_version") ) {
                JSONObject newerVersion = updateObject.getJSONObject("newer_version");
                translateObject = newerVersion.getJSONObject("translate");

                String state = newerVersion.optString("state", "no");
                appopen.storeLink = newerVersion.optString("link");
                appopen.versionDescription = newerVersion.optString("version");

                if( state.equalsIgnoreCase("force") ) {
                    appopen.updateAvailable = true;
                    appopen.forcedUpdate = true;
                }

                else if( state.equalsIgnoreCase("yes") ) {
                    appopen.updateAvailable = true;
                }
            }

            else if( updateObject.has("new_in_version") ) {
                JSONObject newInVersion = updateObject.getJSONObject("new_in_version");
                translateObject = newInVersion.getJSONObject("translate");

                appopen.versionDescription = newInVersion.optString("version");
                appopen.changelogAvailable = true;
            }

            if( translateObject != null ) {
                appopen.update.title = translateObject.optString("title");
                appopen.update.message = translateObject.optString("message");
                appopen.update.positiveBtn = translateObject.optString("positiveBtn");
            }
        } catch( Exception e ) {
            Logger.e(e);
        }

        // Version control
        try {
            JSONObject translateObject = json.getJSONObject("data").getJSONObject("translate");
            JSONObject versionControlObject = translateObject.getJSONObject("versionControl");

            appopen.versionControl.forceHeader = versionControlObject.optString("forceHeader");
            appopen.versionControl.negativeBtn = versionControlObject.optString("negativeBtn");
            appopen.versionControl.newInVersionHeader = versionControlObject.optString("newInVersionHeader");
            appopen.versionControl.okBtn = versionControlObject.optString("okBtn");
            appopen.versionControl.updateHeader = versionControlObject.optString("updateHeader");
            appopen.versionControl.positiveBtn = versionControlObject.optString("positiveBtn");
        } catch( Exception e ) {
            Logger.e(e);
        }

        // Rate reminder
        try {
            JSONObject translateObject = json.getJSONObject("data").getJSONObject("translate");
            JSONObject versionControlObject = translateObject.getJSONObject("rateReminder");

            appopen.rateReminder.body = versionControlObject.optString("body");
            appopen.rateReminder.laterBtn = versionControlObject.optString("laterBtn");
            appopen.rateReminder.noBtn = versionControlObject.optString("noBtn");
            appopen.rateReminder.title = versionControlObject.optString("title");
            appopen.rateReminder.yesBtn = versionControlObject.optString("yesBtn");
        } catch( Exception e ) {
            Logger.e(e);
        }

        // Use versionControl translations if none are provided by update object
        if (appopen.update.title == null) {
            appopen.update.title =  appopen.forcedUpdate ? appopen.versionControl.forceHeader :
                                    appopen.updateAvailable ? appopen.versionControl.updateHeader :
                                    appopen.changelogAvailable ? appopen.versionControl.newInVersionHeader :
                                    "";
        }

        // General Translations
        try {
            JSONObject translateObject = json.getJSONObject("data").getJSONObject("translate");

            if( translateObject.has("default") || translateObject.has("defaultSection") ) {
                appopen.translationRoot = translateObject;
            }
        } catch( Exception e ) {
            Logger.e(e);
        }

        return appopen;
    }

    class Update {
        public String title;
        public String message;
        public String positiveBtn;
    }

    class VersionControl {
        public String updateHeader;
        public String forceHeader;
        public String negativeBtn;
        public String positiveBtn;
        public String newInVersionHeader;
        public String okBtn;
    }

    class RateReminder {
        public String title;
        public String body;
        public String yesBtn;
        public String laterBtn;
        public String noBtn;
    }

}
