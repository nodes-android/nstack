package dk.nodes.nstack.util.appopen;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import dk.nodes.nstack.NStack;
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
        SharedPreferences.Editor editor = context.getSharedPreferences(APPOPEN_INFO_KEY, Context.MODE_PRIVATE).edit();

        editor.putString(VERSION_INFO_KEY, version).apply();
        editor.putString(GUID_KEY, guid).apply();
        editor.putString(LAST_UPDATED_KEY, lastUpdatedString);
        editor.commit();
    }

    public void load() {
        Context context = NStack.getStack().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(APPOPEN_INFO_KEY, Context.MODE_PRIVATE);
        oldVersion = prefs.getString(VERSION_INFO_KEY, version);
        guid = prefs.getString(GUID_KEY, UUID.randomUUID().toString());
        lastUpdatedString = prefs.getString(LAST_UPDATED_KEY, dateFormat.format(new Date(0)));
        /*
        String str_date = prefs.getString(LAST_UPDATED_KEY, dateFormat.format(new Date()));
        try {
            lastUpdated = dateFormat.parse(str_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
    }

    public static void resetLastUpdated() {
        Context context = NStack.getStack().getApplicationContext();
        SharedPreferences.Editor editor = context.getSharedPreferences(APPOPEN_INFO_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(LAST_UPDATED_KEY, null);
        editor.commit();
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
