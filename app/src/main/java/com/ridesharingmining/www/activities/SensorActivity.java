package com.ridesharingmining.www.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import lib.ContextManager;
import services.SensorService;

/**
 * Created by Ivan on 19/03/2015.
 * Screen to Start / Stop sensor activity
 */
public class SensorActivity extends Activity{// implements SensorEventListener {
    private ContextManager contextManager;
    private boolean sensorFlag;

    @Override
    /**
     * Prepare for first run (or coming from a notification)
     */
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        this.contextManager = new ContextManager(this);
        this.sensorFlag = false; //do not start until button is clicked
        if(isMyServiceRunning()) { //if coming from a notification
            this.onClickStart(true);
        }
    }

    @Override
    /**
     * Resume activity
     */
    protected void onResume() {
        super.onResume();
    }

    @Override
    /**
     * Pause activity
     */
    protected void onPause() {
        super.onPause();
    }

    /**
     * Change UI according to nwe state
     * @param running
     */
    protected void onClickStart(boolean running){
        sensorFlag = true;
        //start service
        if(running == false) {
            this.startAsyncService();
        }
        //Update UI
        Button start_button = (Button) findViewById(R.id.start_button);
        TextView title = (TextView) findViewById(R.id.txtTitle);
        start_button.setText(R.string.stop);
        title.setText(R.string.sensing);
    }

    /**
     * Change UI according to nwe state
     */
    protected void onClickStop(){
        super.onPause();
        sensorFlag = false;
        //stop service
        this.stopAsyncService();
        //Update UI
        Button start_button = (Button) findViewById(R.id.start_button);
        TextView title = (TextView) findViewById(R.id.txtTitle);
        start_button.setText(R.string.start);
        title.setText(R.string.beginning);
    }

    /**
     * Change between Start and Stop
     * @param view
     */
    public void onToggle(View view){
        if(sensorFlag) //if running
            this.onClickStop(); //stop
        else
            this.onClickStart(false);
    }

    /**
     * Check whether the service is running
     * @return
     */
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SensorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start the service
     */
    public void startAsyncService() {
        if(!isMyServiceRunning()) {
            Intent service = new Intent(this, SensorService.class);
            startService(service);
        }
    }

    /**
     * Stop service
     */
    public void stopAsyncService() {
        if(isMyServiceRunning()) {
            Intent service = new Intent(this, SensorService.class);
            stopService(service);
        }
    }
}