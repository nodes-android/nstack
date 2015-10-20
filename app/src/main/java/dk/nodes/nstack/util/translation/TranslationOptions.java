package dk.nodes.nstack.util.translation;

import java.io.File;

/**
 * Created by joso on 05/10/15.
 */
public class TranslationOptions {
    private String languageHeader = "da-DK";
    private boolean flattenKeys = false;
    private boolean allLanguages = true;
    private final String NSTACK_CONTENT_URL = "https://baas.like.st/api/v1/translate/mobile/keys";
    private String fallbackFile;

    public TranslationOptions() {
    }

    public TranslationOptions locale(String languageHeader) {
        this.languageHeader = languageHeader;
        return this;
    }


    public TranslationOptions allLanguages( boolean allLanguages ) {
        this.allLanguages = allLanguages;
        return this;
    }

    public TranslationOptions flatten( boolean flattenKeys ) {
        this.flattenKeys = flattenKeys;
        return this;
    }

    public TranslationOptions fallback( String fallbackFile ) {
        this.fallbackFile = fallbackFile;
        return this;
    }

    public String getFallbackFile() {
        return fallbackFile;
    }

    public String getLanguageHeader() {
        return languageHeader;
    }

    protected boolean isFlattenKeys() {
        return flattenKeys;
    }

    protected boolean allLanguages() {
        return allLanguages;
    }

    protected String getContentURL() {
        return NSTACK_CONTENT_URL + "?all=" + Boolean.toString(allLanguages) + "&flat=" + Boolean.toString(flattenKeys);
    }
}
