package dk.nodes.nstackexampleproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.message.MessageListener;
import dk.nodes.nstack.util.appopen.ratereminder.RateReminderListener;
import dk.nodes.nstack.util.appopen.versioncontrol.VersionControlListener;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.translation.Translate;
import dk.nodes.nstack.util.translation.backend.OnTranslationResultListener;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    @Translate("defaultSection.ok")
    Toolbar toolbar;
    @BindView(R.id.text_view)
    @Translate("defaultSection.ok")
    TextView textView;
    @BindView(R.id.app_compat_text_view)
    @Translate("defaultSection.ok")
    AppCompatTextView appCompatTextView;
    @BindView(R.id.button)
    @Translate("defaultSection.ok")
    Button button;
    @BindView(R.id.app_compat_button)
    @Translate("defaultSection.ok")
    AppCompatButton appCompatButton;
    @BindView(R.id.switch_x)
    @Translate("defaultSection.ok")
    Switch switchX;
    @BindView(R.id.switch_compat)
    @Translate("defaultSection.ok")
    SwitchCompat switchCompat;
    @BindView(R.id.edit_text)
    @Translate("defaultSection.ok")
    EditText editText;
    @BindView(R.id.text_input_layout)
    @Translate("defaultSection.ok")
    TextInputLayout textInputLayout;
    @BindView(R.id.app_compat_edit_text)
    @Translate("defaultSection.ok")
    AppCompatEditText appCompatEditText;
    @BindView(R.id.text_input_edit_text)
    @Translate("defaultSection.ok")
    TextInputEditText textInputEditText;
    @BindView(R.id.radio_button)
    @Translate("defaultSection.ok")
    RadioButton radioButton;
    @BindView(R.id.checked_text_view)
    @Translate("defaultSection.ok")
    CheckedTextView checkedTextView;
    @BindView(R.id.check_box)
    @Translate("defaultSection.ok")
    CheckBox checkBox;

    @BindView(R.id.focus_thief)
    View focusThief;
    @BindView(R.id.change_btn)
    Button changeBtn;
    @BindView(R.id.list_btn)
    Button listBtn;
    @BindView(R.id.clear_btn)
    Button clearBtn;

    Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        NStack.getStack().getAppOpenManager().checkVersionControl(this, new VersionControlListener() {
            @Override
            public void onForcedUpdate(AlertDialog dialog) {
                Logger.d("", "dialog: " + dialog);
                //dialog.show();
            }

            @Override
            public void onUpdate(AlertDialog dialog) {
                Logger.d("", "dialog: " + dialog);
                //dialog.show();
            }

            @Override
            public void onChangelog(AlertDialog dialog) {
                Logger.d("", "dialog: " + dialog);
                dialog.show();
            }

            @Override
            public void onNothing() {

            }
        });

        NStack.getStack().getAppOpenManager().checkRateReminder(this, new RateReminderListener() {
            @Override
            public void onRateReminder(AlertDialog dialog) {

            }
        });

        NStack.getStack().getAppOpenManager().checkMessages(this, new MessageListener() {
            @Override
            public void onMessage(AlertDialog dialog) {
                Log.d(MainActivity.class.getSimpleName(), "message received");
                dialog.show();
            }
        });

        focusThief.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NStack.getStack().translate(this);
    }

    @OnClick({R.id.change_btn, R.id.list_btn, R.id.clear_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_btn:
                changeLanguage();
                break;
            case R.id.list_btn:
                startActivity(new Intent(MainActivity.this, LanguagesActivity.class));
                break;
            case R.id.clear_btn:
                NStack.getStack().clearLastUpdated();
                makeToast("_Last Updated Cleared");
                break;
        }
    }

    public void changeLanguage(){
        NStack.getStack().changeLanguage("es-ES", new OnTranslationResultListener() {
            @Override
            public void onSuccess(boolean cached) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.recreate();
                        makeToast("_Changed Language Successfully");

                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        makeToast("_Error");
                    }
                });
            }
        });
    }

    public void makeToast(String message){
        if (toast!= null){
            toast.cancel();
        }
        toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
