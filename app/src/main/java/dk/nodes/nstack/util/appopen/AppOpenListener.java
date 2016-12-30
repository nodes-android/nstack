package dk.nodes.nstack.util.appopen;

/**
 * Created by Mario on 29/12/2016.
 */

public interface AppOpenListener {
    void onUpdated(boolean cached);
    void onFailure();
}
