package dk.nodes.nstack.util.log;

import android.util.Log;

import dk.nodes.nstack.NStack;

/**
 * Created by joso on 02/10/2015.
 */
public class NLog {
    private final static String TAG = "NLog";

    public static void d(String msg) {
        if(NStack.getStack().isDebugMode()) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if(NStack.getStack().isDebugMode()) {
            Log.d(tag, msg);
        }
    }

    public static void e(Exception e) {
        if(NStack.getStack().isDebugMode()) {
            Log.e(TAG, e.toString());
        }
    }

    public static void e(String msg) {
        if(NStack.getStack().isDebugMode()) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if(NStack.getStack().isDebugMode()) {
            Log.e(tag, msg);
        }
    }
}
