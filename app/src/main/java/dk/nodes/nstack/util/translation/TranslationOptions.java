package dk.nodes.nstack.util.translation;

import java.io.File;

/**
 * Created by joso on 05/10/15.
 */
public class TranslationOptions {
    private String languageHeader = "da-DK";
    private String fallbackLocale = "en-US";
    private boolean flattenKeys = false;
    private boolean allLanguages = true;
    private final String NSTACK_CONTENT_URL = "https://baas.like.st/api/v1/translate/mobile/keys";
    private String fallbackFile;
    private String customContentURL = null;

    public TranslationOptions() {
    }

    public TranslationOptions locale(String languageHeader) {
        this.languageHeader = cleanLocale(languageHeader);
        return this;
    }

    /**
     * Fetch all languages for switching languages from the cache
     * @param allLanguages
     * @return
     */
    public TranslationOptions allLanguages( boolean allLanguages ) {
        this.allLanguages = allLanguages;
        return this;
    }

    public TranslationOptions flatten( boolean flattenKeys ) {
        this.flattenKeys = flattenKeys;
        return this;
    }

    /**
     * Fallback file containing all languages for offline language switching
     * @param fallbackFile Path to the json file in Assets
     * @return
     */
    public TranslationOptions fallback( String fallbackFile ) {
        this.fallbackFile = fallbackFile;
        return this;
    }

    /**
     * In the format: en-GB, en-US, da-DK etc.
     * Decides what language to choose if primary locale isn't present
     * @param fallbackLocale
     * @return
     */
    public TranslationOptions fallbackLocale( String fallbackLocale ) {
        this.fallbackLocale = cleanLocale(fallbackLocale);
        return this;
    }

    /**
     * If you have a custom hosted/static json translation file, use this changing where
     * translations are fetched from
     * @param customContentURL
     * @return
     */
    public TranslationOptions customContentURL( String customContentURL ) {
        this.customContentURL = customContentURL;
        return this;
    }

    public String getFallbackFile() {
        return fallbackFile;
    }

    public String getLanguageHeader() {
        return languageHeader;
    }

    public String getFallbackLocale() {
        return fallbackLocale;
    }

    protected boolean isFlattenKeys() {
        return flattenKeys;
    }

    protected boolean allLanguages() {
        return allLanguages;
    }

    /**
     * @return returns customContentURL if set, else NStack default + settings
     */
    protected String getContentURL() {
        if( customContentURL != null ) {
            return customContentURL;
        }

        return NSTACK_CONTENT_URL + "?all=" + Boolean.toString(allLanguages) + "&flat=" + Boolean.toString(flattenKeys);
    }

    /**
     * Locale can be in non-nstack friendly formats, so clean/fix them, ie;
     * en_GB -> en-GB
     * @param locale locale string, ie: "en-GB"
     */
    private String cleanLocale( String locale ) {
        return locale.replaceAll("_","-");
    }
}