package com.example.placesaver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapRegionDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /////////////////////////////////////////
    //when long-clicked, get geocoder data //
    //and put a marker in that spot        //
    /////////////////////////////////////////
    public void addPlace(Location location, final LatLng latLng){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        address = "";
        //from geocoder, get the address of the marker. if not available, asks user to input the name of the place
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if(listAddresses!=null && listAddresses.size()>0 && listAddresses.get(0).getAdminArea()!=null){
                if(listAddresses.get(0).getFeatureName() != null){
                    address += listAddresses.get(0).getFeatureName() + " ";
                }
                if(listAddresses.get(0).getThoroughfare() != null){
                    address += listAddresses.get(0).getThoroughfare() + " ";
                }
                if(listAddresses.get(0).getLocality() != null){
                    address += listAddresses.get(0).getLocality() + " ";
                }
                if(listAddresses.get(0).getAdminArea() != null){
                    address += listAddresses.get(0).getAdminArea() + " ";
                }
                if(listAddresses.get(0).getPostalCode() != null){
                    address += listAddresses.get(0).getPostalCode();
                }
                mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                Toast.makeText(MapsActivity.this, address + "\nhas been added to the list!", Toast.LENGTH_SHORT).show();
//                Log.i("Addresses", address);
//                Log.i("listAddresses", listAddresses.get(0).toString());
            } else {
                //pop up dialog to get the user input
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("no address found. please enter name to save as");

                    input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            address = input.getText().toString();
//                            Toast.makeText(getApplicationContext(), "Address is " + address + " input is " + input, Toast.LENGTH_SHORT).show();
                            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } catch (Exception e){
                    e.printStackTrace();
                    address = "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MapsActivity.this, "fail", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /////////////////////////////////////////
        //////set up and put initial marker//////
        //////also has long click listener///////
        /////////////////////////////////////////
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                mMap.clear();
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("User location"));
           };
            /////////////////////////////////////////
            //////NOT USED / DON'T KNOW WHAT FOR/////
            /////////////////////////////////////////
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
            @Override
            public void onProviderEnabled(String provider) {

            }
            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        /////////////////////////////////////////
        ///////// GPS Permission/Check //////////
        /////////////////////////////////////////
        if(Build.VERSION.SDK_INT < 23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                Toast.makeText(this, "No GPS Signal", Toast.LENGTH_SHORT).show();
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mMap.clear();
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("User location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
                Toast.makeText(this, "GPS Signal Received", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if(listAddresses!=null && listAddresses.size()>0 && listAddresses.get(0).getAdminArea()!=null) {
                if (listAddresses.get(0).getFeatureName() != null) {
                    address += listAddresses.get(0).getFeatureName() + " ";
                }
                if (listAddresses.get(0).getThoroughfare() != null) {
                    address += listAddresses.get(0).getThoroughfare() + " ";
                }
                if (listAddresses.get(0).getLocality() != null) {
                    address += listAddresses.get(0).getLocality() + " ";
                }
                if (listAddresses.get(0).getAdminArea() != null) {
                    address += listAddresses.get(0).getAdminArea() + " ";
                }
                if (listAddresses.get(0).getPostalCode() != null) {
                    address += listAddresses.get(0).getPostalCode();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        if (address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            address += sdf.format(new Date());
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.arrayList.add(address);
        MainActivity.locationList.add(latLng);

        MainActivity.arrayAdapter.notifyDataSetChanged();
    }
}
