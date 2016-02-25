package dk.nodes.nstack.util.cache;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dk.nodes.nstack.util.log.Logger;
/**
 * Created by joso on 19/11/15.
 */
public class CacheManager {

    public static void saveObject(Context context, String key, Object object) {
        try {
            FileOutputStream fos = context.openFileOutput (key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch( Exception e ) {
            Logger.e(e);
        }
    }

    public static Object loadObject (Context context, String key) {
        try {
            FileInputStream fis = context.openFileInput(key);
            ObjectInputStream ois = new ObjectInputStream (fis);
            Object object = ois.readObject ();
            return object;
        } catch( Exception e ) {
            Logger.e(e);
        }

        return null;
    }

}
