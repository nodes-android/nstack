package dk.nodes.nstack.util.backend;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dk.nodes.nstack.NStack;
import dk.nodes.nstack.util.log.NLog;
import dk.nodes.nstack.util.translation.TranslationManager;
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
        initClient();
    }

    protected void initClient() {
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);

        client.interceptors().add(new LoggingInterceptor());
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
                .header("X-Application-Id", NStack.getStack().getApplicationKey())
                .header("X-Rest-Api-Key", NStack.getStack().getApiKey())
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    public void getTranslation( String url, String acceptHeader, Callback callback ) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Language", acceptHeader)
                .header("X-Application-Id", NStack.getStack().getApplicationKey())
                .header("X-Rest-Api-Key", NStack.getStack().getApiKey())
                .build();

        client.newCall(request).enqueue(callback);
    }
}
