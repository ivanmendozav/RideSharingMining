package lib;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import listeners.BatteryReceiver;
import listeners.GPSListener;
import listeners.WifiReceiver;

/**
 * Created by Ivan on 01/04/2015.
 * Procedures to upload sensor files (of current date) to a web server
 */
public class FileUploader extends AsyncTask<String, Void, String> {
    protected String WEB_SERVER_URL = ParameterSettings.ServerUrl;
    protected List<String> files = new ArrayList<>(); //all file names
    protected List<Integer> IDs = new ArrayList<>(); //all sensor ids
    protected UploaderListener listener = null;
    private String username = "anonymous";

    @Override
        // Asynchronous task in background to upload files
        protected String doInBackground(String[] params) {
            try{
                if(this.files.size()>0)
                    System.out.println("preparing "+this.files.size()+" files to upload.");
                //Upload each registered file
                Iterator<String> it = this.files.iterator();
                int i =0;
                while(it.hasNext()){
                    this.UploadByPost(it.next(), (int) this.IDs.get(i));
                    i++;
                }
            }catch(Exception e){
                System.out.println(e.toString());
            }
            return null;
        }

    /**
     * Register a new filename to upload later
     * @param filename
     */
    protected void registerFile(String filename){
        this.files.add(filename);
    }
    public void registerListener(UploaderListener listener){
        this.listener = listener;
    }
    protected void registerSensorID(int sensorID){
        this.IDs.add(sensorID);
    }
    /**
     * To upload a file via POST method to a Web server (by creating a form input file)
     * @param filename
     * @throws IOException
     */
    protected void UploadByPost(String filename, int sensor_id) throws IOException {
        String selectedPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/"+filename;
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null; //to write bytes to the server
        DataInputStream inputStream = null;  //to read bytes from the file
        InputStream is; //to read response from server

        String urlServer = WEB_SERVER_URL;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String response_text = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            // Connect to web server
            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);

            //Send initial headers to server
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"sensor_id\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(String.valueOf(sensor_id));
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"username\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(String.valueOf(this.username));
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + selectedPath + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            //Read from file
            FileInputStream fileInputStream = new FileInputStream(new File(selectedPath));
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize]; //empty buffer

            bytesRead = fileInputStream.read(buffer, 0, bufferSize); //read initial bytes
            //keep reading and writing
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                //in last bytes buffer size can be smaller
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            //Send final headers to server
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens  + lineEnd);

            //Read response (look for errors in file reception)
            int serverResponseCode = connection.getResponseCode();
            //Read response (look for errors in file processing)
            if (serverResponseCode == connection.HTTP_OK) {
                is = connection.getInputStream();
                int ch;
                StringBuffer sb = new StringBuffer();
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                }
                response_text = sb.toString();
                ContextManager.writeAppLog(response_text);
                if(this.listener != null)
                    this.listener.OnResponse(response_text);
                is.close();
            }
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            //Delete file
            File my_file = new File(selectedPath);
            my_file.delete();


        } catch (Exception ex) {
            ContextManager.writeAppLog("Error uploading file: "+filename );
            //System.out.println("Error uploading file: "+filename);
        }
    }

    public void setUserId(String username) {
        this.username = username;
    }

    /**
     * Uploads one single file to the cloud
     * @param username account identifier for clouding services
     * @param filename file to upload (will replace old records)
     */
    public void uploadFile(String username, String filename, int sensorID){
        this.username = username;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",filename);
        if (file.exists()) {
            this.registerFile(filename);
            this.registerSensorID(sensorID);
            this.execute();
        }
    }

    public void uploadAllFiles(String username){
        this.username = username;
        String Batteryfilename = BatteryReceiver.getFileName();
        String Wififilename = WifiReceiver.getFileName();
        String locationfilename = GPSListener.getFileName();

        //Register Battery file to uploading task
        File Batfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",Batteryfilename);
        if (Batfile.exists()) {
            this.registerFile(Batteryfilename);
            this.registerSensorID(BatteryReceiver.getType());
        }

        //Register Wifi file to uploading task
        File Wififile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",Wififilename);
        if (Wififile.exists()) {
            this.registerFile(Wififilename); //Wifi activity
            this.registerSensorID(WifiReceiver.getType());
        }

        //Register GPS updates
        File GPSfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",locationfilename);
        if (GPSfile.exists()) {
            this.registerFile(locationfilename);
            this.registerSensorID(GPSListener.getType());
        }

        //Register sensor files
//        Iterator<Sensor> it = contextManager.getSensors().iterator();
//        while(it.hasNext()){
//            int sensorID = it.next().getType();
//            String filename = contextManager.getSensorFileName(sensorID);
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",filename);
//            if (file.exists()) {
//                this.registerFile(filename); //Battery
//                this.registerSensorID(sensorID);
//            }
//        }
        //upload all existing files (and overwrite)
        if(this.files.size() > 0) {
           this.execute();
        }
    }
}
