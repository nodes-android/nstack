package dk.nodes.nstack;

import android.app.Dialog;
import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dk.nodes.nstack.mock.MockActivity;
import dk.nodes.nstack.mock.ResponseInterceptor;
import dk.nodes.nstack.util.appopen.AppOpenManager;
/**
 * Created by joso on 09/08/16.
 */
public class NStackTest extends ActivityInstrumentationTestCase2<MockActivity> {

    boolean initializedNStack = false;
    boolean openedApp = false;
    boolean versionControl = false;
    boolean message = false;

    public NStackTest() {
        super(MockActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test_nstackNotInitialized() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        NStack.getStack().openApp(new AppOpenManager.AppOpenCallbacks() {
            @Override
            public void onUpdated(boolean cached) {
                initializedNStack = true;
                signal.countDown();
            }

            @Override
            public void onFailure() {
                initializedNStack = true;
                signal.countDown();
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertFalse(initializedNStack);
    }

    public void test_openApp() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidTranslationResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        NStack.getStack().openApp(new AppOpenManager.AppOpenCallbacks() {
            @Override
            public void onUpdated(boolean cached) {
                openedApp = true;
                signal.countDown();
            }

            @Override
            public void onFailure() {
                openedApp = false;
                signal.countDown();
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(openedApp);
    }

    public void test_versionControl() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidVersionControlResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        NStack.getStack().getAppOpenManager().checkVersionControl(getActivity(), new AppOpenManager.VersionControlCallbacks() {
            @Override
            public void onForcedUpdate(Dialog dialog) {
                versionControl = true;
            }

            @Override
            public void onUpdate(Dialog dialog) {
                versionControl = true;
            }

            @Override
            public void onChangelog(Dialog dialog) {
                versionControl = true;
            }

            @Override
            public void onNothing() {
                versionControl = true;
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(versionControl);
    }

    public void test_messages() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidMessageShowOnceResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        NStack.getStack().getAppOpenManager().checkMessages(getActivity(), new AppOpenManager.MessagesCallbacks() {
            @Override
            public void onMessage(Dialog dialog) {
                message = true;
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(message);
    }

    public void test_messages_show_once() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidMessageShowOnceResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        NStack.getStack().getAppOpenManager().checkMessages(getActivity(), new AppOpenManager.MessagesCallbacks() {
            @Override
            public void onMessage(Dialog dialog) {
                message = true;
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(message);

        message = false;

        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidMessageShowOnceResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        //Since previous call had a show-once message the callback will never be called,
        //even if the payload still brings a message
        NStack.getStack().getAppOpenManager().checkMessages(getActivity(), new AppOpenManager.MessagesCallbacks() {
            @Override
            public void onMessage(Dialog dialog) {
                message = true;
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertFalse(message);
    }

    public void test_messages_show_always() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidMessageShowOnceResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        NStack.getStack().getAppOpenManager().checkMessages(getActivity(), new AppOpenManager.MessagesCallbacks() {
            @Override
            public void onMessage(Dialog dialog) {
                message = true;
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(message);

        message = false;

        ResponseInterceptor.mockResponse = ResponseInterceptor.mockValidMessageShowOnceResponse;
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        //Since previous call had a show-always message the callback will still be called
        NStack.getStack().getAppOpenManager().checkMessages(getActivity(), new AppOpenManager.MessagesCallbacks() {
            @Override
            public void onMessage(Dialog dialog) {
                message = true;
            }
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(message);
    }
}
