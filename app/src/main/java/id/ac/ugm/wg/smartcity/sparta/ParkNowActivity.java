package id.ac.ugm.wg.smartcity.sparta;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.helper.DepartmentAutoCompleteAdapter;
import id.ac.ugm.wg.smartcity.sparta.helper.JsonMapper;
import id.ac.ugm.wg.smartcity.sparta.helper.LatLngSphericalTools;
import id.ac.ugm.wg.smartcity.sparta.helper.MapStateManager;
import id.ac.ugm.wg.smartcity.sparta.app.AppController;
import id.ac.ugm.wg.smartcity.sparta.widgets.DelayAutoCompleteTextView;
import id.ac.ugm.wg.smartcity.sparta.helper.GeoAutoCompleteAdapter;
import id.ac.ugm.wg.smartcity.sparta.helper.GeoSearchResult;

// ToDo : Fitur cari lokasi departement (AutoCompleteTextView dkk)

public class ParkNowActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAGi = "INFO";
    private GoogleMap mMap;
    private boolean startupMode = true;
    private boolean canZoom = true;
    private TextInputLayout TILplace, TILplace2;
    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView ETplace, ETplace2;
    private ImageButton btnSwap;

    private MapStateManager mgr;
    private float zoomTemp;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker marker, markerDraggable;
    private ProgressDialog pDialog;
    private ArrayList<HashMap<String,String>> slotList;
    private ArrayList<LatLng> AreaLatLngArrayList = new ArrayList<>();

    private HashMap<String,String> selectedData;
    private ArrayList<Marker> slotMarkerArrayList = new ArrayList<>();
    private ArrayList<Marker> departmentMarkerArrayList = new ArrayList<>();
    private ArrayList<HashMap<String,String>> tempDepartmentArrayList = new ArrayList<>();
    private HashMap<String,String> tempDepartmentHashMap = new HashMap<>();
    private Circle circleDepartment;

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private TextView TVHeaderDepartmentName, TVHeaderCarCapacityLecturers, TVHeaderCarCapacityStudents, TVHeaderDistance;
    private TextView TVBodyDepartmentName, TVBodyCarCapacityLecturers, TVBodyCarMaxCapacityLecturers, TVBodyOpenAt, TVBodyCloseAt;
    private FloatingActionButton fab, fabMetamorphose;

    private static String TAG_REQUEST_SLOT = "slot_request";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_now);

        Intent intent = getIntent();
        try {selectedData = (HashMap<String, String>)intent.getSerializableExtra("selectedLot");}
        catch (Exception e){e.printStackTrace();}

        fab = (FloatingActionButton) findViewById(R.id.fabPark);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Geocoder gc = new Geocoder(getApplicationContext());
                Address address = new Address(Locale.getDefault());
                address.setCountryName("id = 12");
                address.setLatitude(-7.765808);
                address.setLongitude(110.371645);
                address.setLocality("Vacant");
                setMarkerDraggable(address.getLocality(), address.getCountryName(), address.getLatitude(), address.getLongitude());

            }
        });

        fabMetamorphose = (FloatingActionButton) findViewById(R.id.fabChange);
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

        slidingUpPanelLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                fab.hide();
                fabMetamorphose.hide();
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    fab.show();
                    fabMetamorphose.show();
                }
            }
        });

        TILplace = (TextInputLayout) findViewById(R.id.TILplace);
        ETplace = (DelayAutoCompleteTextView) findViewById(R.id.ETplace);
        ETplace.setThreshold(THRESHOLD);
        ETplace.setAdapter(new GeoAutoCompleteAdapter(this));
        ETplace.setDropDownWidth(getResources().getDisplayMetrics().widthPixels);
        ETplace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) parent.getItemAtPosition(position);
                ETplace.setText(result.getAddress());
            }
        });
        ETplace.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    pDialog.setMessage("Searching . . .");
                    showDialog();
                    try {
                        Geocoder gc = new Geocoder(getApplicationContext());
                        List<Address> list = gc.getFromLocationName(ETplace.getText().toString(), 1);
                        if (list.get(0) == null) {
                            Toast.makeText(getApplicationContext(), "NULL RESULT", Toast.LENGTH_SHORT).show();
                        } else {
                            Address address = list.get(0);
                            LatLng userDefinedLocation = new LatLng(address.getLatitude(), address.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(userDefinedLocation, 18, 0, 0)));
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    hideDialog();
                }
                return false;
            }
        });

        TILplace2 = (TextInputLayout) findViewById(R.id.TILplace2);
        ETplace2 = (DelayAutoCompleteTextView) findViewById(R.id.ETplace2);
        ETplace2.setThreshold(THRESHOLD);
        ETplace2.setAdapter(new DepartmentAutoCompleteAdapter(this));
        ETplace2.setDropDownWidth(getResources().getDisplayMetrics().widthPixels);
        ETplace2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String result = (String)parent.getItemAtPosition(position);
                String departmentName = result;
                ETplace.setText(departmentName);
            }
        });
        ETplace2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    pDialog.setMessage("Searching . . .");
                    showDialog();
                    try {
                        String url = AppConfig.URL_LAHAN_DATA + ETplace2.getText();
                        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                hideDialog();
                                try {
                                    JSONArray jsonArray = response.getJSONArray("data");
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    Integer id_lahan = Integer.parseInt(jsonObject.getString(AppConfig.TAG_KEY_ID_LAHAN));
                                    Double lat = Double.parseDouble(jsonObject.getString(AppConfig.TAG_KEY_LONGITUDE));
                                    Double lng = Double.parseDouble(jsonObject.getString(AppConfig.TAG_KEY_LATITUDE));
                                    LatLng latLng = new LatLng(lat, lng);
                                    // ToDo: new case from ETplace2
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 18, 0, 0));
                                    mMap.animateCamera(cameraUpdate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                hideDialog();
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                error.printStackTrace();
                            }
                        });
                        AppController.getInstance().addToRequestQueue(req);

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    hideDialog();
                }
                return false;
            }
        });

        btnSwap = (ImageButton)findViewById(R.id.btnSwap);
        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TILplace.getVisibility()==View.VISIBLE){
                    TILplace.setVisibility(View.GONE);
                    TILplace2.setVisibility(View.VISIBLE);
                }
                else {
                    TILplace.setVisibility(View.VISIBLE);
                    TILplace2.setVisibility(View.GONE);
                }
            }
        });

        TVHeaderDepartmentName = (TextView)findViewById(R.id.TVDepartmentNameHeader);
        TVHeaderCarCapacityLecturers = (TextView)findViewById(R.id.TVCapacityLecturers);
        TVHeaderCarCapacityStudents = (TextView)findViewById(R.id.TVCapacityStudents);
        TVHeaderDistance = (TextView)findViewById(R.id.TVDistance);

        TVBodyDepartmentName = (TextView)findViewById(R.id.TVDepartmentName);
        TVBodyCarCapacityLecturers = (TextView)findViewById(R.id.TVFreeParkingSpace);
        TVBodyCarMaxCapacityLecturers = (TextView)findViewById(R.id.TVMaxParkingSpace);
        TVBodyOpenAt = (TextView)findViewById(R.id.TVOpenAt);
        TVBodyCloseAt = (TextView)findViewById(R.id.TVCloseAt);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if ((float) Math.round(cameraPosition.zoom) != zoomTemp) {
                    Toast.makeText(getApplicationContext(), new StringBuilder("Zoom Level : " + Math.round(cameraPosition.zoom)), Toast.LENGTH_SHORT).show();
                }
                zoomTemp = Math.round(cameraPosition.zoom);
                if (zoomTemp >= 19) {
                    // ToDo : Block Code if Zoomed mode
                    // Show All The slotMarkers
                    if (!canZoom) {
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                        return;
                    }
                    if (slotList.size() == 0 && !startupMode) {
                        getSlotMarker(Integer.valueOf(tempDepartmentHashMap.get(AppConfig.TAG_KEY_ID_LAHAN)));
                    }
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            return false;
                        }
                    });
                    mMap.setInfoWindowAdapter(new DetailInfoWindowAdapter());

                } else {
                    // ToDo : Block Code if Non Zoomed mode
                    if (slotList.size() > 0 || tempDepartmentArrayList == null) {
                        removeMarkerSlot();
                        circleDepartment.remove();
                        getSummaryMarkers();
                        if (canZoom) {
                            canZoom = false;
                        }
                    }
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }
                            for (int i = 0; i < departmentMarkerArrayList.size(); i++) {
                                if (marker.getTitle().equals(tempDepartmentArrayList.get(i).get(AppConfig.TAG_KEY_ALIAS))) {
                                    tempDepartmentHashMap = tempDepartmentArrayList.get(i);
                                    if (updateSlidingUpPanelLayout(tempDepartmentHashMap)) {
                                        canZoom = true;
                                        selectedData = null;
                                    }
                                }
                            }
                            return false;
                        }
                    });
                    mMap.setInfoWindowAdapter(null);
                }
            }
        });
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if(selectedData != null){
            Double lat = Double.valueOf(selectedData.get(AppConfig.TAG_KEY_LONGITUDE));
            Double lng = Double.valueOf(selectedData.get(AppConfig.TAG_KEY_LATITUDE));
            LatLng target = new LatLng(lat, lng);
            Float zoom = Float.valueOf("19");
            Float tilt = Float.valueOf("0");
            Float bearing = Float.valueOf("0");

            CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
        }else{
            LatLng target = new LatLng(0,0);
            Float zoom = Float.valueOf("0");
            Float tilt = Float.valueOf("0");
            Float bearing = Float.valueOf("0");

            CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
        }

        if(selectedData!=null){getSlotMarker(Integer.parseInt(selectedData.get(AppConfig.TAG_KEY_ID_LAHAN)));}
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

    private void getSummaryMarkers(){
        // ToDo : Selesaikan dulu bang
        showDialog();
        JsonObjectRequest getSummaryData = new JsonObjectRequest(AppConfig.URL_LAHAN_DATA, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for (int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = (JSONObject)jsonArray.get(i);
                        HashMap<String,String> tempHashMap = new HashMap<>();
                        tempHashMap.put(AppConfig.TAG_KEY_ID_LAHAN, jsonObject.getString(AppConfig.TAG_KEY_ID_LAHAN));
                        tempHashMap.put(AppConfig.TAG_KEY_DESKRIPSI, jsonObject.getString(AppConfig.TAG_KEY_DESKRIPSI));
                        tempHashMap.put(AppConfig.TAG_KEY_ALIAS, jsonObject.getString(AppConfig.TAG_KEY_ALIAS));
                        tempHashMap.put(AppConfig.TAG_KEY_LATITUDE, jsonObject.getString(AppConfig.TAG_KEY_LATITUDE));
                        tempHashMap.put(AppConfig.TAG_KEY_LONGITUDE, jsonObject.getString(AppConfig.TAG_KEY_LONGITUDE));
                        tempHashMap.put(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL, jsonObject.getString(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL));
                        tempHashMap.put(AppConfig.TAG_KEY_MAX_KAPASITAS_MOBIL, jsonObject.getString(AppConfig.TAG_KEY_MAX_KAPASITAS_MOBIL));
                        tempHashMap.put(AppConfig.TAG_KEY_MAX_KAPASITAS_MOTOR, jsonObject.getString(AppConfig.TAG_KEY_MAX_KAPASITAS_MOTOR));
                        tempHashMap.put(AppConfig.TAG_KEY_JAM_BUKA, jsonObject.getString(AppConfig.TAG_KEY_JAM_BUKA));
                        tempHashMap.put(AppConfig.TAG_KEY_JAM_TUTUP, jsonObject.getString(AppConfig.TAG_KEY_JAM_TUTUP));
                        tempHashMap.put(AppConfig.TAG_KEY_LINK_GAMBAR, jsonObject.getString(AppConfig.TAG_KEY_LINK_GAMBAR));

                        View view = getLayoutInflater().inflate(R.layout.marker_summary, null);
                        TextView TValias = (TextView) view.findViewById(R.id.TVAlias);
                        TValias.setText(tempHashMap.get(AppConfig.TAG_KEY_ALIAS));
                        MarkerOptions options = new MarkerOptions()
                                .title(tempHashMap.get(AppConfig.TAG_KEY_ALIAS))
                                .position(new LatLng(Double.valueOf(tempHashMap.get(AppConfig.TAG_KEY_LONGITUDE)), Double.valueOf(tempHashMap.get(AppConfig.TAG_KEY_LATITUDE))))
                                .icon(BitmapDescriptorFactory.fromBitmap(createMarkerDrawable(getApplicationContext(), view)))
                                .snippet(
                                        tempHashMap.get(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL)
                                                + " of "
                                                + tempHashMap.get(AppConfig.TAG_KEY_MAX_KAPASITAS_MOBIL)
                                                + " available");

                        departmentMarkerArrayList.add(mMap.addMarker(options));
                        tempDepartmentArrayList.add(tempHashMap);
                    }
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "JSON Data is corrupted", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                error.printStackTrace();
            }
        });
        AppController.getInstance().addToRequestQueue(getSummaryData);
    }

    private void setMarkerSlot(String status, double lat, double lng, String id){
        MarkerOptions options = new MarkerOptions()
                .title(id)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_logo_marker_car_green))
                .snippet(status);
        if(status.equals("VACANT")){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        slotMarkerArrayList.add(mMap.addMarker(options));
    }

    private void removeMarkerSlot(){
        for (int i = 0; i < slotMarkerArrayList.size(); i++){
            Marker marker = slotMarkerArrayList.get(i);
            marker.remove();
        }
        slotList.clear();
        slotMarkerArrayList.clear();
    }

    private void setCircleArea(double lat, double lng, double radius){
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat,lng))
                .radius(radius)
                .fillColor(getResources().getColor(R.color.circle_fill))
                .strokeWidth(0);

        circleDepartment = mMap.addCircle(circleOptions);
    }

    private void getSlotMarker(int id_lahan) {
        showDialog();
        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(AppConfig.TAG_KEY_ID_LAHAN, String.valueOf(id_lahan));
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_SLOT_DATA, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    hideDialog();
                    JSONArray slot_array = response.getJSONArray(AppConfig.TAG_SLOT_JSON_ARRAY);
                    for(int i=0; i<slot_array.length();i++){
                        JSONObject slotObj = slot_array.getJSONObject(i);
                        HashMap<String,String> slot = new HashMap<>();

                        slot.put(AppConfig.TAG_SLOT_ID, slotObj.getString(AppConfig.TAG_SLOT_ID));
                        slot.put(AppConfig.TAG_SLOT_LATITUDE, slotObj.getString(AppConfig.TAG_SLOT_LATITUDE));
                        slot.put(AppConfig.TAG_SLOT_LONGITUDE, slotObj.getString(AppConfig.TAG_SLOT_LONGITUDE));
                        slot.put(AppConfig.TAG_SLOT_STATUS, slotObj.getString(AppConfig.TAG_SLOT_STATUS));

                        slotList.add(slot);
                    }

                    Toast.makeText(getApplicationContext(), "Slot list : "+ slotList.size(), Toast.LENGTH_SHORT).show();
                    ArrayList<LatLng> arrayListLatLng = new ArrayList<>(slotList.size());

                    for (int i=0; i<slotList.size(); i++){
                        HashMap<String,String> slotData = slotList.get(i);
                        setMarkerSlot(
                                slotData.get(AppConfig.TAG_SLOT_STATUS),
                                Double.parseDouble(slotData.get(AppConfig.TAG_SLOT_LATITUDE)),
                                Double.parseDouble(slotData.get(AppConfig.TAG_SLOT_LONGITUDE)),
                                slotData.get(AppConfig.TAG_SLOT_ID)
                        );

                        LatLng latLng = new LatLng(
                                Double.parseDouble(slotData.get(AppConfig.TAG_SLOT_LATITUDE)),
                                Double.parseDouble(slotData.get(AppConfig.TAG_SLOT_LONGITUDE)
                        ));
                        arrayListLatLng.add(i, latLng);
                    }

                    LatLngSphericalTools latLngSphericalTools = new LatLngSphericalTools();
                    latLngSphericalTools.setArrayListLatLng(arrayListLatLng);
                    LatLng midPointLatLng = latLngSphericalTools.getMidPoint();
                    double midPointRange = latLngSphericalTools.getRangeMidPoint();

                    Toast.makeText(ParkNowActivity.this, new StringBuilder("The distance is " + String.valueOf(midPointRange)), Toast.LENGTH_SHORT).show();

                    setCircleArea(midPointLatLng.latitude, midPointLatLng.longitude, midPointRange);
                    startupMode=false;
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

    private boolean updateSlidingUpPanelLayout(HashMap<String, String> tempHashMap){
        TVHeaderDepartmentName.setText(tempHashMap.get(AppConfig.TAG_KEY_DESKRIPSI));
        String textTemp = tempHashMap.get(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL) + " OF " + tempHashMap.get(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL);
        TVHeaderCarCapacityLecturers.setText(textTemp);

        // Calculate Distance
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Access location permission is denied", Toast.LENGTH_SHORT).show();
            return false;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(lastLocation==null){
            TVHeaderDistance.setText("N/A");
            Toast.makeText(getApplicationContext(), "Location services are not enabled", Toast.LENGTH_SHORT).show();
        }
        else{
            Double latDestination = Double.valueOf(tempHashMap.get(AppConfig.TAG_KEY_LONGITUDE));
            Double lngDestination = Double.valueOf(tempHashMap.get(AppConfig.TAG_KEY_LATITUDE));
            LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            LatLng destinationLatLng = new LatLng(latDestination, lngDestination);
            LatLngSphericalTools latLngSphericalTools = new LatLngSphericalTools();
            latLngSphericalTools.setTwoLatLng(currentLatLng, destinationLatLng);
            double distanceInMeters = latLngSphericalTools.getDistance();
            if (distanceInMeters>1000){
                double distance = Math.round(distanceInMeters/1000 * 100.0) / 100.0;
                TVHeaderDistance.setText(new StringBuilder(String.valueOf(distance) + " KM"));
            }else{
                double distance = Math.round(distanceInMeters * 100.0) / 100.0;
                TVHeaderDistance.setText(new StringBuilder(String.valueOf(distance) + " M"));
            }
        }

        TVBodyDepartmentName.setText(tempHashMap.get(AppConfig.TAG_KEY_DESKRIPSI));
        TVBodyCarCapacityLecturers.setText(new StringBuilder(tempHashMap.get(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL) + " slots available"));
        TVBodyCarMaxCapacityLecturers.setText(new StringBuilder(tempHashMap.get(AppConfig.TAG_KEY_MAX_KAPASITAS_MOBIL) + " slots capacity"));
        TVBodyOpenAt.setText(new StringBuilder(tempHashMap.get(AppConfig.TAG_KEY_JAM_BUKA).substring(0, 5) + " Jakarta Time"));
        TVBodyCloseAt.setText(new StringBuilder(tempHashMap.get(AppConfig.TAG_KEY_JAM_TUTUP).substring(0, 5) + " Jakarta Time"));

        return true;
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


    class DetailInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
        private final View mWindow;

        DetailInfoWindowAdapter(){
            mWindow = getLayoutInflater().inflate(R.layout.info_window_detail, null);
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
            TextView TVId_lahan = (TextView) v.findViewById(R.id.TVId_slot);
            TextView TVStatus = (TextView) v.findViewById(R.id.TVStatus);
            RelativeLayout RLRight = (RelativeLayout) v.findViewById(R.id.RLRight);

            TVId_lahan.setText(marker.getTitle());
            TVStatus.setText(marker.getSnippet());
            if(marker.getSnippet().equals("VACANT")){
                RLRight.setBackground(getResources().getDrawable(R.drawable.ic_balloon_circle3));
            }
            else {
                RLRight.setBackground(getResources().getDrawable(R.drawable.ic_balloon_circle2));
            }
        }
    }

    private Bitmap createMarkerDrawable (Context context, View v){
        if (v.getMeasuredHeight() <= 0) {
            v.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.draw(c);
            return b;
        }
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }
}
