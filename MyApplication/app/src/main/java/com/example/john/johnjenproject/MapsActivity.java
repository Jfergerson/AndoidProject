package com.example.john.johnjenproject;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import static android.os.SystemClock.sleep;
import static com.example.john.johnjenproject.R.id.map;
import static com.example.john.johnjenproject.R.id.name;
import static com.example.john.johnjenproject.R.id.ratingBar;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private AlertDialog.Builder builder;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private static JSONObject curTruck;

    private static JSONObject mTacoTrucks;
    private static GetTacoTrucksTask mGetTacoTrucksTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_key),MODE_PRIVATE);
        String s = sharedPreferences.getString("result", null);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGetTacoTrucksTask = new MapsActivity.GetTacoTrucksTask();
        mGetTacoTrucksTask.execute((Void) null);

        if (s != null){ }
    }

    public class GetTacoTrucksTask extends AsyncTask<Void, Void, Boolean> {

        GetTacoTrucksTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL("http://104.236.190.64:8000/tacotruck/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                int i = httpURLConnection.getResponseCode();
                Log.d("Got response ", Integer.toString(i));

                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                Log.d("JSON ", sb.toString());
                mTacoTrucks = new JSONObject(sb.toString());
            }
            catch (Exception e) {
                Log.d("Exception: ", e.toString());
            }
            return true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(getInfoWindowClickListener());
        enableMyLocation();

        while(mTacoTrucks == null) { sleep(500); }

        try {
            JSONArray results = mTacoTrucks.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject t = results.getJSONObject(i);
                Log.d("Got truck ", t.toString());

                LatLng truck = new LatLng(t.getDouble("lat"), t.getDouble("lon"));
                Marker marker = mMap.addMarker(new MarkerOptions().position(truck).title(t.getString("name")));
                marker.setTag(t);
            }
        }
        catch (Exception e) {
            Log.d("Exception: ", e.toString());
        }

    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).

        return false;
    }

    public GoogleMap.OnInfoWindowClickListener getInfoWindowClickListener(){
        return new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                try {
                    JSONObject tag = (JSONObject) marker.getTag();

                    enableMyLocation();
                    Location location;
                    LatLng cur_loc = null;
                    if (mMap.isMyLocationEnabled()) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location != null) {
                            cur_loc = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }

                    float[] f = new float[5];
                    Location.distanceBetween(tag.getDouble("lat"), tag.getDouble("lon"), cur_loc.latitude, cur_loc.longitude, f);
                    Toast.makeText(getApplicationContext(), "You are: " + f[0] / 1000 + " KM from this truck", Toast.LENGTH_LONG).show();

                    DialogFragment fire = new CustomDialogFragment();
                    fire.show(getSupportFragmentManager(), "missile");

                    curTruck = tag;
                }
                catch(SecurityException e){
                    Log.d("Security Exception", e.toString());
                }
                catch (Exception e) {
                    Log.d("Exception", e.toString());
                }
            }
        };
    }
    public static class CustomDialogFragment extends DialogFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.display_truck, container, false);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Dialog dialog = super.onCreateDialog(savedInstanceState);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int id){
                }
            });

            builder.setView(R.layout.display_truck);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            ViewGroup vg = (ViewGroup)inflater.inflate(R.layout.display_truck, null);

            ImageView image = (ImageView) vg.findViewById(R.id.truckimg);
            TextView text = (TextView) vg.findViewById(R.id.textview);
            RatingBar ratingBar = (RatingBar) vg.findViewById(R.id.ratingBar);
            TextView name = (TextView) vg.findViewById(R.id.name);

            try {
                name.setText(curTruck.getString("name"));
                text.setText(curTruck.getString("hours"));
                ratingBar.setRating(curTruck.getInt("rating"));

                if (curTruck.getInt("rating") > 2) {
                    image.setImageResource(R.drawable.foodtruck);
                } else {
                    image.setImageResource(R.drawable.coffeespider);
                }
            }
            catch (Exception e) {
                Log.d("Exception", e.toString());
            }

            builder.setView(vg);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return builder.create();
        }
    }
}
