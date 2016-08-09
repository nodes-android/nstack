package dk.nodes.nstack;

import android.app.Dialog;
import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dk.nodes.nstack.mock.MockActivity;
import dk.nodes.nstack.util.appopen.AppOpenManager;
/**
 * Created by joso on 09/08/16.
 */
public class NStackTest extends ActivityInstrumentationTestCase2<MockActivity> {

    boolean initializedNStack = false;
    boolean openedApp = false;
    boolean versionControl = false;

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
            public void onUpdated() {
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
        NStack.init(getInstrumentation().getContext(), "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke");

        NStack.getStack().openApp(new AppOpenManager.AppOpenCallbacks() {
            @Override
            public void onUpdated() {
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
        });

        signal.await(1, TimeUnit.SECONDS);
        assertTrue(versionControl);
    }
}
