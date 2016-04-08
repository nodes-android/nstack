package dk.nodes.nstack.util.content;

import android.content.Context;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import dk.nodes.nstack.util.backend.BackendManager;
import dk.nodes.nstack.util.log.NLog;

/**
 * Created by Cesit on 16/10/15.
 */
public class ContentManager {

    //Using "baas.like.st" because of SSL certificate error when using "nstack.io"
    private static final String BASE_URL = "https://nstack.io/api/v1/content/responses/";
    private static final String BASE_FILENAME = "content-responses-";
    private static final String BASE_FILE_TYPE = ".dat";

    //Had to use context to write to filesystem.
    private final Context context;

    public ContentManager(Context context) {
        this.context = context;
    }

    /**
     * Generates filename based on content response id.
     * @param id Content response id from nStack
     * @return Local filename for content response.
     */
    private String getContentResponseFilenameById(int id) {
        return BASE_FILENAME+id+BASE_FILE_TYPE;
    }

    /**
     * Enqueue a single update request for content response.
     * @param id Content response id from nStack
     */
    public void updateContentResponseSilentlyById(final int id) {
        try {
            BackendManager.getInstance().getContentResponse(BASE_URL + id, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    NLog.e("onFailure");
                    if(request != null) {
                        NLog.e(request.method());
                        NLog.e(request.urlString());
                        NLog.e(request.headers().toString());
                    }
                    if(e != null) {
                        NLog.e(e);
                    }
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    ContentManager.this.saveContentResponseToCache(id, response.body().byteStream());
                }
            });
        } catch( Exception e ) {
            NLog.e(e);
        }
    }

    /**
     * Enqueue update requests for array of content response ids.
     * @param ids Content response ids from nStack
     */
    public void updateAllContentResponsesSilentlyByIdArray(int... ids) {
        for(int id : ids) {
            updateContentResponseSilentlyById(id);
        }
    }

    /**
     * Try to load content response as String from filesystem cache.
     * @param id Content response id from nStack
     * @return String value of content response or null if response was not found in cache.
     */
    public String loadContentResponseFromCacheById(int id) {
        String filename = getContentResponseFilenameById(id);
        BufferedReader bufferedReader = null;

        String contentResponse = null;

        try {

            bufferedReader = new BufferedReader(
                    new InputStreamReader(context.openFileInput(filename), "UTF-8"));

            String readLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((readLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(readLine);
            }
            contentResponse = stringBuilder.toString();

        } catch (Exception e) {
            NLog.e(e);
            contentResponse = null;
        }
        finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    NLog.e(e);
                }
            }
        }

        return contentResponse;
    }

    /**
     * Stream content response body into file in internal directory for app.
     * @param contentResponseId Content response id from nStack
     * @param bodyByteStream Body stream from HTTP request
     */
    private void saveContentResponseToCache(int contentResponseId, InputStream bodyByteStream) {

        String filename = getContentResponseFilenameById(contentResponseId);
        FileOutputStream fileOutputStream = null;

        try {

            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            copyStream(bodyByteStream, fileOutputStream);

        }
        catch (Exception e) {
            NLog.e(e);
        }
        finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    NLog.e(e);
                }
            }
        }
    }

    //TODO: THIS SHOULD NOT BE HERE! MAYBE USE LIBRARY METHOD FROM SOMEWHERE?!
    private static void copyStream(InputStream input, OutputStream output)
            throws IOException
    {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }
}
