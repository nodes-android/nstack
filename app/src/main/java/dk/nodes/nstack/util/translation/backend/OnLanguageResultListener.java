package dk.nodes.nstack.util.translation.backend;

import java.util.ArrayList;

import dk.nodes.nstack.util.model.Language;

/**
 * Created by Mario on 28/12/2016.
 */

public interface OnLanguageResultListener {
    void onSuccess(ArrayList<Language> languages);

    void onFailure();
}
