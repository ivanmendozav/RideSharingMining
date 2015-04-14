package com.ridesharingmining.www.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import listeners.BatteryReceiver;
import lib.ContextManager;
import lib.FileUploader;
import listeners.WifiReceiver;
import services.SensorService;

/**
 * Created by Ivan on 19/03/2015.
 * Screen to Start / Stop sensor activity
 */
public class SensorActivity extends Activity{// implements SensorEventListener {
    private ContextManager contextManager;
    private boolean sensorFlag;
    private boolean justCreated;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        this.contextManager = new ContextManager(this);
        this.sensorFlag = false; //do not start until button is clicked

        //if coming from a notification
        if(isMyServiceRunning()) {
            this.justCreated = false;
            this.onClickStart(true);
        }else
            this.justCreated = true;
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
        if (justCreated == false) { //if window was just created do not start (wait for button)
            sensorFlag = true;
            if(running == false) {
                this.startAsyncService();
            }
            Button start_button = (Button) findViewById(R.id.start_button);
            TextView title = (TextView) findViewById(R.id.txtTitle);
            start_button.setText(R.string.stop);
            title.setText(R.string.sensing);
        }
    }

    /**
     * Change UI according to nwe state
     */
    protected void onClickStop(){
        super.onPause();
        sensorFlag = false;
        //Upload files asynchronously
        this.stopAsyncService();
        this.uploadAllFiles();
        Button start_button = (Button) findViewById(R.id.start_button);
        TextView title = (TextView) findViewById(R.id.txtTitle);
        start_button.setText(R.string.start);
        title.setText(R.string.beginning);
    }

    /*
    Creates asynchronous task to upload all existing sensor files
     */
    protected void uploadAllFiles(){
        String Batteryfilename = BatteryReceiver.getFileName();
        String Wififilename = WifiReceiver.getFileName();
        String now = Calendar.getInstance().get(Calendar.YEAR)+""+(Calendar.getInstance().get(Calendar.MONTH)+1)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String locationfilename = "location_"+now+".txt";

        int user_id = 12;
        FileUploader thread1 = new FileUploader();
        thread1.setUserId(user_id);

        //Register Battery file to uploading task
        File Batfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",Batteryfilename);
        if (Batfile.exists()) {
            thread1.registerFile(Batteryfilename);
            thread1.registerSensorID(BatteryReceiver.getType());
        }

        //Register Wifi file to uploading task
        File Wififile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",Wififilename);
        if (Wififile.exists()) {
            thread1.registerFile(Wififilename); //Wifi activity
            thread1.registerSensorID(WifiReceiver.getType());
        }

        //Register GPS updates
        File GPSfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",locationfilename);
        if (GPSfile.exists()) {
            thread1.registerFile(locationfilename);
            thread1.registerSensorID(92);
        }

        //Register sensor files
        Iterator<Sensor> it = this.contextManager.getSensors().iterator();
        while(it.hasNext()){
            int sensorID = it.next().getType();
            String filename = this.contextManager.getSensorFileName(sensorID);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",filename);
            if (file.exists()) {
                thread1.registerFile(filename); //Battery
                thread1.registerSensorID(sensorID);
            }
        }
        //upload all existing files (and overwrite)
        thread1.execute();
    }

    /**
     * Change between Start and Stop
     * @param view
     */
    public void onToggle(View view){
        justCreated = false;
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

    public void stopAsyncService() {
        if(isMyServiceRunning()) {
            Intent service = new Intent(this, SensorService.class);
            stopService(service);
        }
    }
}
