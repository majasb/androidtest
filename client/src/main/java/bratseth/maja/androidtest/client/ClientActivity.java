package bratseth.maja.androidtest.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ClientActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
        setContentView(R.layout.main);
    }

}

