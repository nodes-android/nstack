package dk.nodes.nstack.util.translation.options;

import android.content.Context;
import android.os.Build;

import java.util.Locale;


/**
 * Created by joso on 05/10/15.
 */
public class TranslationOptions {
    private String languageHeader = "en-UK";
    private String fallbackLanguageHeader = "da-DK";
    private boolean allLanguages;
    private String contentUrl;

    public TranslationOptions(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        this.languageHeader = locale.toString().replace("_","-");
    }

    public String getLanguageHeader() {
        return languageHeader;
    }

    /**
     * In the format: en-GB, en-US, da-DK etc.
     */
    public void setLanguageHeader(String languageHeader) {
        this.languageHeader = languageHeader;
    }

    public String getFallbackLanguageHeader() {
        return fallbackLanguageHeader;
    }

    /**
     * In the format: en-GB, en-US, da-DK etc.
     * Decides what language to choose if primary locale isn't present
     */
    public void setFallbackLanguageHeader(String fallbackLanguageHeader) {
        this.fallbackLanguageHeader = fallbackLanguageHeader;
    }

    public boolean isAllLanguages() {
        return allLanguages;
    }

    /**
     * If you want to get all languages keys (should be only used by nstack to save them in shared prefs.
     */
    public void setAllLanguages(boolean allLanguages) {
        this.allLanguages = allLanguages;
    }

    /**
     * @return returns contentUrl if set, else NStack default + settings
     */
    public String getContentUrl() {
        if(contentUrl != null) {
            return contentUrl;
        }
        return Constants.NSTACK_CONTENT_URL + "?all=" + Boolean.toString(allLanguages);
    }

    /**
     * If you have a custom hosted/static json translation file, use this changing where
     */
    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }
}
