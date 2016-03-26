package id.ac.ugm.wg.smartcity.sparta;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.helper.LatLngSphericalTools;
import id.ac.ugm.wg.smartcity.sparta.helper.MapStateManager;
import id.ac.ugm.wg.smartcity.sparta.app.AppController;

public class ParkNowActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAGi = "INFO";
    private GoogleMap mMap;
    private TextInputLayout TILplace;
    private EditText ETplace;
    private MapStateManager mgr;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker marker, markerDraggable;
    private ProgressDialog pDialog;
    private ArrayList<HashMap<String,String>> slotList;
    private ArrayList<LatLng> AreaLatLngArrayList = new ArrayList<>();

    private static String TAG_SLOT_JSON_ARRAY = "slot_data";
    private static String TAG_SLOT_ID = "id_slot";
    private static String TAG_SLOT_STATUS = "status";
    private static String TAG_SLOT_LATITUDE = "latitude";
    private static String TAG_SLOT_LONGITUDE = "longitude";

    private static String TAG_AREA_JSON_ARRAY = "area_data";
    private static String TAG_AREA_LATITUDE = "latitude";
    private static String TAG_AREA_LONGITUDE = "longitude";
    private int temp_id_lahan = 0;

    private static String TAG_ID_LAHAN = "id_lahan";

    private static String TAG_REQUEST_SLOT = "slot_request";

    private static String TAG_BALLOON_ID = "ID : ";
    private static String TAG_BALLOON_LATITUDE = "Latitude : ";
    private static String TAG_BALLOON_LONGITUDE = "Longitude : ";

    private Boolean statusPostLatLng = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_now);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Geocoder gc = new Geocoder(getApplicationContext());
                Address address = new Address(Locale.getDefault());
                address.setCountryName("id = 12");
                address.setLatitude(-7.765808);
                address.setLongitude(110.371645);
                address.setLocality("Vacant");
                setMarkerDraggable(address.getLocality(),address.getCountryName(),address.getLatitude(),address.getLongitude());
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Submit Now", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                LatLng LatLngToBePosted = markerDraggable.getPosition();
                PostLatLng(LatLngToBePosted);
                if(statusPostLatLng==true){
                    SnackBarToast(view, "Success Post LatLng");
                }
                else{
                    SnackBarToast(view, "Failed Post LatLng");
                }
            }
        });
        FloatingActionButton fabMetamorphose = (FloatingActionButton) findViewById(R.id.fabChange);
        fabMetamorphose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mMap.getMapType()) {
                    case GoogleMap.MAP_TYPE_NORMAL:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        PopUpSnackBarMetamorphose(view, "Satellite Map");
                        break;
                    case GoogleMap.MAP_TYPE_SATELLITE:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        PopUpSnackBarMetamorphose(view, "Hybrid Map");
                        break;
                    case GoogleMap.MAP_TYPE_HYBRID:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        PopUpSnackBarMetamorphose(view, "Normal Map");
                        break;
                }
            }
        });
        FloatingActionButton fabCurrentLocation = (FloatingActionButton) findViewById(R.id.fabLocateMe);
        fabCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCurrentLocation(view);
            }
        });

        TILplace = (TextInputLayout) findViewById(R.id.TILplace);
        ETplace = (EditText) findViewById(R.id.ETplace);

        ETplace.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    pDialog.setMessage("Searching . . .");
                    showDialog();
                    try {
                        Geocoder gc = new Geocoder(getApplicationContext());
                        List<Address> list = gc.getFromLocationName(ETplace.getText().toString(), 1);
                        if (list == null) {
                            Toast.makeText(getApplicationContext(), "NULL RESULT", Toast.LENGTH_SHORT).show();
                        } else {
                            Address address = list.get(0);
                            String locality = address.getLocality();
                            String country = address.getCountryName();
                            Toast.makeText(getApplicationContext(), locality, Toast.LENGTH_SHORT).show();
                            Log.d("Deb", locality);
                            LatLng userDefinedLocation = new LatLng(address.getLatitude(), address.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(userDefinedLocation));

                            setMarkerUser(locality, country, address.getLatitude(), address.getLongitude());
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    hideDialog();
                }
                return false;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        slotList = new ArrayList<>();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        mgr = new MapStateManager(this);
        mgr.saveMapState(mMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mgr = new MapStateManager(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setInfoWindowAdapter(new ParkNowInfoWindowAdapter());
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng ll = marker.getPosition();
                marker.setTitle("UPDATED");
                marker.showInfoWindow();
            }
        });
        mgr = new MapStateManager(this);
        mMap.setMapType(mgr.getMapType());
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mgr.getLatLng()));
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
        }

        getDrawPolygonMarkers(1);
        getSlotMarker(1);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Access location permission is denied", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Toast.makeText(getApplicationContext(), "Connected to Location Service", Toast.LENGTH_SHORT).show();
        Log.i(TAGi, "GoogleApiClient connection has been connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAGi, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAGi, "Location Received : " + location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAGi, "GoogleApiClient connection has failed");
    }

    public void PopUpSnackBarMetamorphose(View view, String TextPopUp) {
        Snackbar snackbar = Snackbar.make(view, TextPopUp, Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (mMap.getMapType()) {
                            case GoogleMap.MAP_TYPE_NORMAL:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            case GoogleMap.MAP_TYPE_SATELLITE:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case GoogleMap.MAP_TYPE_HYBRID:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                        }
                    }
                });
        snackbar.setActionTextColor(Color.YELLOW);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void SnackBarToast(View view, String ToastMessage){
        Snackbar snackbar = Snackbar.make(view, ToastMessage, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void goToCurrentLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Access location permission is denied", Toast.LENGTH_SHORT).show();
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation==null){
            SnackBarToast(view, "Current location is unavailable");
        }
        else{
            pDialog.setMessage("Searching . . .");
            showDialog();
            LatLng currentlatlng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            try{
                Geocoder gc = new Geocoder(getApplicationContext());
                List<Address> addressListUser = gc.getFromLocation(currentlatlng.latitude, currentlatlng.longitude, 1);
                Address addressUser = addressListUser.get(0);
                String locality = addressUser.getLocality();
                String country = addressUser.getCountryName();

//                setMarkerDraggable(locality, country, currentlatlng.latitude, currentlatlng.longitude);
                setMarkerUser(locality, country, currentlatlng.latitude, currentlatlng.longitude);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentlatlng, 17);
                mMap.animateCamera(cameraUpdate);
            }catch (IOException e){
                SnackBarToast(view, e.getMessage());
            }
            hideDialog();
        }
    }

    private void setMarkerUser(String locality, String country, double lat, double lng) {
        if(marker!=null){
            marker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        if (country!=null){
            options.snippet(country);
        }

        marker = mMap.addMarker(options);
    }

    private void setMarkerDraggable(String locality, String country, double lat, double lng) {
        if(markerDraggable!=null){
            markerDraggable.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng))
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        if (country!=null){
            options.snippet(country);
        }

        markerDraggable = mMap.addMarker(options);
    }

    private void setMarkerSlot(String status, double lat, double lng, String id){
        
        MarkerOptions options = new MarkerOptions()
                .title(status)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_logo_marker))
                .snippet(TAG_BALLOON_ID + id);
        if(status.equals("VACANT")){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        mMap.addMarker(options);
    }

    private void setMarkerArea(double lat, double lng, double radius, String title, String snippet){

        MarkerOptions options = new MarkerOptions()
                .title(title)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .snippet(snippet);

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat,lng))
                .radius(radius)
                .fillColor(getResources().getColor(R.color.circle_fill))
                .strokeWidth(0);

        mMap.addMarker(options);
        mMap.addCircle(circleOptions);
    }

    private void getSlotMarker(int id_lahan) {
        showDialog();
        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TAG_ID_LAHAN, String.valueOf(id_lahan));
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_SLOT_DATA, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    hideDialog();
                    JSONArray slot_array = response.getJSONArray(TAG_SLOT_JSON_ARRAY);
                    for(int i=0; i<slot_array.length();i++){
                        JSONObject slotObj = slot_array.getJSONObject(i);
                        HashMap<String,String> slot = new HashMap<>();

                        slot.put(TAG_SLOT_ID, slotObj.getString(TAG_SLOT_ID));
                        slot.put(TAG_SLOT_LATITUDE, slotObj.getString(TAG_SLOT_LATITUDE));
                        slot.put(TAG_SLOT_LONGITUDE, slotObj.getString(TAG_SLOT_LONGITUDE));
                        slot.put(TAG_SLOT_STATUS, slotObj.getString(TAG_SLOT_STATUS));

                        slotList.add(slot);
                    }

                    Toast.makeText(getApplicationContext(), "Slot list : "+ slotList.size(), Toast.LENGTH_SHORT).show();
                    ArrayList<LatLng> arrayListLatLng = new ArrayList<>(slotList.size());

                    for (int i=0; i<slotList.size(); i++){
                        HashMap<String,String> slotData = slotList.get(i);
                        setMarkerSlot(
                                slotData.get(TAG_SLOT_STATUS),
                                Double.parseDouble(slotData.get(TAG_SLOT_LATITUDE)),
                                Double.parseDouble(slotData.get(TAG_SLOT_LONGITUDE)),
                                slotData.get(TAG_SLOT_ID)
                        );

                        LatLng latLng = new LatLng(
                                Double.parseDouble(slotData.get(TAG_SLOT_LATITUDE)),
                                Double.parseDouble(slotData.get(TAG_SLOT_LONGITUDE)
                        ));
                        arrayListLatLng.add(i, latLng);
                    }

                    LatLngSphericalTools latLngSphericalTools = new LatLngSphericalTools();
                    latLngSphericalTools.setArrayListLatLng(arrayListLatLng);
                    LatLng midPointLatLng = latLngSphericalTools.getMidPoint();
                    double midPointRange = latLngSphericalTools.getRangeMidPoint();

                    Toast.makeText(ParkNowActivity.this, new StringBuilder("The distance is " + String.valueOf(midPointRange)), Toast.LENGTH_SHORT).show();

                    setMarkerArea(midPointLatLng.latitude, midPointLatLng.longitude, midPointRange, "DTETi", "Free Parking Available");
                }
                catch (JSONException e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(req, TAG_REQUEST_SLOT);
    }

    private void PostLatLng(LatLng latLng){
        showDialog();
        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("latitude", String.valueOf(latLng.latitude));
        params.put("longitude", String.valueOf(latLng.longitude));
        JsonObjectRequest PostLatLng = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_POST_LATLNG, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    hideDialog();
                    Boolean statusResponse = response.getBoolean("status");
                    statusPostLatLng = statusResponse;
                    if(statusResponse==true){Toast.makeText(getApplicationContext(), "Success Upload LatLng", Toast.LENGTH_SHORT).show();}
                    else{Toast.makeText(getApplicationContext(), "Failed to Upload LatLng", Toast.LENGTH_SHORT).show();}
                }
                catch (JSONException e){
                    hideDialog();
                    e.printStackTrace();
                    statusPostLatLng = false;
                    Toast.makeText(getApplicationContext(), "Failed to Upload LatLng", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(), "Failed to Upload LatLng", Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(PostLatLng);
    }

    private void getDrawPolygonMarkers(int id_lahan){
        showDialog();
        temp_id_lahan=0;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TAG_ID_LAHAN, String.valueOf(id_lahan));
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_AREA_DATA, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    hideDialog();
                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                    temp_id_lahan = response.getInt("id_lahan");
                    JSONArray area_data_array = response.getJSONArray(TAG_AREA_JSON_ARRAY);
                    for (int i=0; i < area_data_array.length(); i++){
                        JSONObject area_data = area_data_array.getJSONObject(i);
                        Double temp_lat = Double.valueOf(area_data.getString(TAG_AREA_LATITUDE));
                        Double temp_lng = Double.valueOf(area_data.getString(TAG_AREA_LONGITUDE));
                        LatLng temp_latlng = new LatLng(temp_lat, temp_lng);
                        AreaLatLngArrayList.add(temp_latlng);
                    }
                    if(AreaLatLngArrayList!=null){
                        Toast.makeText(getApplicationContext(), "Area data downloaded successfully", Toast.LENGTH_SHORT).show();
                        drawPolygon(getResources().getColor(R.color.polygon_fill), getResources().getColor(R.color.polygon_border), AreaLatLngArrayList);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Area data failed to be downloaded", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(req, TAG_REQUEST_SLOT);
    }

    private void drawPolygon(int fillColor, int strokeColor, ArrayList<LatLng> AreaLatLngArrayList){
        PolygonOptions polygonOptions = new PolygonOptions()
                .fillColor(fillColor)
                .strokeColor(strokeColor)
                .strokeWidth(3);

        for (int i = 0; i<AreaLatLngArrayList.size(); i++){
            polygonOptions.add(AreaLatLngArrayList.get(i));
        }

        Polygon shape = mMap.addPolygon(polygonOptions);
    }

    private void showDialog(){
        if(!pDialog.isShowing()){
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    class ParkNowInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
        private final View mWindow;
        private final View mContents;

        ParkNowInfoWindowAdapter(){
            mWindow = getLayoutInflater().inflate(R.layout.info_window_balloon, null);
            mContents = getLayoutInflater().inflate(R.layout.info_contents_balloon, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View v) {
            TextView tvTitle = (TextView)v.findViewById(R.id.TVtitle);
            TextView tvLat = (TextView)v.findViewById(R.id.TVlatitude);
            TextView tvLng = (TextView)v.findViewById(R.id.TVlongitude);
            TextView tvSnip = (TextView)v.findViewById(R.id.TVsnippet);

            LatLng latlng = marker.getPosition();

            tvTitle.setText(marker.getTitle());
            tvLat.setText(new StringBuilder(TAG_BALLOON_LATITUDE + latlng.latitude));
            tvLng.setText(new StringBuilder(TAG_BALLOON_LONGITUDE + latlng.longitude));
            tvSnip.setText(marker.getSnippet());
        }
    }
}
