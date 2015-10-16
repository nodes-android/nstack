package dk.nodes.nstack.util.backend;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dk.nodes.nstack.util.log.NLog;
import okio.Buffer;

/**
 * Created by joso on 29/09/15.
 */
public class BackendManager {

    private static BackendManager instance;
    protected OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private BackendManager() {
        client = new OkHttpClient();
        //client = UnsafeSSLClient.getUnsafeOkHttpClient();
        initClient();
    }

    protected void initClient() {
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);

        client.interceptors().add(new LoggingInterceptor());
        client.interceptors().add(new NStackInterceptor());
    }

    public static BackendManager getInstance() {
        if (instance == null) {
            instance = new BackendManager();
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
        } catch( Exception e ) {
            NLog.e(e);
        }
    }

    public Response getTranslation( String url, String acceptHeader ) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    public void getTranslation( String url, String acceptHeader, Callback callback ) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getLanguage( Callback callback ) throws Exception {
        Request request = new Request.Builder()
                .url("https://baas.like.st/api/v1/translate/mobile/languages/best_fit")
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getContentResponse( String url, Callback callback) throws Exception {
        //example url https://nstack.io/api/v1/content/responses/0
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
