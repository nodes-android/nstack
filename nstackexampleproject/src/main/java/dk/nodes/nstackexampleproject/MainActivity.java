package dk.nodes.nstackexampleproject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.translation.Translate;
import dk.nodes.nstack.util.translation.manager.OnTranslationResultListener;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NStack.getStack().changeLanguage("es-ES", new OnTranslationResultListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.recreate();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LanguagesActivity.class));
            }
        });


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

        focusThief.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NStack.getStack().translate(this);
    }
}
