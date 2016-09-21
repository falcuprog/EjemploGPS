package com.seminnova.mvpar.ejemplogpsnuevo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 100;

    /***/

    ArrayList<MonumentoMarker> ls = new ArrayList<>();

    private Marker marcador;
    double lat = 0.0, lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        crearMarkers();

        mMap = googleMap;

        googleMap.setOnMarkerClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        /*
        LatLng sydney = new LatLng(10.4598546, -73.2467258);
        mMap.addMarker(new MarkerOptions().position(sydney).title("CaliBella"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        /**/

        miUbicacion();

        for (int i = 0; i < ls.size(); i++){
            LatLng coordenadas = new LatLng(ls.get(i).getLat(), ls.get(i).getLon());
            mMap.addMarker(new MarkerOptions().position(coordenadas).title(ls.get(i).getNombre()));
        }



        /*DIBUJAR LÍNEA
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(sydney, new LatLng(10.4598632, -73.2480631))
                .width(5)
                .color(Color.RED));

        /***/



    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  GPS turned off, Show the user a dialog
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show dialog
                }

                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /***************/

    private void agregarMarcador(double lat, double lon) {
        LatLng coordenadas = new LatLng(lat, lon);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        if (marcador != null) marcador.remove();
        marcador = mMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .title("Aquí estoy")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.animateCamera(miUbicacion);
    }

    private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Toast.makeText(getApplicationContext(), "Latitud: " + lat + "Longitud: " + lon, Toast.LENGTH_SHORT).show();
            agregarMarcador(lat, lon);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            actualizarUbicacion(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        actualizarUbicacion(location);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, locationListener);

    }

    /***************/

    public void crearMarkers(){
        MonumentoMarker monumentoMarker = new MonumentoMarker();
        monumentoMarker.setNombre("Monu1");
        monumentoMarker.setLat(10.4598546);
        monumentoMarker.setLon(-73.2467258);
        ls.add(monumentoMarker);

        MonumentoMarker monumentoMarker2 = new MonumentoMarker();
        monumentoMarker2.setNombre("Monu2");
        monumentoMarker2.setLat(10.4698546);
        monumentoMarker2.setLon(-73.2467258);
        ls.add(monumentoMarker2);

        MonumentoMarker monumentoMarker3 = new MonumentoMarker();
        monumentoMarker3.setNombre("Monu3");
        monumentoMarker3.setLat(10.4798546);
        monumentoMarker3.setLon(-73.2467258);
        ls.add(monumentoMarker3);

        MonumentoMarker monumentoMarker4 = new MonumentoMarker();
        monumentoMarker4.setNombre("Monu4");
        monumentoMarker4.setLat(10.4798546);
        monumentoMarker4.setLon(-73.2567258);
        ls.add(monumentoMarker4);

        MonumentoMarker monumentoMarker5 = new MonumentoMarker();
        monumentoMarker5.setNombre("Monu5");
        monumentoMarker5.setLat(10.4898546);
        monumentoMarker5.setLon(-73.2567258);
        ls.add(monumentoMarker5);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTag().toString(), Toast.LENGTH_SHORT).show();
        return true;
    }
}

