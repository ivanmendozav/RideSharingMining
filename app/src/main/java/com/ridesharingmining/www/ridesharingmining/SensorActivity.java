package com.ridesharingmining.www.ridesharingmining;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import app.BatteryReceiver;
import app.ContextManager;
import app.FileUploader;
import app.WifiReceiver;

/**
 * Created by Ivan on 19/03/2015.
 * Screen to Start / Stop sensor activity
 */
public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private ContextManager contextManager;
    private List<Sensor> sensors;
    private boolean sensorFlag;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        contextManager = new ContextManager(this);
        mSensorManager = contextManager.getSensorManager();
        sensors = contextManager.getSensors();
        //Get file names
        String now = Calendar.getInstance().get(Calendar.YEAR)+""+(Calendar.getInstance().get(Calendar.MONTH)+1)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    /**
     * Gather sensors information as soon as available
     */
    public final void onSensorChanged(SensorEvent event) {
        float x_acceleration=0, y_acceleration=0, z_acceleration=0, temperature=0, x_gravity=0, y_gravity=0, z_gravity=0, x_gyroscope=0, y_gyroscope=0;
        float z_gyroscope=0, light=0, x_linear=0, y_linear=0, z_linear=0, magnetic_field=0, pressure=0, proximity=0, humidity=0, x_rotation=0;
        float y_rotation=0, z_rotation=0, steps=0;
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x_acceleration = event.values[0];
            y_acceleration = event.values[1];
            z_acceleration = event.values[2];
            this.logActivity(x_acceleration+","+y_acceleration+","+z_acceleration,Sensor.TYPE_ACCELEROMETER);
        }
        if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            temperature = event.values[0];
            this.logActivity(Float.toString(temperature),Sensor.TYPE_AMBIENT_TEMPERATURE);
        }
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            x_gravity = event.values[0];
            y_gravity = event.values[1];
            z_gravity = event.values[2];
            this.logActivity(x_gravity+","+y_gravity+","+z_gravity,Sensor.TYPE_GRAVITY);
        }
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            x_gyroscope = event.values[0];
            y_gyroscope = event.values[1];
            z_gyroscope = event.values[2];
            this.logActivity(x_gyroscope+","+y_gyroscope+","+z_gyroscope,Sensor.TYPE_GYROSCOPE);
        }
        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            light = event.values[0];
            this.logActivity(Float.toString(light),Sensor.TYPE_LIGHT);
        }
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            x_linear = event.values[0];
            y_linear = event.values[1];
            z_linear = event.values[2];
            this.logActivity(x_linear+","+y_linear+","+z_linear,Sensor.TYPE_LINEAR_ACCELERATION);
        }
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetic_field = event.values[0];
            this.logActivity(Float.toString(magnetic_field),Sensor.TYPE_MAGNETIC_FIELD);
        }
        if(event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressure = event.values[0];
            this.logActivity(Float.toString(pressure),Sensor.TYPE_PRESSURE);
        }
        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proximity = event.values[0];
            this.logActivity(Float.toString(proximity),Sensor.TYPE_PROXIMITY);
        }
        if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            humidity = event.values[0];
            this.logActivity(Float.toString(humidity),Sensor.TYPE_RELATIVE_HUMIDITY);
        }
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            x_rotation = event.values[0];
            y_rotation = event.values[1];
            z_rotation = event.values[2];
            this.logActivity(x_rotation+","+y_rotation+","+z_rotation,Sensor.TYPE_ROTATION_VECTOR);
        }
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps = event.values[0];
            this.logActivity(Float.toString(steps),Sensor.TYPE_STEP_DETECTOR);
        }

        /*String data = x_acceleration+","+y_acceleration+","+z_acceleration+","+x_gravity+","+y_gravity+","+z_gravity+","+x_gyroscope+","+y_gyroscope+","+z_gyroscope+","+x_linear+","+y_linear+","+z_linear+","+x_rotation+","+y_rotation+","+z_rotation+","+humidity+","+light+","+magnetic_field+","+pressure+","+proximity+","+temperature+".\n";
        TextView textView = (TextView) findViewById(R.id.txtLog);
        textView.setText(textView.getText()+data);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorFlag = true;
        Button start_button = (Button) findViewById(R.id.start_button);
        start_button.setText("STOP");

        Iterator<Sensor> it = this.sensors.iterator();
        while(it.hasNext()){
            mSensorManager.registerListener(this, it.next(), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorFlag = false;
        Button start_button = (Button) findViewById(R.id.start_button);
        start_button.setText("START");
        //Upload files asynchronously
            this.uploadAllFiles();
        mSensorManager.unregisterListener(this);
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
        Iterator<Sensor> it = this.sensors.iterator();
        while(it.hasNext()){
            int sensorID = it.next().getType();
            String filename = contextManager.getSensorFileName(sensorID);
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
        if(sensorFlag) //if running
            this.onPause(); //stop
        else
            this.onResume();;
    }

    /**
     * Log sensor values to a file as soon as info is available
     * @param string
     * @param sensorID
     */
    private void logActivity(String string, int sensorID){
        try {
            File external = Environment.getExternalStorageDirectory(); //external to share to other apps later
            String sdcardPath = external.getPath();
            String filename = contextManager.getSensorFileName(sensorID);

            File dir = new File(sdcardPath+"/Documents");
            File file = new File(sdcardPath+"/Documents/"+filename);

            if (!dir.isDirectory())
                dir.mkdirs(); //create all necessary directories

            file.createNewFile();
            FileWriter filewriter = new FileWriter(sdcardPath+"/Documents/"+filename, true); //true for append
            BufferedWriter out = new BufferedWriter(filewriter);
            out.write(string+","+Calendar.getInstance().getTimeInMillis()+"\n"); //CSV format with line break between measures
            out.close();
            filewriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
