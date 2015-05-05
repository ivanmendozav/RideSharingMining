package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.ridesharingmining.www.activities.R;
import com.ridesharingmining.www.activities.SensorActivity;

import java.util.Timer;
import java.util.TimerTask;

import lib.ContextManager;
import lib.FileUploader;
import lib.ParameterSettings;
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
    private String username;

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
        //last check whether there were files to be uploaded
        FileUploader _uploader = new FileUploader();
        synchronized (_uploader) {
            _uploader.uploadAllFiles(this.username);
        }
    }

    protected void startAutoSync(){
        this.timer = new Timer();
        final String c =  this.username;

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //check whether there are files to be uploaded
                FileUploader _uploader = new FileUploader();
                synchronized (_uploader) {
                    _uploader.uploadAllFiles(c);
                }
            }
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
