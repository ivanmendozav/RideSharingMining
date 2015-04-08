/*
package com.ridesharingmining.www.ridesharingmining;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.Driver;
import app.JSON_Connection;
import app.driversGridAdapter;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;


public class MainActivitybackup extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static final String EXTRA_MESSAGE = "com.ridesharingmining.www.ridesharingmining.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.buildGoogleApiClient();
    }


    protected synchronized void buildGoogleApiClient() {
        if (isGooglePlayServicesAvailable(this)== 	ConnectionResult.SUCCESS) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            getGeocoding();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            EditText editText = (EditText) findViewById(R.id.edit_message);
                            editText.setText("Connection to GPS was suspended.");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            EditText editText = (EditText) findViewById(R.id.edit_message);
                            editText.setText("Connection to GPS failed!");
                        }
                    })

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_search:
//                openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {

    }

*
     * Click on Search button in Action Bar


  private void openSearch() {
        String url ="http://www.proyectomed.org/json2.php";
        Map<String, String> params = null;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSON_Connection jsObjRequest = new JSON_Connection(Request.Method.POST, url, params, this.onSearchSuccess(), this.onSearchFailure());
        requestQueue.add(jsObjRequest);
    }


//    private Response.Listener<JSONObject> onSearchSuccess(){
 final GridView grid = (GridView) findViewById(R.id.results_grid);
        final Context main = this;
        Response.Listener<JSONObject> r = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject glossary = response.getJSONObject("query");
                    JSONArray GlossDiv = glossary.getJSONArray("list");
                    final List<Driver> lDriver = new ArrayList<>();
                    int i;
                    for (i = 0; i < GlossDiv.length(); i++) {
                        Driver driver = new Driver((JSONObject) GlossDiv.getJSONObject(i));
                        lDriver.add(driver);
                    }
                    grid.setAdapter(new driversGridAdapter(main, lDriver));
                    grid.setOnItemClickListener(OnClickGrid());
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        return r;

//    }

    private AdapterView.OnItemClickListener OnClickGrid(){
        final Context c = this;
        return new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(c, DisplayMessageActivity.class);
                intent.putExtra(EXTRA_MESSAGE, ((Driver)(((GridView) parent).getAdapter()).getItem(position)).getName());
                startActivity(intent);
            }
        };
    }

    private Response.ErrorListener onSearchFailure(){
        Response.ErrorListener r = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error retrieving JSON data"+error.getMessage());
            }
        };
        return r;
    }

    public void getGeocoding(Location mLastLocation){
            EditText editText = (EditText) findViewById(R.id.edit_message);
            editText.setText(String.valueOf(mLastLocation.getLatitude())+" "+String.valueOf(mLastLocation.getLongitude()));
    }
* Called when the user clicks the Send button

    public void sendMessage(View view) {
Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

        //getGeocoding();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        if (mLastLocation != null) {
            getGeocoding(mLastLocation);
        }
        else{
            LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,this. mLocationRequest, this);
        }
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
        getGeocoding(mLastLocation);
    }

}
*/
