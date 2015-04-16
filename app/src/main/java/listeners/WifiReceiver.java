package listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

/**
 * Receiver for broadcast transmission of Wifi adapter status change
 */
public class WifiReceiver extends BroadcastReceiver {
    protected static int pseudo_id = 91; //not really a sensor
    @Override
    /**
     * Only on state or connectivity changes
     */
    public void onReceive(Context context, Intent intent) {
        String type = intent.getAction();
        String state = "";
        if(type == "android.net.wifi.WIFI_STATE_CHANGED" || type == "android.net.conn.CONNECTIVITY_CHANGE") {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);            
            if(wifiManager.isWifiEnabled()){
                WifiInfo info = wifiManager.getConnectionInfo();
                state = info.getSSID();
            }
            //				0 WIFI_STATE_DISABLING
            //				1 WIFI_STATE_DISABLED
            //				2 WIFI_STATE_ENABLING
            //				3 WIFI_STATE_ENABLED
            //				4 WIFI_STATE_UNKNOWN
            state = wifiManager.getWifiState() + ","+state;
            logActivity(state);
        }
    }

    /**
     * Store to file
     * @param string
     */
    private void logActivity(String string){
        try {
            File external = Environment.getExternalStorageDirectory(); //external to share to other apps later
            String sdcardPath = external.getPath();
            String filename = WifiReceiver.getFileName();
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

    /**
     * Retrieves log's filename for current date
     * @return
     */
    public synchronized static String getFileName(){
        //String now = Calendar.getInstance().get(Calendar.YEAR)+""+(Calendar.getInstance().get(Calendar.MONTH)+1)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String filename = "Wifi_"+".txt";
        return filename;
    }

    /**
     * Retrieves pseudo id of wifi adapter "sensor"
     * @return
     */
    public synchronized static int getType(){
        return WifiReceiver.pseudo_id;
    }
};