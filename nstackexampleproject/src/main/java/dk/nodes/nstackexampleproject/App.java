package dk.nodes.nstackexampleproject;

import android.app.Application;

import dk.nodes.nstack.NStack;
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

    }
}
