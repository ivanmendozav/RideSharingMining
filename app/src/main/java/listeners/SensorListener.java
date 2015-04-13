package listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import lib.ContextManager;


/**
 * Created by Ivan on 13/04/2015.
 */
public class SensorListener implements SensorEventListener {
    private ContextManager contextManager;
    private SensorManager mSensorManager;
    private List<Sensor> sensors;

    /**
     * Constructor
     * @param  contextManager
     */
    public SensorListener(ContextManager contextManager){
        super();
        this.contextManager = contextManager;
        mSensorManager = contextManager.getSensorManager();
        sensors = contextManager.getSensors();
    }

    /**
     * Start GPS
     */
    public void start(){
        Iterator<Sensor> it = this.sensors.iterator();
        while (it.hasNext()) {
            mSensorManager.registerListener(this, it.next(), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Stop GPS
     */
    public void pause(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void logActivity(String string, int sensorID){
        try {
            File external = Environment.getExternalStorageDirectory(); //external to share to other apps later
            String sdcardPath = external.getPath();
            String filename = this.contextManager.getSensorFileName(sensorID);

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
