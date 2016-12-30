package dk.nodes.nstackexampleproject;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.translation.manager.OnTranslationResultListener;
import dk.nodes.nstack.util.translation.manager.TranslationManager;
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

        // Translation
        NStack.getStack().translationClass(Translation.class);
        NStack.getStack().getTranslationOptions().setLanguageHeader("es-ES");
        NStack.getStack().getTranslationOptions().setFallbackLanguageHeader("es-ES");

//        To test changing the language on the phone
//        String transLocale = Locale.getDefault().toString().replace('_', '-');
//
//        NStack.getStack().changeLanguage(transLocale, new OnTranslationResultListener() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onFailure() {
//
//            }
//        });

        // NStack content download
        try {
            NStack.getStack().getContentResponse(25, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Example project", "Failed to download content");
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    Log.d("Example project", "downloaded content " + response.body().toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
