package dk.nodes.nstack.util.backend;

import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import dk.nodes.nstack.NStack;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
/**
 * Created by joso on 09/08/16.
 */
public class ClientProvider {

    public static OkHttpClient provideHttpClient(@Nullable Cache cache) {
        OkHttpClient.Builder client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);

        client.addInterceptor(new NStackInterceptor());

        if(NStack.getStack().isDebugMode()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.addInterceptor(logging);
        }

        if (cache != null) {
            client.cache(cache);
        }

        return client.build();
    }

}
