package dk.nodes.nstack.util.backend;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;
/**
 * Created by joso on 09/08/16.
 */
public class ClientProvider {

    public static OkHttpClient provideHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);

        client.interceptors().add(new NStackInterceptor());
        client.interceptors().add(new LoggingInterceptor());
        return client;
    }

}
