package dk.nodes.nstack.util.backend;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import dk.nodes.nstack.NStack;
import okio.Buffer;

/**
 * Created by joso on 08/10/15.
 */
public class NStackInterceptor implements Interceptor {

    public NStackInterceptor() {


    }

    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request newRequest = originalRequest.newBuilder().header("Accept-Language", NStack.getStack().getTranslationManager().options().getLanguageHeader()).build();

        return chain.proceed(newRequest);
    }

}
