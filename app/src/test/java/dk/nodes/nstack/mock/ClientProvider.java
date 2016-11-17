package dk.nodes.nstack.mock;

import okhttp3.OkHttpClient;
/**
 * Created by joso on 09/08/16.
 */
public class ClientProvider {
    public static OkHttpClient provideHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new ResponseInterceptor());
        return client;
    }
}
