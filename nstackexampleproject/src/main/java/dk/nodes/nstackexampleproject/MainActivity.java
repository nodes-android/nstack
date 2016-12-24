package dk.nodes.nstackexampleproject;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenManager;
import dk.nodes.nstack.util.log.Logger;
import dk.nodes.nstack.util.translation.Translate;
import dk.nodes.nstack.util.translation.TranslationManager;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textview)
    @Translate("defaultSection.ok")
    TextView textview;
    @BindView(R.id.button)
    @Translate("defaultSection.ok")
    Button button;
    @BindView(R.id.switch_x)
    @Translate("defaultSection.ok")
    Switch switchX;
    @BindView(R.id.switch_compat)
    @Translate("defaultSection.ok")
    SwitchCompat switchCompat;
    @BindView(R.id.edittext)
    @Translate("defaultSection.ok")
    EditText edittext;
    @BindView(R.id.textinputlayout)
    @Translate("defaultSection.ok")
    TextInputLayout textinputlayout;
    @BindView(R.id.tooglebutton)
    @Translate("defaultSection.ok")
    ToggleButton tooglebutton;
    @BindView(R.id.toolbar)
    @Translate("defaultSection.ok")
    Toolbar toolbar;
    @BindView(R.id.radiobutton)
    @Translate("defaultSection.ok")
    RadioButton radiobutton;
    @BindView(R.id.checkedtextview)
    @Translate("defaultSection.ok")
    CheckedTextView checkedtextview;
    @BindView(R.id.checkbox)
    @Translate("defaultSection.ok")
    CheckBox checkbox;
    @BindView(R.id.textinputedittext)
    @Translate("defaultSection.ok")
    TextInputEditText textinputedittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        NStack.getStack().translate(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NStack.getStack().changeLanguage("es-ES", new TranslationManager.OnTranslationResultListener() {
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
