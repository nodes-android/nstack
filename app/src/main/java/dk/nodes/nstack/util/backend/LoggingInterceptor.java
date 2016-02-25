package dk.nodes.nstack.util.backend;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import okio.Buffer;

/**
 * Created by joso on 02/10/2015.
 */
public class LoggingInterceptor implements Interceptor {
    private final String TAG = LoggingInterceptor.class.getSimpleName();
    private boolean logResponseBody = true;

    public LoggingInterceptor() {
    }

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        boolean requestHasBody = false;
        if(request.body() != null) {
            requestHasBody = true;
        }

        String body = "";
        if(requestHasBody) {
            try {
                Request requestLine = request.newBuilder().build();
                Buffer response = new Buffer();
                requestLine.body().writeTo(response);
                body = response.readUtf8();
            } catch (IOException var15) {
                body = "Body could not be converted to text.";
            }
        } else {
            body = "No body.";
        }

        String requestLine1 = request.method() + " " + request.urlString();
        Log.i(this.TAG, String.format("Sending request %s\n%s\n%s", new Object[]{requestLine1, request.headers(), body}));
        Response response1 = chain.proceed(request);
        body = "No body.";
        boolean responseHasBody = false;
        if(response1.body() != null) {
            responseHasBody = true;
        }

        MediaType contentType = null;
        if(responseHasBody) {
            contentType = response1.body().contentType();
            if(contentType != null && !contentType.toString().contains("application/xml")) {
                responseHasBody = false;
            }
        }

        if(this.logResponseBody && responseHasBody) {
            body = response1.body().string();
        }

        long t2 = System.nanoTime();
        String responseLine = response1.code() + " " + response1.message();
        Log.i(this.TAG, String.format("Received response %s for %s in %.1fms%n%s\n%s", new Object[]{responseLine, response1.request().url(), Double.valueOf((double)(t2 - t1) / 1000000.0D), response1.headers(), body}));
        if(this.logResponseBody && responseHasBody) {
            ResponseBody responseBody = ResponseBody.create(contentType, body);
            return response1.newBuilder().body(responseBody).build();
        } else {
            return response1;
        }
    }
}
