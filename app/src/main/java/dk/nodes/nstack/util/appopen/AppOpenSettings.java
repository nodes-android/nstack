package dk.nodes.nstack.util.appopen;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import dk.nodes.nstack.util.cache.PrefsManager;
import dk.nodes.nstack.util.log.Logger;

/**
 * Created by joso on 19/11/15.
 */
public class AppOpenSettings {

    public String guid;
    public String version;
    public String oldVersion;
    public final String platform = "android";
    public Date lastUpdated;
    public String lastUpdatedString;
    SimpleDateFormat dateFormat;

    private PrefsManager prefsManager;

    public AppOpenSettings(Context context) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        guid = UUID.randomUUID().toString();
        prefsManager = new PrefsManager(context);

        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            oldVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Logger.e(e);
        }
        //TODO this line below isn't needed
        lastUpdated = new Date();
        lastUpdatedString = dateFormat.format(lastUpdated);
        load();
    }

    public void save() {
        prefsManager.setVersionInfo(version);
        prefsManager.setGUI(guid);
        prefsManager.setLastUpdated(lastUpdatedString);
    }

    public void load() {
        oldVersion = prefsManager.getVersionInfo() != null ? prefsManager.getVersionInfo() : version;
        guid = prefsManager.getGUI() != null ? prefsManager.getGUI() : UUID.randomUUID().toString();
        lastUpdatedString = prefsManager.getLastUpdated() != null ? prefsManager.getLastUpdated() : dateFormat.format(new Date(0));
    }

    //TODO Why is this here?
    public void resetLastUpdated() {
        prefsManager.clearLastUpdated();
    }

    @Override
    public String toString() {
        return "AppOpenSettings{" +
                "guid='" + guid + '\'' +
                ", version='" + version + '\'' +
                ", oldVersion='" + oldVersion + '\'' +
                ", platform='" + platform + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", lastUpdatedString='" + lastUpdatedString + '\'' +
                ", dateFormat=" + dateFormat +
                '}';
    }
}
