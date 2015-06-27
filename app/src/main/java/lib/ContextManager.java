package lib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import lib.mining.CsvParser;
import lib.mining.ModelParameters;

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

    /**
     * Check whether it is possible to connect to server
     * @return
     */
    public static boolean haveInternetAccess(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://www.proyectomed.org");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return new Boolean(true);
                }
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Write to app log file (errors and access logs)
     * @param string
     */
    public static void writeAppLog(String string){
        try {
            File external = Environment.getExternalStorageDirectory(); //external to share to other apps later
            String sdcardPath = external.getPath();
            String filename = ParameterSettings.LogFile;
            File file = new File(sdcardPath+"/Documents/"+filename);
            File dir = new File(sdcardPath+"/Documents");

            if (!dir.isDirectory())
                dir.mkdirs(); //create all necessary directories

            file.createNewFile();
            FileWriter filewriter = new FileWriter(sdcardPath+"/Documents/"+filename, true); //true for append
            BufferedWriter out = new BufferedWriter(filewriter);
            String text = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss",Calendar.getInstance().getTime()) + " "+string+"\n";
            out.write(text); //CSV format with line break between measures
            if(ModelParameters.debug_mode)
                System.out.println(text);
            out.close();
            filewriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param parseAll whether the whole log file must be read.
     *                 Otherwise get the last m lines. m is defined in ParameterSettings
     * @return
     */
    public static String readAppLog(boolean parseAll) {
        int LastEvents = ParameterSettings.LastEvents;
        String text = ""; int from = 0;
        List<String> lines = CsvParser.ParseFile(ParameterSettings.LogFile);
        int size = lines.size();

        try {
            if(size > 0) {
                if (!parseAll) {
                    if (lines.size() >= from) from = lines.size() - (LastEvents - 1);
                } //zero-based position
                int i = from;
                while (i < lines.size()) {
                    text += lines.get(i)+"\n";
                    i++;
                }
            }
            return text;
        }catch(Exception e){
            return "";
        }
    }
}
