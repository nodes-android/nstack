package dk.nodes.nstack.mock;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
/**
 * Created by joso on 09/08/16.
 */
public class ResponseInterceptor implements Interceptor {

    public static String mockValidTranslationResponse = "{\"data\":{\"count\":7424,\"update\":{\"newer_version\":{\"state\":\"force\",\"last_id\":14,\"version\":\"1.0.1 - 2.4.1\",\"link\":\"https:\\/\\/play.google.com\\/store\\/apps\\/details?id=com.crowdit\",\"translate\":{\"title\":null,\"message\":\"\",\"positiveBtn\":null}}},\"translate\":{\"default\":{\"ok\":\"Ok\",\"loltest\":\"loltest\"},\"versionControl\":{\"updateHeader\":\"New version is out, please update\",\"forceHeader\":\"New version ios out, you have to update!\",\"negativeBtn\":\"Cancel\",\"positiveBtn\":\"Update\",\"newInVersionHeader\":\"New in this version\",\"okBtn\":\"Ok\"},\"rateReminder\":{\"title\":\"Rate the app\",\"body\":\"We can see you like the application. Would you like to rate it?\",\"yesBtn\":\"Yes\",\"laterBtn\":\"Later\",\"noBtn\":\"No\"},\"testSectionJoao\":{\"testkeyjoao\":\"test joao new\"}},\"message\":{\"id\":22,\"message\":\"all\",\"show_setting\":\"show_once\"},\"platform\":\"android\",\"created_at\":\"2016-08-09T14:35:06+0100\",\"last_updated\":\"2016-07-15T15:02:16+0100\"},\"meta\":{\"language\":{\"id\":11,\"name\":\"English (UK)\",\"locale\":\"en-GB\",\"direction\":\"LRM\",\"Accept-Language\":\"en-UK\"},\"is_cached\":true}}";
    public static String mockValidVersionControlResponse = "{\"data\":{\"count\":7424,\"update\":{\"newer_version\":{\"state\":\"force\",\"last_id\":14,\"version\":\"1.0.1 - 2.4.1\",\"link\":\"https:\\/\\/play.google.com\\/store\\/apps\\/details?id=com.crowdit\",\"translate\":{\"title\":null,\"message\":\"\",\"positiveBtn\":null}}},\"translate\":{\"default\":{\"ok\":\"Ok\",\"loltest\":\"loltest\"},\"versionControl\":{\"updateHeader\":\"New version is out, please update\",\"forceHeader\":\"New version ios out, you have to update!\",\"negativeBtn\":\"Cancel\",\"positiveBtn\":\"Update\",\"newInVersionHeader\":\"New in this version\",\"okBtn\":\"Ok\"},\"rateReminder\":{\"title\":\"Rate the app\",\"body\":\"We can see you like the application. Would you like to rate it?\",\"yesBtn\":\"Yes\",\"laterBtn\":\"Later\",\"noBtn\":\"No\"},\"testSectionJoao\":{\"testkeyjoao\":\"test joao new\"}},\"message\":{\"id\":22,\"message\":\"all\",\"show_setting\":\"show_once\"},\"platform\":\"android\",\"created_at\":\"2016-08-09T14:35:06+0100\",\"last_updated\":\"2016-07-15T15:02:16+0100\"},\"meta\":{\"language\":{\"id\":11,\"name\":\"English (UK)\",\"locale\":\"en-GB\",\"direction\":\"LRM\",\"Accept-Language\":\"en-UK\"},\"is_cached\":true}}";
    public static String mockValidMessageShowOnceResponse = "{\"data\":{\"count\":7424,\"update\":{\"newer_version\":{\"state\":\"force\",\"last_id\":14,\"version\":\"1.0.1 - 2.4.1\",\"link\":\"https:\\/\\/play.google.com\\/store\\/apps\\/details?id=com.crowdit\",\"translate\":{\"title\":null,\"message\":\"\",\"positiveBtn\":null}}},\"translate\":{\"default\":{\"ok\":\"Ok\",\"loltest\":\"loltest\"},\"versionControl\":{\"updateHeader\":\"New version is out, please update\",\"forceHeader\":\"New version ios out, you have to update!\",\"negativeBtn\":\"Cancel\",\"positiveBtn\":\"Update\",\"newInVersionHeader\":\"New in this version\",\"okBtn\":\"Ok\"},\"rateReminder\":{\"title\":\"Rate the app\",\"body\":\"We can see you like the application. Would you like to rate it?\",\"yesBtn\":\"Yes\",\"laterBtn\":\"Later\",\"noBtn\":\"No\"},\"testSectionJoao\":{\"testkeyjoao\":\"test joao new\"}},\"message\":{\"id\":22,\"message\":\"all\",\"show_setting\":\"show_once\"},\"platform\":\"android\",\"created_at\":\"2016-08-09T14:35:06+0100\",\"last_updated\":\"2016-07-15T15:02:16+0100\"},\"meta\":{\"language\":{\"id\":11,\"name\":\"English (UK)\",\"locale\":\"en-GB\",\"direction\":\"LRM\",\"Accept-Language\":\"en-UK\"},\"is_cached\":true}}";

    public static String mockResponse;

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (request.httpUrl().toString().contains("v1/open")) {
            return new Response.Builder().code(200).body(ResponseBody.create(MediaType.parse("application/json"), mockResponse)).build();
        }

        return chain.proceed(request);
    }
}
