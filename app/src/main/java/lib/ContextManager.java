package lib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ivan on 19/03/2015.
 * ACCELEROMETER 1
 * MAGNETIC FIELD 2
 * GYROSCOPE 4
 * LIGHT 5
 * PRESSURE 6
 * PROXIMITY 8
 * GRAVITY 9
 * LINEAR ACCELERATION 10
 * ROTATION VECTOR 11
 * RELATIVE HUMIDITY 12
 * AMBIENT TEMPERATURE 13
 * TYPE_STEP_DETECTOR 18
 *
 * Pseudo sensors
 * BATTERY 90
 * WIFI 91
 * GPS 92
 */
public class ContextManager {
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public ContextManager(Context c){
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
    }

    public SensorManager getSensorManager(){
        return mSensorManager;
    }

    /**
     * Retrieves list of available sensors in device (present and working)
     * @return
     */
    public List<Sensor> getSensors(){
        Sensor s;
        List<Sensor> sensors = new ArrayList<>();
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (s != null){
            //System.out.println("TYPE_ACCELEROMETER sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (s != null){
            //System.out.println("TYPE_AMBIENT_TEMPERATURE sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (s != null){
            //System.out.println("TYPE_GRAVITY fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (s != null){
            //System.out.println("TYPE_GYROSCOPE fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (s != null){
            //System.out.println("TYPE_LIGHT fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (s != null){
            //System.out.println("TYPE_LINEAR_ACCELERATION fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (s != null){
            //System.out.println("TYPE_MAGNETIC_FIELD fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (s != null){
            //System.out.println("TYPE_PRESSURE fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (s != null){
            //System.out.println("TYPE_PROXIMITY fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (s != null){
            //System.out.println("TYPE_RELATIVE_HUMIDITY fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (s != null){
            //System.out.println("TYPE_ROTATION_VECTOR fields sensor is present");
            sensors.add(s);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (s != null){
            //System.out.println("TYPE_STEP_DETECTOR fields sensor is present");
            sensors.add(s);
        }
        return sensors;
    }

    /**
     * Retrieves filename of given sensor for the current date
     * @param sensorID
     * @return
     */
    public String getSensorFileName(int sensorID){
        //String now = Calendar.getInstance().get(Calendar.YEAR)+""+(Calendar.getInstance().get(Calendar.MONTH)+1)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String filename = sensorID+"_.txt";
        return filename;
    }
}
