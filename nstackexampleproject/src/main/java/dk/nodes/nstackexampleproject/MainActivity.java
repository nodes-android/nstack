package dk.nodes.nstackexampleproject;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstackexampleproject.util.model.Translation;

public class MainActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.test_tv);

        textView.setText(Translation.defaultSection.ok);

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

        NStack.getStack().getAppOpenManager().checkMessages(this, new AppOpenManager.MessagesCallbacks() {
            @Override
            public void onMessage(Dialog dialog) {
                Log.d(MainActivity.class.getSimpleName(), "message received");
                dialog.show();
            }
        });

    }
}
