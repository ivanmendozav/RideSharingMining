package listeners;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

/**
 * Created by Ivan on 13/04/2015.
 */
public class GPSListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /**
     * Constructor
     * @param context
     */
    public GPSListener(Context context){
        super();
        this.context = context;
        this.buildGoogleApiClient();
    }

    /**
     * Start GPS
     */
    public void start(){
        this.mGoogleApiClient.connect();
    }

    /**
     * Stop GPS
     */
    public void pause(){
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
    /**
     * Prepare for location services
     */
    protected synchronized void buildGoogleApiClient() {
        if (isGooglePlayServicesAvailable(this.context)== 	ConnectionResult.SUCCESS) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this.context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            // Create the LocationRequest object
            this.mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000) // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        }
    }

    /**
     * Parse object to coordinates
     * @param mLastLocation
     */
    protected void readLocation(Location mLastLocation){
        String string = String.valueOf(mLastLocation.getLatitude())+","+String.valueOf(mLastLocation.getLongitude())+","+String.valueOf(mLastLocation.getAltitude())+","+String.valueOf(mLastLocation.getTime());
        logText(string);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        if (mLastLocation != null) {
            readLocation(mLastLocation);
        }
        //Always request update location
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,this. mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location mLastLocation) {
        readLocation(mLastLocation);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Store in file
     * @param string
     */
    private void logText(String string){
        try {
            File external = Environment.getExternalStorageDirectory(); //external to share to other apps later
            String sdcardPath = external.getPath();
            String now = Calendar.getInstance().get(Calendar.YEAR)+""+(Calendar.getInstance().get(Calendar.MONTH)+1)+""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            String FILENAME = "location_"+now+".txt";
            //String sdcardPath = "C:/Users/Ivan/AndroidStudioProjects/RideSharingMining";

            File dir = new File(sdcardPath+"/Documents");
            File file = new File(sdcardPath+"/Documents/"+FILENAME);

            if (!dir.isDirectory())
                dir.mkdirs(); //create all necessary directories

            file.createNewFile();
            FileWriter filewriter = new FileWriter(sdcardPath+"/Documents/"+FILENAME, true); //true for append
            BufferedWriter out = new BufferedWriter(filewriter);
            out.write(string+"\n"); //CSV format with line break between measures
            out.close();
            filewriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static int getType(){
        return 92;
    }
}
