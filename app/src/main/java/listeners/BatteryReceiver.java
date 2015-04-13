package listeners;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

/**
 * Created by Ivan (elaboration of Pieter Mekerle's code)
 * Receive broadcast transmission of battery status changes
 */
public class BatteryReceiver extends BroadcastReceiver {
    protected static int pseudo_id = 90; //not really a sensor
    @SuppressLint("SimpleDateFormat")
    @Override
    /**
     * When receiving a new status
     */
    public void onReceive(Context context, Intent intent) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            float level = BatteryReceiver.getBatteryLevel(context);
            //FORMAT:  charging, usb, ac, level, timestamp
            String x = (isCharging ? 1 : 0) + "," + (usbCharge ? 1 : 0) + "," + (acCharge ? 1 : 0) + "," + level;
            logActivity(x);
        }catch(Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    /**
     * Retrieve battery level on demand (not broadcast)
     * @param context
     * @return
     */
    public synchronized  static float getBatteryLevel(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (level / (float)scale)*100; // 0 - 1 percent value
    }

    /**
     * Store to file
     * @param string
     */
    private void logActivity(String string){
        try {
            File external = Environment.getExternalStorageDirectory(); //external to share to other apps later
            String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String filename = BatteryReceiver.getFileName();

            File dir = new File(sdcardPath+"/Documents");
            File file = new File(sdcardPath+"/Documents/",filename);

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
        String now = Calendar.getInstance().get(Calendar.YEAR)+""+(Calendar.getInstance().get(Calendar.MONTH)+1)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String filename = "Battery_"+now+".txt";
        return filename;
    }

    /**
     * Retrieves pseudo id of battery "sensor"
     * @return
     */
    public synchronized static int getType(){
        return BatteryReceiver.pseudo_id;
    }
}
