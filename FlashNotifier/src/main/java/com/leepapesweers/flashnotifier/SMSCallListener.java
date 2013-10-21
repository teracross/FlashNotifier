package com.leepapesweers.flashnotifier;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SMSCallListener extends Service {

    private Camera mCamera;
    private boolean mLightOn;
    private boolean mFlashing;
    private SharedPreferences mPrefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        Toast.makeText(getApplicationContext(), "Started service", Toast.LENGTH_LONG).show();
        Log.i("SERVICE", "Service started");

        mPrefs = this.getSharedPreferences(
                "com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);

        mLightOn = false;
        mFlashing = false;

        // Register SMS listener
        IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSListener, smsFilter);

        // Register call listener
        IntentFilter callFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(mCallListener, callFilter);

        return Service.START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
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

    /**
     * Task for passing in custom lists, probably what we should just use for predefined flash
     * patterns too. Pass in a list.
     */
    public class CustomFlashTask extends AsyncTask<List<Integer>, Void, Void> {

        @Override
        protected void onPreExecute(){
            mFlashing = true;
        }

        @Override
        protected Void doInBackground(List<Integer>... params) {
            List<Integer> list = params[0];

            // Make sure it goes off at the end and isn't unreasonable
            if (list.size() % 2 != 0 || list.size() > 10) {
                Log.e("SERVICE", "Invalid flash parameters");
                return null;
            }

            // Flash on evens (0, 2, 4...)
            boolean flash = true;

            // Iterate over on/off durations.
            for (Integer i : list) {
                if (flash) {
                    flashOn();
                    flash = false;
                } else {
                    flashOff();
                    flash = true;
                }

                // Run for as long as interval passed
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                    Log.e("FLASH", e.getMessage());
                    flashOff();
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
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
        mLightOn = true;
    }

    private void flashOff() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
        mCamera.release();
        mLightOn = false;
    }

    private BroadcastReceiver mSMSListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPrefs.getBoolean("smsNotifications", false)) {
                List<Integer> flashes = Arrays.asList(200, 100, 200, 100);
                new CustomFlashTask().execute(flashes);
            }
        }
    };

    private BroadcastReceiver mCallListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPrefs.getBoolean("callNotifications", false)) {
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

        }
    };

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Started stopped", Toast.LENGTH_LONG).show();
        Log.i("SERVICE", "Service stopped");
        unregisterReceiver(mCallListener);
        unregisterReceiver(mSMSListener);
    }
}
