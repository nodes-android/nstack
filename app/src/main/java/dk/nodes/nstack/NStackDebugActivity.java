package dk.nodes.nstack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import dk.nodes.nstack.util.appopen.versioncontrol.VersionControlDebug;

public class NStackDebugActivity extends AppCompatActivity {
    public static final String TAG = NStackDebugActivity.class.getSimpleName();

    Button launchBtn;
    ToggleButton simulateUpdateBtn;
    ToggleButton simulateForceUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        bindUI();
        setupUI();
    }

    private void bindUI() {
        launchBtn = findViewById(R.id.launch_btn);
        simulateUpdateBtn = findViewById(R.id.simulate_version_update_tb);
        simulateForceUpdateBtn = findViewById(R.id.simulate_version_force_update_tb);
    }

    private void setupUI() {
        launchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLauncherActivity(getApplicationContext());
                finish();
            }
        });
        simulateUpdateBtn.setChecked(VersionControlDebug.simulateUpdate);
        simulateUpdateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                VersionControlDebug.simulateUpdate = isChecked;
            }
        });

        simulateForceUpdateBtn.setChecked(VersionControlDebug.simulateForceUpdate);
        simulateForceUpdateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                VersionControlDebug.simulateForceUpdate = isChecked;
            }
        });
    }

    public void startLauncherActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
    }
}
