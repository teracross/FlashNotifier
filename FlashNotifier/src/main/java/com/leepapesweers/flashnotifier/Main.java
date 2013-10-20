package com.leepapesweers.flashnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Main extends Activity {

    private Camera mCamera;
    private boolean mLightOn;
    private boolean mHasCameraFlash;
    private boolean mFlashing;

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
        mFlashing = false;

        // Register SMS listener
        IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSListener, smsFilter);

        // Register call listener
        IntentFilter callFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(mCallListener, callFilter);
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

        if (!mLightOn && !mFlashing) {
            new FlashTask().execute();
        }
    }

    /**
     * Done in a task because can't use sleep() on main thread
     */
    public class FlashTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute(){
            mFlashing = true;
        }

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

        @Override
        protected void onPostExecute(Void v) {
            mFlashing = false;
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

    private BroadcastReceiver mSMSListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            new FlashTask().execute();
        }
    };

    private BroadcastReceiver mCallListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Ringing
                new FlashTask().execute();
            }

            if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Detect call answered
            }

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // Detect end of call, probably don't need this
            }
        }
    };
}
