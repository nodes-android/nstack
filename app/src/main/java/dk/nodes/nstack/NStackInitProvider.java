package dk.nodes.nstack;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by bison on 05/01/17.
 */

public class NStackInitProvider extends ContentProvider {
    public static final String TAG = NStackInitProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        Log.e(TAG, "NStack init provider onCreate");
        try {
            ApplicationInfo ai = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if(bundle.containsKey("dk.nodes.nstack.appId") && bundle.containsKey("dk.nodes.nstack.apiKey")) {
                String appId = bundle.getString("dk.nodes.nstack.appId");
                String apiKey = bundle.getString("dk.nodes.nstack.apiKey");
                //Log.e(TAG, "Read appId = " + appId + " apiKey = " + apiKey);
                NStack.init(getContext(), appId, apiKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
