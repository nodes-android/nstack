package dk.nodes.nstackexampleproject;

import android.app.Application;
import android.util.Log;

import java.io.IOException;

import dk.nodes.nstack.NStack;
import dk.nodes.nstackexampleproject.util.model.Translation;
import okhttp3.Call;

/**
 * Created by joso on 18/11/15.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // NStack
        NStack.init(this, "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");
        NStack.getStack().enableDebug();

        //If we want to force a locale refresh every time - should only be used in apps without select
        // language screen that want to use phone locale as default always
        //NStack.getStack().forceRefreshLocale();

        // Translation
        NStack.getStack().translationClass(Translation.class);

        // NStack content download
        try {
            NStack.getStack().getContentResponse(25, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Example project", "Failed to download content");
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    Log.d("Example project", "downloaded content " + response.body().string());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
