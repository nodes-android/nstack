package dk.nodes.nstack.util.appopen;

import android.content.Context;

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

    public AppOpenSettings() {
        guid = UUID.randomUUID().toString();

        try {
            Context applicationContext = NStack.getStack().getApplicationContext();
            version = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0).versionName;
            oldVersion = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0).versionName;
        } catch(Exception e) {
            Logger.e(e);
        }

        lastUpdated = new Date();
    }
}
