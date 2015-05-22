package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.ridesharingmining.www.activities.R;
import com.ridesharingmining.www.activities.SensorActivity;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lib.ContextManager;
import lib.FileUploader;
import lib.ParameterSettings;
import lib.UploaderListener;
import lib.UserData;
import lib.db.GpsDataObject;
import lib.db.PrefeDataObject;
import lib.db.StayPointDataObject;
import lib.mining.CsvParser;
import lib.mining.GpsLog;
import lib.mining.GpsPoint;
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
        //started from activity
        if(intent!=null) {
            Bundle b = intent.getExtras();
            this.username = b.getString("USERNAME");
        }else{ //otherwise take user from the database
            PrefeDataObject preferences = PrefeDataObject.GetInstance(this);
            String stored_user = preferences.Get("user");
            if(stored_user!= null){
                this.username = stored_user;
            }else{
                this.username = ParameterSettings.default_user;
            }
        }

        super.onStartCommand(intent,flags,startId);
        this.displayNotificationIcon();
//        this.sensorListener.start();
        this.gpsListener.start();
        this.startAutoSync();
        this.rememberUser();
        return Service.START_STICKY;
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
        //boolean debug = ModelParameters.debug_mode;
        GpsLog gps_log = new GpsLog();
        GpsDataObject gps_DAO = GpsDataObject.GetInstance(context);
        if(!gps_DAO.isLocked()) {
            //from last checkpoint
            gps_log.setGps_points(gps_DAO.GetAll(UserData.Get(context,"lastStayCheckpoint")));
            if (gps_log.length() > 0) {
                //store stay points in database
                List<StayPoint> stay_points = gps_log.GetStayPoints(0, gps_log.length());
                gps_log = null; //release memory
                StayPointDataObject stay_point_DAO = StayPointDataObject.GetInstance(context);
                Iterator<StayPoint> it = stay_points.iterator();
                while(it.hasNext()){
                    StayPoint sp = it.next();
                    sp.id = stay_point_DAO.Persist(sp); //add new stay point (or update last one)
                }
                 //backup processed data and save last checkPoint before last stay point
                if(stay_points.size() > 0) {
                    long lastStop = stay_points.get(stay_points.size() - 1).getArrival();
                    stay_points = null; //release memory
                    UserData.Set(context, "lastStayCheckpoint", String.valueOf(lastStop));
                    //backup gsp points to server
                    SensorService.uploadTracesToCloud(context, username, lastStop);

//                    int affected = gps_DAO.Purge(lastStop);
//                    if (affected > 0){
//                        if(debug) ContextManager.writeAppLog(affected + " points were purged.");}
                }
                //upload stay points to server
                SensorService.uploadStaysToCloud(context, username);
            }
        }
    }

    /**
     * Synchronize with server
     * @param context
     * @param username
     */
    public synchronized static void uploadStaysToCloud(Context context, String username){
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
     * Synchronize with server
     * @param context
     * @param username
     */
    public synchronized static void uploadTracesToCloud(Context context, String username, long timestamp){
        String filename = GPSListener.getFileName();
        GpsDataObject DAO = GpsDataObject.GetInstance(context);
        final Context c = context;

        String lastBackupPoint = UserData.Get(context,"lastBackupPoint"); //timestamp of last backup

        List<GpsPoint> gps_points = DAO.GetAll(lastBackupPoint);
        if(gps_points.size()>0) {
            CsvParser.PersistGpsPoints(gps_points);
            final String nextBackupPoint = String.valueOf(gps_points.get(gps_points.size()-1).getTimestamp());
            FileUploader _uploader = new FileUploader();
            //update backup point after uploading files
            _uploader.registerListener(new UploaderListener() {
                @Override
                public void OnResponse(String response) {
                    UserData.Set(c,"lastBackupPoint",nextBackupPoint);
                    long lastStayCheckpoint = Long.parseLong(UserData.Get(c,"lastStayCheckpoint"));
                    long toLong = Long.parseLong(nextBackupPoint);
                    boolean debug = ModelParameters.debug_mode;

                    if(toLong > lastStayCheckpoint){
                        GpsDataObject DAO = GpsDataObject.GetInstance(c);
                        int affected = DAO.Purge(lastStayCheckpoint);
                        if (affected > 0){
                            if(debug) ContextManager.writeAppLog(affected + " points were purged.");
                        }
                    }
                }
            });
            synchronized (_uploader) {
                _uploader.uploadFile(username, filename, GPSListener.getType()); //file will be removed here
            }
        }
    }

    /**
     * Store username in database for further use in next session
     */
    private void rememberUser() {
        UserData.Set(this,"user", this.username);
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
        mNotifyMgr.cancelAll();
    }


}
