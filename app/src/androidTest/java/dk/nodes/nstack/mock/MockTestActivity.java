package dk.nodes.nstack.mock;

import android.app.Activity;
import android.os.Bundle;

import dk.nodes.nstack.R;
/**
 * Created by joso on 10/08/16.
 */
public class MockTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
