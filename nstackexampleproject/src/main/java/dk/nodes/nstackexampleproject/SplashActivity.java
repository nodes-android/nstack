package dk.nodes.nstackexampleproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;
/**
 * Created by joso on 09/08/16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NStack.getStack().openApp(new AppOpenManager.AppOpenCallbacks() {
            @Override
            public void onUpdated() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }

            @Override
            public void onFailure() {

            }
        });
    }

}
