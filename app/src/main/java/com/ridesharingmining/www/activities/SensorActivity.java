package com.ridesharingmining.www.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import lib.ContextManager;
import lib.ParameterSettings;
import lib.db.AppSQLiteHelper;
import lib.db.PrefeDataObject;
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
        this.updateLog();
        this.startSession();
    }

    /**
     * Retrieve last user using the service
     */
    private void startSession() {
        EditText txtUser = (EditText) findViewById(R.id.txtId);
        PrefeDataObject preferences = PrefeDataObject.GetInstance(this);
        String stored_user = preferences.Get("user");
        if(stored_user == null){
            txtUser.setText(ParameterSettings.default_user);
        }else{txtUser.setText(stored_user);}
    }

    /**
     * Shows last ten events recorded in log file
     */
    private void updateLog() {
        TextView txtLog = (TextView) findViewById(R.id.txtLog);
        String text = ContextManager.readAppLog(false);
        txtLog.setText(text);
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
        //disable text box
        EditText txtId = (EditText) findViewById(R.id.txtId);
        txtId.setEnabled(false);
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
        //disable text box
        EditText txtId = (EditText) findViewById(R.id.txtId);
        txtId.setEnabled(true);
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
            EditText txtId = (EditText) findViewById(R.id.txtId);
            Intent service = new Intent(this, SensorService.class);
            service.putExtra("USERNAME",txtId.getText().toString() );
            startService(service);
            if(isMyServiceRunning()) {
                this.writeLog(R.string.service_has_started);
            }
        }
    }

    /**
     * Stop service
     */
    public void stopAsyncService() {
        if(isMyServiceRunning()) {
            Intent service = new Intent(this, SensorService.class);
            stopService(service);
            if(!isMyServiceRunning())
                this.writeLog(R.string.service_has_stopped);
        }
    }

    /**
     * Add text to log textview
     * @param resourceId
     */
    protected void writeLog(int resourceId){
        TextView txtLog = (TextView) findViewById(R.id.txtLog);
        String current_text = txtLog.getText().toString();
        ContextManager.writeAppLog(String.valueOf(getResources().getText(resourceId)));
        String text = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss",Calendar.getInstance().getTime()) + " "+String.valueOf(getResources().getText(resourceId))+"\n";
        txtLog.setText(current_text + text);
    }
}