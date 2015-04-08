package com.ridesharingmining.www.ridesharingmining;

import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Calendar;


import app.BatteryReceiver;
import app.ContextManager;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static final String EXTRA_MESSAGE = "com.ridesharingmining.www.ridesharingmining.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.buildGoogleApiClient();
    }

    public void enableSensors(View view){
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }
    //Prepare for location services
    protected synchronized void buildGoogleApiClient() {
        if (isGooglePlayServicesAvailable(this)== 	ConnectionResult.SUCCESS) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI(Location mLastLocation){
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String string = String.valueOf(mLastLocation.getLatitude())+","+String.valueOf(mLastLocation.getLongitude())+","+String.valueOf(mLastLocation.getAltitude())+","+String.valueOf(mLastLocation.getTime());
        //editText.setText(string);
        logText(string);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        if (mLastLocation != null) {
            updateUI(mLastLocation);
        }
        //Always request update location
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,this. mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        editText.setText("Connection to GPS was suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        editText.setText("Connection to GPS failed!");
    }

    @Override
    public void onLocationChanged(Location mLastLocation) {
        updateUI(mLastLocation);
    }

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
}
