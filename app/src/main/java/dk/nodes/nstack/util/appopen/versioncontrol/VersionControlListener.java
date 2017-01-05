package dk.nodes.nstack.util.appopen.versioncontrol;


import android.support.v7.app.AlertDialog;

/**
 * Created by Mario on 29/12/2016.
 */

public interface VersionControlListener {
    void onForcedUpdate(AlertDialog dialog);

    void onUpdate(AlertDialog dialog);

    void onChangelog(AlertDialog dialog);

    void onNothing();
}
