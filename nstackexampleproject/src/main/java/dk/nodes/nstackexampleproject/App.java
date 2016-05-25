package dk.nodes.nstackexampleproject;

import android.app.Application;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstackexampleproject.util.model.Translation;
/**
 * Created by joso on 18/11/15.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // NStack
        NStack.init(this, "bOdrNuZd4syxuAz6gyCb3xwBCjA8U4h4IcQI", "X0ENl5QpKI51tS9CzKSt1PGwfZeq2gBMTU58");
        NStack.getStack().enableDebug();

        // Translation
        NStack.getStack().getTranslationManager().setTranslationClass(Translation.class);
        // These are the default options
        NStack.getStack()
                .getTranslationManager()
                .options()
                .fallbackLocale("en-US");

        // Translation callback
        NStack.getStack().getTranslationManager().updateTranslationsSilently();

        // NStack content download
        try {
            BackendManager.getInstance().getContentResponse(25, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e("Example project", "Failed to download content");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    Log.d("Example project", "downloaded content " + response.body().toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
