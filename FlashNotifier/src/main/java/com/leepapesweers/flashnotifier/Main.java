package com.leepapesweers.flashnotifier;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;

public class Main extends Activity {

    private boolean mServiceRunning;
    private Switch mServiceSwitch;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = this.getSharedPreferences(
                "com.leepapesweers.flashnotifier", Context.MODE_PRIVATE);

        mServiceSwitch = (Switch) findViewById(R.id.chkbox);

        mServiceRunning = isServiceRunning();

        // Check pref values
        ((CheckBox) findViewById(R.id.smsCheckBox)).setChecked(mPrefs.getBoolean("smsNotifications", false));
        ((CheckBox) findViewById(R.id.callCheckBox)).setChecked(mPrefs.getBoolean("callNotifications", false));

        updateServiceStatus();
    }

    /**
     * Toggle the service if the checkboxview is clicked
     * @param v the view parameter passed in from onClick
     */
    public void toggleService(View v) {
        Switch chkBox = (Switch) v;
        if (chkBox.isChecked()) {
            mServiceRunning = true;
            startService(new Intent(this, SMSCallListener.class));
            updateServiceStatus();
        } else {
            mServiceRunning = false;
            stopService(new Intent(this, SMSCallListener.class));
            updateServiceStatus();
        }
    }

    /**
     * Updates the contents of the switch view
     */
    public void updateServiceStatus() {
        if (mServiceRunning) {
            mServiceSwitch.setChecked(true);
            mServiceSwitch.setText("Service is running!");
        } else {
            mServiceSwitch.setChecked(false);
            mServiceSwitch.setText("Service isn't running");
        }
    }

    public void updateUserPref(View v) {
        CheckBox checkBox = (CheckBox) v;
        boolean val = checkBox.isChecked();

        if (v.getId() == R.id.callCheckBox) {
//            Toast.makeText(this, "Calls updated", Toast.LENGTH_LONG).show();
            mPrefs.edit().putBoolean("callNotifications", val).commit();
        } else {
            // Must be SMS
            mPrefs.edit().putBoolean("smsNotifications", val).commit();
        }
    }

    public void APIDialog(View v) {
        startActivity(new Intent(this, APIAccess.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Borrowed from http://stackoverflow.com/a/5921190
     * @return true or false, depending on if service is running
     */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMSCallListener.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}