package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.ridesharingmining.www.activities.R;
import com.ridesharingmining.www.activities.SensorActivity;
import lib.ContextManager;
import listeners.GPSListener;
import listeners.SensorListener;

/**
 * Created by Ivan on 14/04/2015.
 */
public class SensorService extends Service {
    private ContextManager contextManager;
    private SensorListener sensorListener;
    private GPSListener gpsListener;
    private Resources resources;

    @Override
    /**
     * Instatiate a new service (not run yet)
     */
    public void onCreate(){
        this.contextManager = new ContextManager(this);
        //instantiate sensors
        this.sensorListener = new SensorListener(this.contextManager);
        //instantiate GPS
        this.gpsListener = new GPSListener(this);
        this.resources = this.getBaseContext().getResources();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    /**
     * Run the service (start listening)
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        this.displayNotification();
        this.sensorListener.start();
        this.gpsListener.start();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    /**
     * Stop the service (pause sensors)
     */
    public void onDestroy() {
        super.onDestroy();
        this.sensorListener.pause();
        this.gpsListener.pause();
        //issue notification
        NotificationManager mNotifyMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(R.id.notification_id);
    }

    /**
     * Display icon on notification bar
     */
    protected void displayNotification(){
        //When clicked go to this activity
        Intent resultIntent = new Intent(this, SensorActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(  this,  0,  resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //display the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setContentTitle(resources.getString(R.string.notification_title));
        mBuilder.setContentText(resources.getString(R.string.notification_text));
        mBuilder.setContentIntent(resultPendingIntent);
        //issue notification
        NotificationManager mNotifyMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(R.id.notification_id, mBuilder.build());
    }
}
