package dk.nodes.nstackexampleproject;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.w3c.dom.Text;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstackexampleproject.util.model.Translation;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // App open
        NStack.getStack().getAppOpenManager().openApp(this, new AppOpenManager.AppOpenCallbacks() {
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

            @Override
            public void onRateReminder(Dialog dialog) {
                Logger.d("", "dialog: " + dialog);
                dialog.show();
            }

            @Override
            public void onFailure() {
                Logger.d("", "dialog: ");
            }

            @Override
            public void translationsUpdated() {
                TextView textView = (TextView) findViewById(R.id.test_tv);
                textView.setText(Translation.testSectionJoao.testkeyjoao);
            }
        });



    }
}
