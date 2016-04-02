package id.ac.ugm.wg.smartcity.sparta.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by root on 28/02/16.
 */
public class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "maptype";
    private static final String PREFS_NAME = "mapCameraState";
    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context){
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveMapState(GoogleMap map){
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = map.getCameraPosition();

        editor.putFloat(LATITUDE, (float)position.target.latitude);
        editor.putFloat(LONGITUDE, (float)position.target.longitude);
        editor.putFloat(ZOOM, position.zoom);
        editor.putFloat(TILT, position.tilt);
        editor.putFloat(BEARING, position.bearing);
        editor.putInt(MAPTYPE, map.getMapType());

        editor.commit();

        Log.d("DEB", "SAVED MAP STATE");
    }

    public CameraPosition getSavedCameraPosition(){
        double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
        if (latitude == 0){
            return null;
        }
        double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
        LatLng target  = new LatLng(latitude, longitude);
        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);

        CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
        return position;
    }

    public LatLng getLatLng(){
        double latitude = (double)mapStatePrefs.getFloat(LATITUDE,0);
        double longitude = (double)mapStatePrefs.getFloat(LONGITUDE,0);

        Log.d("DEBUGSS", String.valueOf(latitude));
        Log.d("DEBUGSS", String.valueOf(longitude));

        return new LatLng(latitude,longitude);
    }

    public int getMapType(){
        return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
    }
}
