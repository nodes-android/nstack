package dk.nodes.nstackexampleproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.model.Language;
import dk.nodes.nstack.util.translation.backend.OnLanguageResultListener;
import dk.nodes.nstack.util.translation.manager.OnTranslationResultListener;

/**
 * Created by Mario on 28/12/2016.
 */
public class LanguagesActivity extends AppCompatActivity {

    @BindView(R.id.languages_rv)
    RecyclerView recyclerView;
    LanguageAdapter adapter;

    ArrayList<Language> arrayList = new ArrayList<>();

    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);
        ButterKnife.bind(this);

        dialog = ProgressDialog.show(this, "NStackExampleProject", "_Loading Languages");

        adapter = new LanguageAdapter(arrayList, new OnClickListener() {
            @Override
            public void onClick(final Language language) {
                dialog = ProgressDialog.show(LanguagesActivity.this, "NStackExampleProject", "_Changing Language to " + language.getName());
                NStack.getStack().changeLanguage(language.getLocale(), new OnTranslationResultListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        dialog.dismiss();
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        NStack.getStack().getAllLanguages(new OnLanguageResultListener() {
            @Override
            public void onSuccess(final ArrayList<Language> languages) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        arrayList.addAll(languages);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    interface OnClickListener {
        void onClick(Language language);
    }

    class LanguageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<Language> languageArrayList;
        OnClickListener onClickListener;

        LanguageAdapter(ArrayList<Language> languageArrayList, OnClickListener onClickListener) {
            this.languageArrayList = languageArrayList;
            this.onClickListener = onClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_language, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final Language language = languageArrayList.get(position);
            viewHolder.button.setText(language.getName());
        }

        @Override
        public int getItemCount() {
            return languageArrayList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public Button button;

            ViewHolder(View itemView) {
                super(itemView);
                this.button = (Button) itemView.findViewById(R.id.title_tv);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Language language = languageArrayList.get(getAdapterPosition());
                        if (language != null && onClickListener != null) {
                            onClickListener.onClick(language);
                        }
                    }
                });
            }
        }
    }
}
