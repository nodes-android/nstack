package dk.nodes.nstack.util.appopen;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import dk.nodes.nstack.NStack;
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
    private final static String APPOPEN_INFO_KEY = "APPOPEN_INFO";
    private final static String VERSION_INFO_KEY = "VERSION_KEY";
    private final static String GUID_KEY = "GUID_KEY";
    private final static String LAST_UPDATED_KEY = "LAST_UPDATED_KEY";
    SimpleDateFormat dateFormat;

    public AppOpenSettings() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        guid = UUID.randomUUID().toString();

        try {
            Context applicationContext = NStack.getStack().getApplicationContext();
            version = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0).versionName;
            oldVersion = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Logger.e(e);
        }

        lastUpdated = new Date();
        lastUpdatedString = dateFormat.format(lastUpdated);
        load();
    }

    public void save() {
        Context context = NStack.getStack().getApplicationContext();
        CacheManager.with(context, APPOPEN_INFO_KEY).putString(CacheManager.Key.VERSION_INFO_KEY, version);
        CacheManager.with(context, APPOPEN_INFO_KEY).putString(CacheManager.Key.GUID_KEY, guid);
        CacheManager.with(context, APPOPEN_INFO_KEY).putString(CacheManager.Key.LAST_UPDATED_KEY, lastUpdatedString);
    }

    public void load() {
        Context context = NStack.getStack().getApplicationContext();
        oldVersion = CacheManager.with(context, APPOPEN_INFO_KEY).getString(CacheManager.Key.VERSION_INFO_KEY);
        guid = CacheManager.with(context, APPOPEN_INFO_KEY).getString(CacheManager.Key.GUID_KEY);
        lastUpdatedString = CacheManager.with(context, APPOPEN_INFO_KEY).getString(CacheManager.Key.LAST_UPDATED_KEY);
    }

    public static void resetLastUpdated() {
        Context context = NStack.getStack().getApplicationContext();
        CacheManager.with(context, APPOPEN_INFO_KEY).clear(CacheManager.Key.LAST_UPDATED_KEY);
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
