package dk.nodes.nstack.util.backend;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import dk.nodes.nstack.util.appopen.AppOpenSettings;
import okhttp3.Callback;
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

    //Should we use this?
    public void getLanguage(@NonNull final Callback callback) throws Exception {
        Request request = new Request.Builder()
                .url("https://nstack.io/api/v1/translate/mobile/languages/best_fit")
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
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"guid\""),
                        RequestBody.create(null, settings.guid))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"version\""),
                        RequestBody.create(null, settings.version))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"old_version\""),
                        RequestBody.create(null, settings.oldVersion))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"platform\""),
                        RequestBody.create(null, settings.platform))
//                .addPart(
//                        Headers.of("Content-Disposition", "form-data; name=\"last_updated\""),
//                        RequestBody.create(null, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(0))))
//        RequestBody.create(null, settings.lastUpdatedString != null ? settings.lastUpdatedString : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(0))))
        .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void viewMessage(AppOpenSettings settings, int messageId, Callback callback) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"guid\""),
                        RequestBody.create(null, settings.guid))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"message_id\""),
                        RequestBody.create(null, String.valueOf(messageId)))
                .build();

        Request request = new Request.Builder()
                .url("https://nstack.io/api/v1/notify/messages/views")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
