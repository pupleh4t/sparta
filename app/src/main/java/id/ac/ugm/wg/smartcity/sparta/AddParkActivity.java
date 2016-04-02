package id.ac.ugm.wg.smartcity.sparta;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.microedition.khronos.egl.EGLDisplay;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.app.AppController;
import id.ac.ugm.wg.smartcity.sparta.helper.MapStateManager;

public class AddParkActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MapStateManager mgr;
    private float zoomTemp = 0;
    private ArrayList<Marker> markers;
    private Marker userMarker;
    private ProgressDialog pDialog;

    private Button submitButton;
    private EditText ETid_lahan;
    private EditText ETpoints;

    private static String TAG_BALLOON_ID = "ID : ";
    private static String TAG_BALLOON_LATITUDE = "Latitude : ";
    private static String TAG_BALLOON_LONGITUDE = "Longitude : ";

//    private SlidingUpPanelLayout slidingLayout;
//    private TextView HeaderTVDepartmentName, HeaderTVSpaceLecturers, HeaderTVStudents, HeaderTVDistance;
//    private TextView BodyTVDepartmentName, BodyTVFreeParkingSpace, BodyTVMaxParkingSpace, BodyTVOpenAt, BodyTVCloseAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_park);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
//        HeaderTVDepartmentName = (TextView)findViewById(R.id.TVDepartmentNameHeader);
//        HeaderTVSpaceLecturers = (TextView)findViewById(R.id.TVCapacityLecturers);
//        HeaderTVStudents = (TextView)findViewById(R.id.TVCapacityStudents);
//        HeaderTVDistance = (TextView)findViewById(R.id.TVDistance);
//        BodyTVDepartmentName = (TextView)findViewById(R.id.TVDepartmentName);
//        BodyTVFreeParkingSpace = (TextView)findViewById(R.id.TVFreeParkingSpace);
//        BodyTVMaxParkingSpace = (TextView)findViewById(R.id.TVMaxParkingSpace);
//        BodyTVOpenAt = (TextView)findViewById(R.id.TVOpenAt);
//        BodyTVCloseAt = (TextView)findViewById(R.id.TVCloseAt);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fabMetamorphose = (FloatingActionButton) findViewById(R.id.fabChange);
        if (fabMetamorphose != null) {
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
        }
        FloatingActionButton fabCurrentLocation = (FloatingActionButton) findViewById(R.id.fabLocateMe);
        if (fabCurrentLocation != null) {
            fabCurrentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToCurrentLocation(view);
                }
            });
        }

        ETid_lahan = (EditText)findViewById(R.id.ETid);
        ETpoints = (EditText)findViewById(R.id.ETpoints);

        submitButton = (Button)findViewById(R.id.BTNSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp1 = ETid_lahan.getText().toString();
                String temp2 = ETpoints.getText().toString();
                int id_lahan = Integer.parseInt(temp1);
                int points = Integer.parseInt(temp2);
                if (id_lahan < 1) {
                    Toast.makeText(getApplicationContext(), "ID must be higher than 0", Toast.LENGTH_SHORT).show();
                } else if (points < 2) {
                    Toast.makeText(getApplicationContext(), "Points must be higher than 1", Toast.LENGTH_SHORT).show();
                } else {
                    submitMarkers(id_lahan, points);
                }
            }
        });

        pDialog = new ProgressDialog(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

//        slidingLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//
//            }
//
//            @Override
//            public void onPanelCollapsed(View panel) {
//
//            }
//
//            @Override
//            public void onPanelExpanded(View panel) {
//
//            }
//
//            @Override
//            public void onPanelAnchored(View panel) {
//
//            }
//
//            @Override
//            public void onPanelHidden(View panel) {
//
//            }
//        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        markers = new ArrayList<>();
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Access Location Permission Failed", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setInfoWindowAdapter(new AddParkInfoWindowAdapter());
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.showInfoWindow();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (markers.size() > 1) {
                    removeMarkers();
                }
                setMarker(latLng);
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
    }

    @Override
    public void onLocationChanged(Location location) {

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
        Log.i("INFO", "GoogleApiClient connection has been connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("INFO", "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("INFO", "GoogleApiClient connection has failed");
    }

    private void setMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker());
        if(markers.size()<1){
            markerOptions.title("Start Marker");
        }
        else{
            markerOptions.title("End Marker");
        }
        markers.add(mMap.addMarker(markerOptions));
    }

    private void removeMarkers(){
        for(Marker marker:markers){
            marker.remove();
        }
        markers.clear();
    }


    class AddParkInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
        private final View mWindow;
        private final View mContents;

        AddParkInfoWindowAdapter(){
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
            LatLng currentlatlng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .position(currentlatlng)
                    .title("Current Location");
            userMarker = mMap.addMarker(markerOptions);
            try{
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentlatlng, 25);
                mMap.animateCamera(cameraUpdate);
            }catch (Exception e){
                SnackBarToast(view, e.getMessage());
            }
        }
    }

    private void submitMarkers(int id_lahan, int numPoints) {
        pDialog.setMessage("Uploading");
        showDialog();

        HashMap<String,String> params = new HashMap<>();
        params.put("id_lahan", String.valueOf(id_lahan));
        params.put("fromLatitude", String.valueOf(markers.get(0).getPosition().latitude));
        params.put("fromLongitude", String.valueOf(markers.get(0).getPosition().longitude));
        params.put("toLatitude", String.valueOf(markers.get(1).getPosition().latitude));
        params.put("toLongitude", String.valueOf(markers.get(1).getPosition().longitude));
        params.put("numPoints",String.valueOf(numPoints));

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_POST_LATLNG2, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hideDialog();
                    boolean error = response.getBoolean("error");
                    String error_msg = response.getString("error_msg");
                    if (!error){
                        Toast.makeText(getApplicationContext(), "Success upload", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Error : " + response.getString("error_msg"), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(req);
    }

    public void SnackBarToast(View view, String ToastMessage){
        Snackbar snackbar = Snackbar.make(view, ToastMessage, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
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
}
