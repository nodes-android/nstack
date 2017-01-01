package dk.nodes.nstack.util.appopen;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import dk.nodes.nstack.util.cache.CacheManager;
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

    private CacheManager cacheManager;

    public AppOpenSettings(Context context) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        guid = UUID.randomUUID().toString();
        cacheManager = new CacheManager(context);

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
        cacheManager.setVersionInfo(version);
        cacheManager.setGUI(guid);
        cacheManager.setLastUpdated(lastUpdatedString);
    }

    public void load() {
        oldVersion = cacheManager.getVersionInfo() != null ? cacheManager.getVersionInfo() : version;
        guid = cacheManager.getGUI() != null ? cacheManager.getGUI() : UUID.randomUUID().toString();
        lastUpdatedString = cacheManager.getLastUpdated() != null ? cacheManager.getLastUpdated() : dateFormat.format(new Date(0));
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
