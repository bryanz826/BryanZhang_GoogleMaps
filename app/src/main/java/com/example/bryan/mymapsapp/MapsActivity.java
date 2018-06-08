package com.example.bryan.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.google.android.gms.common.util.CrashUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xml.sax.helpers.ParserAdapter;

import java.io.IOException;
import java.nio.file.SecureDirectoryStream;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private boolean getLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean trackingMyLocation = true;

    private static final long MIN_TIME_BW_UPDATES = 1000*5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

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
        LatLng san_diego = new LatLng(32.885939, -117.221751);
        mMap.addMarker(new MarkerOptions().position(san_diego).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(san_diego));

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("MyMapsApp", "Failed FINE permission check");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
//        }
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("MyMapsApp", "Failed COARSE permission check");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
//        }
//
//        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
//            mMap.setMyLocationEnabled(true);
//        }

        locationSearch = (EditText) findViewById(R.id.editText_address);

        getLocationOneTime = false;
        getLocation();
    }

    public void changeView(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

    }

    public void onSearch(View view) {
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
            //check the last known location, ned to specifically list the provider network or gps
            if (locationManager != null) {
                Log.d("MyMapsApp", "onSearch: locationManager not null");
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
            //Create Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                //Get a list of addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0),
                        userLocation.latitude + (5.0 / 60.0),
                        userLocation.longitude + (5.0 / 60.0));
                Log.d("MyMapsApp", "created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size: " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latlng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            }
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMapsApp", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMapsApp", "getLocation: Network is enabled");

            if (!(isGPSEnabled||isNetworkEnabled)) {
                Log.d("MyMapsApp", "getLocation: no provider is enabled!");
            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }

                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                }
            }

        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Caught exception");
            e.printStackTrace();
        }
    }

    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            dropAMarker(LocationManager.NETWORK_PROVIDER);

            //Check if doing one time via onMapReady, if so remove updates to both gps and network
            if (getLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                getLocationOneTime = true;
            } else {
                //if here then tracking so relaunch request for network
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");
            switch(status) {
                case LocationProvider.AVAILABLE:
                    Toast.makeText(getApplicationContext(), "LocationProvider.AVAILABLE", Toast.LENGTH_SHORT).show();
                    Log.d("MyMapsApp", "locationListenerNetwork: LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenerNetwork: LocationProvider.OUT_OF_SERVICE");
                    isNetworkEnabled=true;
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "LocationProvider is temporarily unavailable");
                    isNetworkEnabled=true;
                    isGPSEnabled=true;
                    break;
                default:
                    Log.d("MyMaps", "LocationProvider default");
                    isNetworkEnabled=true;
                    isGPSEnabled=true;
            }
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
            dropAMarker(LocationManager.GPS_PROVIDER);

            //Check if doing one time via onMapReady, if so remove updates to both gps and network
            if (getLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                getLocationOneTime = true;
            } else {
                //if here then tracking so relaunch request for network
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");
            switch(status) {
                case LocationProvider.AVAILABLE:
                    Toast.makeText(getApplicationContext(), "LocationProvider.AVAILABLE", Toast.LENGTH_SHORT).show();
                    Log.d("MyMapsApp", "locationListenerNetwork: LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenerNetwork: LocationProvider.OUT_OF_SERVICE");
                    isNetworkEnabled=true;
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "LocationProvider is temporarily unavailable");
                    isNetworkEnabled=true;
                    isGPSEnabled=true;
                    break;
                default:
                    Log.d("MyMaps", "LocationProvider default");
                    isNetworkEnabled=true;
                    isGPSEnabled=true;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    public void dropAMarker(String provider){
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
            LatLng userLocation = null;
            if (myLocation == null) return;
            else {
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(3).strokeColor(Color.RED).strokeWidth(2));
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(5).strokeColor(Color.RED).strokeWidth(2));
                } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(3).strokeColor(Color.BLUE).strokeWidth(2));
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(5).strokeColor(Color.BLUE).strokeWidth(2));
                }
                mMap.animateCamera(update);
            }
        }
    }

    public void trackMyLocation (View view) {
        if (trackingMyLocation) {
            getLocation();
            Toast.makeText(this, "Tracking On", Toast.LENGTH_SHORT).show();
            trackingMyLocation = false;
        } else {
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGPS);
            Toast.makeText(this, "Tracking On", Toast.LENGTH_SHORT).show();
            trackingMyLocation = true;
        }
    }

    public void clearMap(View view) {
        mMap.clear();
    }
}