package dk.nodes.nstackexampleproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenListener;
import dk.nodes.nstack.util.appopen.AppOpenManager;
/**
 * Created by joso on 09/08/16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        NStack.getStack().openApp(new AppOpenListener() {
            @Override
            public void onUpdated(boolean cached) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });
    }

}
