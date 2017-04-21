package dk.nodes.nstack.util.appopen.versioncontrol;


import android.support.v7.app.AlertDialog;

/**
 * Created by Mario on 29/12/2016.
 */

public interface VersionControlExListener {
    void onForcedUpdate(AlertDialog.Builder builder);

    void onUpdate(AlertDialog.Builder builder);

    void onChangelog(AlertDialog.Builder builder);

    void onNothing();
}
