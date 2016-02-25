package dk.nodes.nstackexampleproject;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // App open
        NStack.getStack().getAppOpenManager().openApp(this, new AppOpenManager.AppOpenCallbacks() {
            @Override
            public void onForcedUpdate(Dialog dialog) {
                Log.d("", "dialog: " + dialog);
                dialog.show();
            }

            @Override
            public void onUpdate(Dialog dialog) {
                Log.d("", "dialog: " + dialog);
                dialog.show();
            }

            @Override
            public void onChangelog(Dialog dialog) {
                Log.d("", "dialog: " + dialog);
                dialog.show();
            }

            @Override
            public void onFailure() {
                Log.d("", "dialog: ");
            }
        });
    }
}
