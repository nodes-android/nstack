package dk.nodes.nstack.util.translation.backend;

/**
 * Created by Mario on 28/12/2016.
 */

public interface OnTranslationResultListener {
    void onSuccess(boolean cached);

    void onFailure();
}
