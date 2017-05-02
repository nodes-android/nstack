package dk.nodes.nstack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import dk.nodes.nstack.util.appopen.versioncontrol.VersionControlDebug;
import dk.nodes.nstack.util.log.Logger;

public class NStackDebugActivity extends AppCompatActivity {
    public static final String TAG = NStackDebugActivity.class.getSimpleName();

    Button launchBtn;
    ToggleButton simulateUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        bindUI();
        setupUI();
    }

    private void bindUI()
    {
        launchBtn = (Button) findViewById(R.id.launch_btn);
        simulateUpdateBtn = (ToggleButton) findViewById(R.id.simulate_version_update_tb);
    }

    private void setupUI()
    {
        launchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.e(TAG, "Launching App");
                startLauncherActivity(getApplicationContext());
                finish();
            }
        });
        Logger.e(TAG, "VersionControlDebug.simulateUpdate = " + VersionControlDebug.simulateUpdate);
        simulateUpdateBtn.setChecked(VersionControlDebug.simulateUpdate);
        simulateUpdateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                VersionControlDebug.simulateUpdate = isChecked;
            }
        });
    }

    public void startLauncherActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
    }
}
