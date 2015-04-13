package lib;

import android.os.AsyncTask;
import android.os.Environment;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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

/**
 * Created by Ivan on 01/04/2015.
 * Procedures to upload sensor files (of current date) to a web server
 */
public class FileUploader extends AsyncTask<String, Void, String> {
    protected String WEB_SERVER_URL = "http://10.0.2.2:88/ridesharingmining/receive.php";
    protected List<String> files = new ArrayList<>(); //all file names
    protected List<Integer> IDs = new ArrayList<>(); //all sensor ids
    private int userId;

    @Override
        // Asynchronous task in background to upload files
        protected String doInBackground(String[] params) {
            try{
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
    public void registerFile(String filename){
        this.files.add(filename);
    }

    public void registerSensorID(int sensorID){
        this.IDs.add(sensorID);
    }
    /**
     * @deprecated
     * Upload a single file in binary format (not suitable for PHP server-side scripts)
     * @param filename
     */
    protected void Upload(String filename) {
        String url = WEB_SERVER_URL; //to local machine
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/",
                filename);
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            FileEntity reqEntity = new FileEntity(file, "binary/octet-stream");
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(true); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);

            String res = EntityUtils.toString(response.getEntity());
            System.out.println(res);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
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
            outputStream.writeBytes("Content-Disposition: form-data; name=\"user_id\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(String.valueOf(this.userId));
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
                System.out.println(sb.toString());
                is.close();
            }
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            //Delete file
            File myfile = new File(selectedPath);
            boolean deleted = myfile.delete();

        } catch (Exception ex) {
            System.out.println("Error uploading file: "+filename);
        }
    }

    public void setUserId(int user_id) {
        this.userId = user_id;
    }
}
