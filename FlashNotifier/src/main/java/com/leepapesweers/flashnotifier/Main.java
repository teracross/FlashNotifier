package com.leepapesweers.flashnotifier;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Main extends Activity {

    private Camera mCamera;
    private boolean mLightOn;
    private boolean mHasCameraFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!mHasCameraFlash) {
            Toast.makeText(this, "No camera to open", Toast.LENGTH_LONG).show();
            return;
        }

        mCamera = Camera.open();
        mLightOn = false;
    }

    /**
     * Test method for the light, uses the button
     * @param v view that's passed in, in this case it's the button
     */
    public void toggleLight(View v) {
        if (!mHasCameraFlash) {
            Toast.makeText(this, "No camera to open", Toast.LENGTH_LONG).show();
            return;
        }

        if (mLightOn) {
            // Toggle off
            flashOff();
        } else {
            // Toggle on
            flashOn();
        }
    }

    public void flash(View v) {
        if (!mHasCameraFlash) {
            Toast.makeText(this, "No camera to open", Toast.LENGTH_LONG).show();
            return;
        }

        new FlashTask().execute();
    }

    /**
     * Done in a task because can't use sleep() on main thread
     */
    public class FlashTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (!mHasCameraFlash) {
                Toast.makeText(getBaseContext(), "No camera to open", Toast.LENGTH_LONG).show();
                return null;
            }

            for (int i = 0; i < 2; ++i) {
                flashOn();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("FLASH", e.getMessage());
                    flashOff();     // Turn it off if something went wrong
                }
                flashOff();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e("FLASH", e.getMessage());
                }
            }
            return null;
        }
    }

    private void flashOn() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
        mLightOn = true;
    }

    private void flashOff() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
        mLightOn = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
