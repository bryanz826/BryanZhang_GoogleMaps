package com.example.bryan.mymapsapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private EditText locationSearch;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean gotMyLocationOneTime;
    private boolean notTrackingMyLocation = true;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; //updates in msec
    private static final float MiN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_BOOM_FACTOR = 17;
    private static final double SCOPE = 5.0/60.0;

    private double lastLocationX = 0.0;
    private double lastLocationY = 0.0;
    private double currentLocationX = 0.0;
    private double currentLocationY = 0.0;

    private double distanceX;
    private double distanceY;
    private double distanceX2;
    private double distanceY2;
    private double slope;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng sandiego = new LatLng(32.72, -117.16);
        mMap.addMarker(new MarkerOptions().position(sandiego).title("Born here"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sandiego));

        /*if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("MyMapsApp", "Failed FINE Permission Check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("MyMapsApp", "Failed COARSE Permission Check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)||
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }*/

        locationSearch = (EditText) findViewById(R.id.editText_address);

        gotMyLocationOneTime = false;
        getLocation();
    }

    public void changeView(View view){
        if(mMap.getMapType() == mMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        }
    }

    public void onSearchForward(View view){
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //Use LocationManager for user location
        //Implement the LocationListener interface to setup location services
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);


        Log.d("MyMapsApp","onSearch: location = " + location);
        Log.d("MyMapsApp","onSearch: provider " + provider);

        LatLng userLocation = null;

        //Check the last known location, need to specifically list the provider (network or gps)

        try{
            if(locationManager != null) {
                Log.d("MyMapsApp","onSearch: locationManager is not null");

                if((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using NETWORK_PROVIDER userLocation is "
                            + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using GPS_PROVIDER userLocation is "
                            + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else{
                    Log.d("MyMapsApp","onSearch: myLocation is null from getLastKnownLocation");
                }
            } else {
                Log.d("MyMapsApp","onSearch: locationManager is null");
            }
        }
        catch(SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp","onSearch: Exception getLastKnownLocation");
            Toast.makeText(this,"onSearch: Exception getLastKnownLocation", Toast.LENGTH_SHORT);
        }


        slope = (currentLocationY -lastLocationY ) / (currentLocationX - lastLocationX);
        distanceX2 = 5 / Math.sqrt(1 + Math.pow(slope, 2));
        distanceY2 = slope * distanceX2;
        slope = -1*(1/slope);
        distanceX = 5 / Math.sqrt(1 + Math.pow(slope, 2));
        distanceY = slope * distanceX;

        //Get the location if it exists
        if(!location.matches("")){
            Log.d("MyMapsApp","onSearch: location field is populated");
            Toast.makeText(MapsActivity.this,"Searching",Toast.LENGTH_LONG).show();
            Geocoder geocoder = new Geocoder(this, Locale.US);
            Log.d("MyMapsApp","onSearch: created Geocoder");
            try{
                if(slope > 0 ){
                    if(Math.abs(currentLocationY) > Math.abs(lastLocationY) || (Math.abs(currentLocationX) > Math.abs(lastLocationX))){
                        Toast.makeText(MapsActivity.this,"traveling Southeast",Toast.LENGTH_LONG).show();
                        addressList = geocoder.getFromLocationName(location, 100,
                                userLocation.latitude + (distanceX/60),
                                userLocation.longitude + (distanceY/60),
                                userLocation.latitude - (distanceX/60) + (distanceX2/60),
                                userLocation.longitude - (distanceY/60) - (distanceY2/60));
                    } else {
                        Toast.makeText(MapsActivity.this,"traveling Northwest",Toast.LENGTH_LONG).show();

                        addressList = geocoder.getFromLocationName(location, 100,
                                userLocation.latitude - (distanceX/60),
                                userLocation.longitude - (distanceY/60),
                                userLocation.latitude + (distanceX/60) - (distanceX2/60),
                                userLocation.longitude + (distanceY/60) + (distanceY2/60));
                    }
                } else{
                    if(Math.abs(currentLocationY) > Math.abs(lastLocationY) || (Math.abs(currentLocationX) > Math.abs(lastLocationX)))
                    {
                        Toast.makeText(MapsActivity.this,"traveling Northeast",Toast.LENGTH_LONG).show();
                        addressList = geocoder.getFromLocationName(location, 100,
                                userLocation.latitude - (distanceX/60),
                                userLocation.longitude + (distanceY/60),
                                userLocation.latitude + (distanceX/60) - (distanceX2/60),
                                userLocation.longitude - (distanceY/60) + (distanceY2/60));
                    }
                    else{
                        Toast.makeText(MapsActivity.this,"traveling Southwest",Toast.LENGTH_LONG).show();
                        addressList = geocoder.getFromLocationName(location, 100,
                                userLocation.latitude + (distanceX/60),
                                userLocation.longitude - (distanceY/60),
                                userLocation.latitude - (distanceX/60) - (distanceX2/60),
                                userLocation.longitude + (distanceY/60) - (distanceY2/60));
                    }
                }
                //Get a List of the addresses
                /*addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0/60),
                        userLocation.longitude - (5.0/60),
                        userLocation.latitude + (5.0/60),
                        userLocation.longitude + (5.0/60));*/
                Toast.makeText(MapsActivity.this,"addressList is created",Toast.LENGTH_LONG).show();
                Log.d("MyMapsApp","onSearch: addressList is created");
            }
            catch(IOException e){
                e.printStackTrace();
                Toast.makeText(MapsActivity.this,"failed",Toast.LENGTH_LONG).show();
            }

            if(!addressList.isEmpty()) {
                Toast.makeText(MapsActivity.this,"address list size is " + addressList.size(),Toast.LENGTH_LONG).show();
                Log.d("MyMapsApp", "onSearch: addressList size is " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //Place a marker on the map
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i+ ": " + address.getSubThoroughfare()
                            + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }
        }
    }

    public void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //Get GPS status, isProviderEnabled returns true if user has enabled GPS
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            if(isGPSEnabled){
                Log.d("MyMapsApp","getLocation: GPS is enabled");
            }

            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled){
                Log.d("MyMapsApp","getLocation: Network is enabled");
            }

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: no provider enabled");
            } else {
                if(isNetworkEnabled){
                    //request location updates
                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if(isGPSEnabled){
                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                }
            }

        }
        catch(Exception e){
            Log.d("MyMapsApp", "getLocation: Exception in getLocation");
            e.printStackTrace();


        }

    }

    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.NETWORK_PROVIDER);

            //Check if doing one time, if so remove updates to both gps and network'
            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTime = true;
            } else {
                if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }



        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp","locationListenerNetwork: status change");
            Toast.makeText(MapsActivity.this,"status change",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.GPS_PROVIDER);

            //Check if doing one time, if so remove updates to both gps and network'
            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            }
            /*else {
                if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
            }*/


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp","locationListenerNetwork: status change");
            Toast.makeText(MapsActivity.this,"status change",Toast.LENGTH_LONG).show();
            switch(status){
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp","locationListenerNetwork: GPS available");
                    Toast.makeText(MapsActivity.this,"location provider available",Toast.LENGTH_LONG).show();
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp","locationListenerNetwork: GPS out of service");
                    Toast.makeText(MapsActivity.this,"status change",Toast.LENGTH_LONG).show();
                    if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MiN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                default:


            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    public void dropAmarker(String provider) {
        LatLng userLocation = null;
        try {
            if(locationManager != null) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            if ((myLocation = locationManager.getLastKnownLocation(provider)) != null) {
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation,MY_LOC_BOOM_FACTOR);
                if(provider == LocationManager.GPS_PROVIDER) {
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                } else if(provider == LocationManager.NETWORK_PROVIDER){
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }

                lastLocationX = currentLocationX;
                lastLocationY = currentLocationY;
                currentLocationX = myLocation.getLatitude();
                currentLocationY = myLocation.getLongitude();


                mMap.animateCamera(update);
            } else{
                Log.d("MyMapsApp","dropAmarker: location is null");
            }

        } catch (SecurityException e) {
            Log.d("MyMapsApp", "Exception in dropAmarker");
        }
    }

    public void dropAmarker2(View view) {
        LatLng userLocation = null;
        try {
            if(locationManager != null) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            if(((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null)) {
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation,MY_LOC_BOOM_FACTOR);

                mMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


                lastLocationX = currentLocationX;
                lastLocationY = currentLocationY;
                currentLocationX = myLocation.getLatitude();
                currentLocationY = myLocation.getLongitude();


                mMap.animateCamera(update);
            } else{
                Log.d("MyMapsApp","dropAmarker: location is null");
            }

        } catch (SecurityException e) {
            Log.d("MyMapsApp", "Exception in dropAmarker");
        }
    }

    public void trackMyLocation(View view){

        if(notTrackingMyLocation){
            getLocation();
            notTrackingMyLocation = false;

        } else {
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            notTrackingMyLocation = true;
        }
    }

    public void clear (View view){
        mMap.clear();
    }

    public void onSearch(View view) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //Use LocationManager for user location
        //Implement the LocationListener interface to setup location services
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);


        Log.d("MyMapsApp","onSearch: location = " + location);
        Log.d("MyMapsApp","onSearch: provider " + provider);

        LatLng userLocation = null;

        //Check the last known location, need to specifically list the provider (network or gps)

        try{
            if(locationManager != null) {
                Log.d("MyMapsApp","onSearch: locationManager is not null");

                if((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using NETWORK_PROVIDER userLocation is "
                            + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using GPS_PROVIDER userLocation is "
                            + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else{
                    Log.d("MyMapsApp","onSearch: myLocation is null from getLastKnownLocation");
                }
            } else {
                Log.d("MyMapsApp","onSearch: locationManager is null");
            }
        }
        catch(SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp","onSearch: Exception getLastKnownLocation");
            Toast.makeText(this,"onSearch: Exception getLastKnownLocation", Toast.LENGTH_SHORT);
        }

        //Get the location if it exists
        if(!location.matches("")){
            Log.d("MyMapsApp","onSearch: location field is populated");
            Toast.makeText(MapsActivity.this,"Searching",Toast.LENGTH_LONG).show();
            Geocoder geocoder = new Geocoder(this, Locale.US);
            Log.d("MyMapsApp","onSearch: created Geocoder");
            try{
                //Get a List of the addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (SCOPE),
                        userLocation.longitude - (SCOPE),
                        userLocation.latitude + (SCOPE),
                        userLocation.longitude + (SCOPE));
                Toast.makeText(MapsActivity.this,"addressList is created",Toast.LENGTH_LONG).show();
                Log.d("MyMapsApp","onSearch: addressList is created");
            }
            catch(IOException e){
                e.printStackTrace();
                Toast.makeText(MapsActivity.this,"failed",Toast.LENGTH_LONG).show();
            }

            if(!addressList.isEmpty()) {
                Toast.makeText(MapsActivity.this,"address list size is " + addressList.size(),Toast.LENGTH_LONG).show();
                Log.d("MyMapsApp", "onSearch: addressList size is " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //Place a marker on the map
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i+ ": " + address.getSubThoroughfare()
                            + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }
        }
    }

    public void searchForward(View view) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //use LocationManager  for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp:", "onSearch: location = " + location);
        Log.d("MyMapsApp:", "onSearch: provider = " + provider);

        LatLng userLocation = null;
        try {
            //check the last known location, need to specifically list the provider network or gps
            if (locationManager != null) {
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null");
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Exception on getLastKnownLocation");
        }

        if (!location.matches("")) {

            double finalUpperRightLat = 0.0;
            double finalUpperRightLong = 0.0;
            double finalLowerLeftLat = 0.0;
            double finalLowerLeftLong = 0.0;

            if (previousLocation != null) {
                String direction = "";
                double prevX = previousLocation.getLatitude();
                double prevY = previousLocation.getLongitude();
                double currX = myLocation.getLatitude();
                double currY = myLocation.getLongitude();

                if (currX < prevX) direction += "left";
                else if (currX > prevX) direction += "right";
                if (currY < prevY) direction += "up";
                else if (currY > prevY) direction += "down";

                double slopeBtwnPrevAndCurr = (currY - prevY) / (currX - prevX);
                double slopeOfBoundingBoxTop = -1 / slopeBtwnPrevAndCurr;
                double theta = Math.atan(slopeOfBoundingBoxTop);

                double unitDistanceY = SCOPE * Math.sin(theta);
                double unitDistanceX = SCOPE * Math.cos(theta);

                if (direction.contains("left")) {
                    finalLowerLeftLong = currY + unitDistanceY;
                    finalUpperRightLong = currY - 2 * unitDistanceX;
                } else if (direction.contains("right")) {
                    finalLowerLeftLong = currY - unitDistanceY;
                    finalUpperRightLong = currY + 2 * unitDistanceX;
                }

                if (direction.contains("up")) {
                    finalLowerLeftLat = currX - unitDistanceX;
                    finalUpperRightLat = currX + 2 * unitDistanceY;
                } else if (direction.contains("down")) {
                    finalLowerLeftLat = currX + unitDistanceX;
                    finalUpperRightLat = currX - 2* unitDistanceY;
                }

                Log.d("MyMapsApp", "(" + finalLowerLeftLat + ", " + finalLowerLeftLong + "), (" +
                        finalUpperRightLat + ", " + finalUpperRightLong + ")");

                mMap.addMarker(new MarkerOptions().position(new LatLng(finalLowerLeftLat, finalLowerLeftLong)).title("whatthe"));
                mMap.addMarker(new MarkerOptions().position(new LatLng(finalUpperRightLat, finalUpperRightLong)).title("ok"));
            }

            Geocoder geocoder = new Geocoder(this, Locale.US);
            try {
                addressList = geocoder.getFromLocationName(location, 100,
                        finalUpperRightLat, finalUpperRightLong, finalLowerLeftLat, finalLowerLeftLong);
                Log.d("MyMapsApp", "created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("MyMapsApp", "Address list size: " + addressList.size());
            if (!addressList.isEmpty()) {
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latlng).title((i + 1) + ": " + address.getAddressLine(i)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            } else {
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT);
            }
        }
    }
}