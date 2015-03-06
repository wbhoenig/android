package com.example.kush.geocaching461l;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class MapsActivity extends FragmentActivity{
    /*Bonus 1:          Clicking on marker will show the indentifier of the input.  Ex. entering Spain
                            will produce a marker identified as "country);
      Extra Features:    After each address is entered, the map will center on the newest added marker
                             This bonus should be 3 points

                         Markers have been replaced with ducks.  This bonus should be 2 points.
                         Personally it is worth 15

                         Button has been added to find current location.
                         This feature should be worth 4 points
                         
                         TODO:  Fix main menu.  This is worth 6 points once working
            */
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    double currentLatitude;
    double currentLongitude;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void displayCurrentLocation (View view) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000, 10, this);
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if (gpsLocation != null) {
            currentLatitude = gpsLocation.getLatitude();
            currentLongitude = gpsLocation.getLongitude();
        }
        mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude))
                    .title("Current Location"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(25, 25)).title(currentLatitude+""));
        //setUpMapIfNeeded();
    }

    public void parseLocation (View view){

        EditText e = (EditText)findViewById(R.id.EnterLocation);
        String st = e.getText().toString();

        st = st.replace(' ', '+');
        st = "https://maps.googleapis.com/maps/api/geocode/json?address=" + st;
        //st = st + "&key=AIzaSyBuRTXTtX1vQsE_OPSa9ZHRW3cAFmg4WnM";

        String jsonString = "";
        try {

            HttpClient client = new DefaultHttpClient();

            URI uri = new URI(st);

            HttpGet httpGet = new HttpGet(uri);
            HttpResponse response = client.execute(httpGet);

            InputStream inputStream = response.getEntity().getContent();
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            StringBuffer buffer = new StringBuffer();
            int inChar;

            while ((inChar = reader.read()) != -1) {
                buffer.append((char) inChar);
            }

            jsonString = buffer.toString();
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("results");
            String name= ((JSONArray)json.get("results")).getJSONObject(0)
                    .getJSONArray("address_components").getJSONObject(0).getJSONArray("types").getString(0);
            json = array.getJSONObject(0);
            json = json.getJSONObject("geometry");
            json = json.getJSONObject("location");
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");


            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.duckmedium)));
           LatLng latlng=new LatLng(lat,lng);
           CameraUpdate center=CameraUpdateFactory.newLatLng(latlng);
            mMap.moveCamera(center);
            mMap.getMyLocation();
        }
        catch (JSONException j) {
            e.setText("Invalid address");
            j.printStackTrace();
            Log.d("jsonString", jsonString);
            return;
        }
        catch (Exception ex) { return; }

        e.setText("");

    }


}
