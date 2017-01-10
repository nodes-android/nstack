package dk.nodes.nstack.util.backend;

import java.io.IOException;

import dk.nodes.nstack.NStack;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by joso on 08/10/15.
 */
public class NStackInterceptor implements Interceptor {

    public NStackInterceptor() {


    }

    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request newRequest = originalRequest.newBuilder()
                //Commented this out because it was causing issues with the cached languageHeader
                //.header("Accept-Language", NStack.getStack().getSelectedLanguageHeader())
                .header("X-Application-Id", NStack.getStack().getApplicationId())
                .header("X-Rest-Api-Key", NStack.getStack().getRestApiKey())
                .build();

        return chain.proceed(newRequest);
    }

}
