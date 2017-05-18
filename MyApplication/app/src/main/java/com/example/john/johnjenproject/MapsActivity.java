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

import org.w3c.dom.Text;

import java.net.URL;
import java.util.Random;

import static com.example.john.johnjenproject.R.id.map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private AlertDialog.Builder builder;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    public static String review;
    public static float rating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_key),MODE_PRIVATE);
        String s = sharedPreferences.getString("result",null);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     //  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

        if(s != null){//do some stuff to parse vals out of s but do the parsing in on map ready
            //so we can just load in those pointer right away.

        }
    }
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
       // map
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(getInfoWindowClickListener());
        enableMyLocation();
        // Add a marker in Sydney and move the camera
        Location location;
        LatLng cur_loc = null;
        try {
            if (mMap.isMyLocationEnabled()) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null) {
                    cur_loc = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        }catch(SecurityException e){Log.d("sec except",e.toString());}

        LatLng truck_A = new LatLng(39.724315, -121.849576);
        LatLng truck_B = new LatLng(39.730541, -121.858539);
        LatLng truck_C = new LatLng(39.725461, -121.834747);
        float[] dbl = new float[3];
        Marker marker = mMap.addMarker(new MarkerOptions().position(truck_A).title("Truck A"));
        if(cur_loc!= null){
            Location.distanceBetween(truck_A.latitude,truck_A.longitude,cur_loc.latitude,cur_loc.longitude,dbl);
            marker.setTag(dbl[0]);
        }
        marker = mMap.addMarker(new MarkerOptions().position(truck_C).title("Truck C"));
        if(cur_loc!= null){
            Location.distanceBetween(truck_B.latitude,truck_B.longitude,cur_loc.latitude,cur_loc.longitude,dbl);
            marker.setTag(dbl[0]);
        }
        marker = mMap.addMarker(new MarkerOptions().position(truck_B).title("Truck B"));
        if(cur_loc!= null){
            Location.distanceBetween(truck_C.latitude,truck_C.longitude,cur_loc.latitude,cur_loc.longitude,dbl);
            marker.setTag(dbl[0]);
        }


        Log.d("returned from distbet",Float.toString(dbl[0]));
        Log.d("returned from dbl1",Float.toString(dbl[1]));
        Log.d("returned from dbl2",Float.toString(dbl[2]));







        //mMap.moveCamera(CameraUpdateFactory.newLatLng(chico_A));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(chico_A));

    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
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
                if(marker.getTag() != null) {
                    Toast.makeText(getApplicationContext(), "You are: " + (float) marker.getTag() / 1000 + " KM from this truck", Toast.LENGTH_LONG).show();
                }
                DialogFragment fire = new CustomDialogFragment();

                //this is where you are setting up all the review text and
                //rating score that is displayed in the little popup menu
                //add any more images you want to use to the drawables folder manually
                //there is not a way to add them through android studio so drag and drop images
                //into the /res/drawable/ folder and use them that way
 

                review = "hello there";
                Random r = new Random();
                rating = (r.nextInt(50)/10f);
//              review = jsonArray.getjson(marker.getTag()).getString(marker.title())
                //rating = jsonArray.getjson(marker.getTag()).getString(marker.title()).


                fire.show(getSupportFragmentManager(),"missile");

            }
        };
    }
    public static class CustomDialogFragment extends DialogFragment {
        /** The system calls this to get the DialogFragment's layout, regardless
         of whether it's being displayed as a dialog or an embedded fragment. */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Inflate the layout to use as dialog or embedded fragment

            return inflater.inflate(R.layout.display_truck, container, false);
        }

        /** The system calls this only when creating the layout in a dialog. */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // The only reason you might override this method when using onCreateView() is
            // to modify any dialog characteristics. For example, the dialog includes a
            // title by default, but your custom layout might not need it. So here you can
            // remove the dialog title, but you must call the superclass to get the Dialog.
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

            //set text and stuff above here where i set the reveiw and rating to
            //random shit just parse out the relevent bits from the json and set them there
            text.setText(review);
            ratingBar.setRating(rating);

            //this is where you can set the rating that you pass from the json
            //into the program
            if(rating > 2){
                image.setImageResource(R.drawable.foodtruck);

            } else {
                image.setImageResource(R.drawable.coffeespider);
            }



            builder.setView(vg);


            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return builder.create();
        }
    }
}
