package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.ridesharingmining.www.activities.R;
import com.ridesharingmining.www.activities.SensorActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lib.ContextManager;
import lib.FileUploader;
import lib.ParameterSettings;
import lib.db.AbstractDataObject;
import lib.db.AppSQLiteHelper;
import lib.db.GpsDataObject;
import lib.db.StayPointDataObject;
import lib.mining.CsvParser;
import lib.mining.GpsLog;
import lib.mining.ModelParameters;
import lib.mining.StayPoint;
import listeners.GPSListener;

/**
 * Created by Ivan on 14/04/2015.
 */
public class SensorService extends Service {
    private ContextManager contextManager;
//    private SensorListener sensorListener;
    private GPSListener gpsListener;
    private Resources resources;
    private static FileUploader uploader;
    private Timer timer;
    private static String username;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public SensorService() {
        super();
    }

    @Override
    /**
     * Instatiate a new service (not run yet)
     */
    public void onCreate(){
        this.contextManager = new ContextManager(this);
        //this.sensorListener = new SensorListener(this.contextManager);
        this.gpsListener = new GPSListener(this);
        this.resources = this.getBaseContext().getResources();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

   /* @Override
    protected void onHandleIntent(Intent intent) {
        Bundle b = intent.getExtras();
        this.username = b.getString("USERNAME");

        this.displayNotificationIcon();
        this.gpsListener.start();
        this.startAutoSync();
    }*/

    @Override
    /**
     * Run the service (start listening)
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = intent.getExtras();
        this.username = b.getString("USERNAME");

        super.onStartCommand(intent,flags,startId);
        this.displayNotificationIcon();
//        this.sensorListener.start();
        this.gpsListener.start();
        this.startAutoSync();
        return Service.START_REDELIVER_INTENT;
    }
    @Override
    /**
     * Stop the service (pause sensors)
     */
    public void onDestroy() {
        super.onDestroy();
//        this.sensorListener.pause();
        this.gpsListener.pause();
        this.stopAutoSync();
        this.removeNotificationIcon();
        mineData(this);

        //last check whether there were files to be uploaded
//        if(ContextManager.haveInternetAccess(this)) {
//            FileUploader _uploader = new FileUploader();
//            synchronized (_uploader) {
//                _uploader.uploadAllFiles(this.username);
//            }
//        }
    }

    /**
     * Search for stay points in gps log since last position (last stay point)
     * @param context
     */
    public synchronized static void mineData(Context context){
        //Get stay points on client
        boolean debug = ModelParameters.debug_mode;
        GpsLog gps_log = new GpsLog();
        GpsDataObject gps_DAO = GpsDataObject.GetInstance(context);
        if(!gps_DAO.isLocked()) {
            gps_log.setGps_points(gps_DAO.GetAll());
            if (gps_log.length() > 0) {
                //store stay points in database
                List<StayPoint> stay_points = gps_log.GetStayPoints(0, gps_log.length());
                StayPointDataObject stay_point_DAO = StayPointDataObject.GetInstance(context);
                Iterator<StayPoint> it = stay_points.iterator();
                while(it.hasNext()){
                    StayPoint sp = it.next();
                    sp.id = stay_point_DAO.Persist(sp); //add new stay point (or update last one)
                }
                 //delete all processed data before last stay point
                if(stay_points.size() > 0) {
                    int affected = gps_DAO.Purge(stay_points.get(stay_points.size() - 1).getArrival());
                    if (affected > 0){
                        if(debug) ContextManager.writeAppLog(affected + " points were purged.");}
                }

                //upload to server
                SensorService.uploadToCloud(context,username);
            }
        }
    }

    /**
     * Synchronize with server
     * @param context
     * @param username
     */
    public synchronized static void uploadToCloud(Context context, String username){
        String filename = ModelParameters.csv_stay_points;
        StayPointDataObject DAO = StayPointDataObject.GetInstance(context);
        List<StayPoint> stay_points = DAO.GetAll();
        CsvParser.PersistStayPoints(stay_points);
        FileUploader _uploader = new FileUploader();
        synchronized (_uploader) {
            _uploader.uploadFile(username,filename, ParameterSettings.StayPointsSensorID); //file will be removed here
        }
    }

    /**
     * Define timer for auto updates
     */
    protected void startAutoSync(){
        this.timer = new Timer();
        //final String c =  this.username;
        final Context c = this;

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            mineData(c);
        //check whether there are files to be uploaded
//                if(ContextManager.haveInternetAccess(context)) {
//            FileUploader _uploader = new FileUploader();
//            synchronized (_uploader) {
//                _uploader.uploadAllFiles(c);
//            }
                }
//            }
        }, ParameterSettings.TimerInterval, ParameterSettings.TimerInterval);
    }

    /**
     * Stop the timer
     */
    private void stopAutoSync() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    /**
     * Display icon on notification bar
     */
    protected void displayNotificationIcon(){
        //When clicked go to this activity
        Intent resultIntent = new Intent(this, SensorActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(  this,  0,  resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //display the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.icon_notification);
        mBuilder.setContentTitle(resources.getString(R.string.notification_title));
        mBuilder.setContentText(resources.getString(R.string.notification_text));
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setOngoing(true);
        //issue notification
        NotificationManager mNotifyMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(R.id.notification_id, mBuilder.build());
    }

    /**
     * Remove from notification bar
     */
    protected void removeNotificationIcon(){
        NotificationManager mNotifyMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(R.id.notification_id);
    }


}
