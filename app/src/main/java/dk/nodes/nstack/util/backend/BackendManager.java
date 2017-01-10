package dk.nodes.nstack.util.backend;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.appopen.AppOpenSettings;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by joso on 29/09/15.
 */
public class BackendManager {

    protected OkHttpClient client;

    public BackendManager(OkHttpClient httpClient) {
        client = httpClient;
    }

    public void updateHttpClient(OkHttpClient httpClient) {
        client = httpClient;
    }

    public void getTranslation(@NonNull final String url, @NonNull final String acceptHeader, @NonNull final Callback callback) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getAllTranslations(@NonNull final String url, @NonNull final Callback callback) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }


    public void getAllLanguages(@NonNull final Callback callback) throws Exception {
        Request request = new Request.Builder()
                .url("https://nstack.io/api/v1/translate/mobile/languages")
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getContentResponse(int id, Callback callback) throws Exception {
        //example url https://nstack.io/api/v1/content/responses/0
        Request request = new Request.Builder()
                .url("https://nstack.io/api/v1/content/responses/" + id)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getAppOpen(String url, AppOpenSettings settings, String acceptHeader, Callback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("guid", settings.guid)
                .add("version", settings.version)
                .add("old_version", settings.oldVersion)
                .add("platform", settings.platform);

        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .post(formBuilder.build())
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void viewMessage(AppOpenSettings settings, int messageId, Callback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("guid", settings.guid)
                .add("message_id", String.valueOf(messageId));

        Request request = new Request.Builder()
                .url("https://nstack.io/api/v1/notify/messages/views")
                .post(formBuilder.build())
                .build();

        client.newCall(request).enqueue(callback);
    }
}
