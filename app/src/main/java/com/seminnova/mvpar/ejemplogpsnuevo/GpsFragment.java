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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class GpsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    //
    String parsedDistance;
    String response;
    //

    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    private int REQUEST_CHECK_SETTINGS = 100;
    private Location miUbicacion = new Location("miUbicacion");
    /***/

    ArrayList<MonumentoMarker> ls = new ArrayList<>();

    private Marker marcador;
    double lat = 0.0, lon = 0.0;

    public GpsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_gps, container, false);

        MultiDex.install(getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**/
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        return view;

    }

    /*@Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
       mapView.getMapAsync(this);
    }*/

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        crearMarkers();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        miUbicacion();

        for (int i = 0; i < ls.size(); i++){
            LatLng coordenadas = new LatLng(ls.get(i).getLat(), ls.get(i).getLon());

            crearMarcador(coordenadas, ls.get(i).getNombre());

        }

    }

    public void calcularDistancias() {

        String distancia;
        Location puntoDestino;

        TextView textView = (TextView) getView().findViewById(R.id.txtPrueba);

        for (int i = 0; i < ls.size(); i++){

            puntoDestino = new Location(ls.get(i).getNombre());
            puntoDestino.setLatitude(ls.get(i).getLat());
            puntoDestino.setLongitude(ls.get(i).getLon());

            distancia = getDistance(miUbicacion.getLatitude(), miUbicacion.getLongitude(), puntoDestino.getLatitude(), puntoDestino.getLongitude());

            Toast.makeText(getContext(), "Lat: " + miUbicacion.getLatitude() + " Lon: " + miUbicacion.getLongitude(), Toast.LENGTH_SHORT).show();

            if (miUbicacion!=null)
                Toast.makeText(getContext(), "Distancia hasta: " + ls.get(i).getNombre() + " ** " + distancia + " **", Toast.LENGTH_SHORT).show();

        }
    }

    public String getDistance(final double lat1, final double lon1, final double lat2, final double lon2){

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=driving");

                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("routes");
                    JSONObject routes = array.getJSONObject(0);
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject steps = legs.getJSONObject(0);
                    JSONObject distance = steps.getJSONObject("distance");
                    parsedDistance=distance.getString("text");

                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return parsedDistance;
    }

    public void crearMarcador(LatLng location, String titulo) {
//        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(titulo)
                .snippet(titulo));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().equals("Monu1")){
                    //Toast.makeText(getApplicationContext(),marker.getSnippet(),Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frameppal, new PruebaFragment())
                            .commit();
                } else if (marker.getTitle().equals("Monu2")) {
                    //Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
                    calcularDistancias();
                } else if (marker.getTitle().equals("Monu3")) {
                    Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
                } else if (marker.getTitle().equals("Monu4")) {
                    Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
                } else if (marker.getTitle().equals("Monu5")) {
                    Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Click en un marcador", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

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

                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getContext(), "GPS enabled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void agregarMarcador(double lat, double lon) {
        LatLng coordenadas = new LatLng(lat, lon);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        if (marcador != null) marcador.remove();
        marcador = mMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .title("Aquí estoy")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        //mMap.animateCamera(miUbicacion);
    }

    private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            //Toast.makeText(getContext(), "Latitud: " + lat + "Longitud: " + lon, Toast.LENGTH_SHORT).show();
            this.miUbicacion.setLatitude(lat);
            this.miUbicacion.setLongitude(lon);
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
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        actualizarUbicacion(location);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, locationListener);

    }

    /***************/

    public void crearMarkers() {
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

}
