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

import java.util.Locale;

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
    @Translate("versionControl.positiveBtn")
    Toolbar toolbar;
    @BindView(R.id.text_view)
    @Translate("versionControl.positiveBtn")
    TextView textView;
    @BindView(R.id.app_compat_text_view)
    @Translate("versionControl.positiveBtn")
    AppCompatTextView appCompatTextView;
    @BindView(R.id.button)
    @Translate("versionControl.positiveBtn")
    Button button;
    @BindView(R.id.app_compat_button)
    @Translate("versionControl.positiveBtn")
    AppCompatButton appCompatButton;
    @BindView(R.id.switch_x)
    @Translate("versionControl.positiveBtn")
    Switch switchX;
    @BindView(R.id.switch_compat)
    @Translate("versionControl.positiveBtn")
    SwitchCompat switchCompat;
    @BindView(R.id.edit_text)
    @Translate("versionControl.positiveBtn")
    EditText editText;
    @BindView(R.id.text_input_layout)
    @Translate("versionControl.positiveBtn")
    TextInputLayout textInputLayout;
    @BindView(R.id.app_compat_edit_text)
    @Translate("versionControl.positiveBtn")
    AppCompatEditText appCompatEditText;
    @BindView(R.id.text_input_edit_text)
    @Translate("versionControl.positiveBtn")
    TextInputEditText textInputEditText;
    @BindView(R.id.radio_button)
    @Translate("versionControl.positiveBtn")
    RadioButton radioButton;
    @BindView(R.id.checked_text_view)
    @Translate("versionControl.positiveBtn")
    CheckedTextView checkedTextView;
    @BindView(R.id.check_box)
    @Translate("versionControl.positiveBtn")
    CheckBox checkBox;

    @BindView(R.id.focus_thief)
    View focusThief;
    @BindView(R.id.current_language_btn)
    Button currentLanguageBtn;
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
        Locale locale = NStack.getStack().getSelectedLanguageLocale();
        if (locale != null) {
            currentLanguageBtn.setText(locale.getDisplayLanguage());
        }
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

    public void changeLanguage() {
        NStack.getStack().updateTranlations("es-ES", new OnTranslationResultListener() {
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

    public void makeToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
