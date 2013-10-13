package com.leepapesweers.flashnotifier;

import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class Main extends Activity {

    private Camera mCamera;
    private boolean mLightOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = Camera.open();
        mLightOn = false;
    }

    public void toggleLight(View v) {
        Camera.Parameters parameters = mCamera.getParameters();
        if (mLightOn) {
            // Toggle off
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mLightOn = false;
        } else {
            // Toggle on
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mLightOn = true;
        }
        mCamera.setParameters(parameters);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
