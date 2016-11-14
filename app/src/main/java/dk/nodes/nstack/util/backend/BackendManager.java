package dk.nodes.nstack.util.backend;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dk.nodes.nstack.util.appopen.AppOpenSettings;
import dk.nodes.nstack.util.log.Logger;
import okio.Buffer;

/**
 * Created by joso on 29/09/15.
 */
public class BackendManager {

    private static BackendManager instance;
    protected OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private BackendManager(OkHttpClient httpClient) {
        client = httpClient;
    }

    public static BackendManager getInstance() {
        if (instance == null) {
            instance = new BackendManager(ClientProvider.provideHttpClient());
        }

        return instance;
    }

    private static String bodyToString(final Request request) {

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    public void initCache(Context context) {
        try {
            File cacheDirectory = context.getCacheDir();

            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(cacheDirectory, cacheSize);

            client.setCache(cache);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public Response getTranslation(String url, String acceptHeader) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    public void getTranslation(String url, String acceptHeader, Callback callback) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getLanguage(Callback callback) throws Exception {
        Request request = new Request.Builder()
                .url("https://nstack.io/api/v1/translate/mobile/languages/best_fit")
                .build();

        client.newCall(request).enqueue(callback);
    }


    public void getAllLanguages(Callback callback) throws Exception {
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
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
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
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"last_updated\""),
                        RequestBody.create(null, settings.lastUpdatedString != null ? settings.lastUpdatedString : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format( new Date(0))))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void viewMessage(AppOpenSettings settings, int messageId, Callback callback) {
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
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
