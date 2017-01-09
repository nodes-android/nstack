package dk.nodes.nstack.util.cache;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dk.nodes.nstack.util.log.Logger;

/**
 * Created by Mario on 28/12/2016.
 */

public class FileCache {

    // File Cache
    public static void saveObject(Context context, String key, Object object) {
        try {
            FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public static Object loadObject(Context context, String key) {
        try {
            FileInputStream fis = context.openFileInput(key);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object object = ois.readObject();
            ois.close();
            return object;
        } catch (Exception e) {
            Logger.e(e);
        }

        return null;
    }
}
