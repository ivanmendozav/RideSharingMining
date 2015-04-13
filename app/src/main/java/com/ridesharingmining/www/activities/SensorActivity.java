package com.ridesharingmining.www.activities;

import android.app.Activity;
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
import listeners.GPSListener;
import listeners.SensorListener;

/**
 * Created by Ivan on 19/03/2015.
 * Screen to Start / Stop sensor activity
 */
public class SensorActivity extends Activity{// implements SensorEventListener {
    private ContextManager contextManager;
    private boolean sensorFlag;
    private boolean justCreated;
    private GPSListener gpsListener;
    private SensorListener sensorListener;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        this.contextManager = new ContextManager(this);
        this.sensorFlag = false; //do not start until button is clicked
        this.justCreated = true;
        //instantiate sensors
        this.sensorListener = new SensorListener(this.contextManager);
        //instantiate GPS
        this.gpsListener = new GPSListener(this);
    }

    @Override
    /**
     * Resume activity
     */
    protected void onResume() {
        super.onResume();
        if (justCreated == false) { //if window was just created do not start (wait for button)
            sensorFlag = true;
            Button start_button = (Button) findViewById(R.id.start_button);
            TextView title = (TextView) findViewById(R.id.txtTitle);
            start_button.setText(R.string.stop);
            title.setText(R.string.sensing);
            this.sensorListener.start();
            this.gpsListener.start();
        }
    }


    @Override
    /**
     * Pause activity
     */
    protected void onPause() {
        super.onPause();
            sensorFlag = false;
            Button start_button = (Button) findViewById(R.id.start_button);
            TextView title = (TextView) findViewById(R.id.txtTitle);
            this.sensorListener.pause();
            this.gpsListener.pause();
            //Upload files asynchronously
            this.uploadAllFiles();
            title.setText(R.string.begining);
            start_button.setText(R.string.start);
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
            this.onPause(); //stop
        else
            this.onResume();
    }
}
