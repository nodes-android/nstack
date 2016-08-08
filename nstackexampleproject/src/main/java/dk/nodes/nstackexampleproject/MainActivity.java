package dk.nodes.nstackexampleproject;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.log.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NStack.getStack().getAppOpenManager().checkVersionControl(this, new AppOpenManager.VersionControlCallbacks() {
            @Override
            public void onForcedUpdate(Dialog dialog) {
                Logger.d("", "dialog: " + dialog);
                //dialog.show();
            }

            @Override
            public void onUpdate(Dialog dialog) {
                Logger.d("", "dialog: " + dialog);
                //dialog.show();
            }

            @Override
            public void onChangelog(Dialog dialog) {
                Logger.d("", "dialog: " + dialog);
                dialog.show();
            }
        });

        NStack.getStack().getAppOpenManager().checkRateReminder(this, new AppOpenManager.RateCallbacks() {
            @Override
            public void onRateReminder(Dialog dialog) {

            }
        });

    }
}
