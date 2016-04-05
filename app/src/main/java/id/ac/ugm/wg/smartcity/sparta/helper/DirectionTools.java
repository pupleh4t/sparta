package id.ac.ugm.wg.smartcity.sparta.helper;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HermawanRahmatHidaya on 03/04/2016.
 */
public class DirectionTools {
    public String makeURL (LatLng sourceLatLng, LatLng destLatLng, String key){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourceLatLng.latitude));
        urlString.append(",");
        urlString.append(Double.toString(sourceLatLng.longitude));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destLatLng.latitude));
        urlString.append(",");
        urlString.append(Double.toString(destLatLng.longitude));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key="+ key);
        return urlString.toString();
    }

    public List<List<HashMap<String, String>>> parseRoutes(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = jRoutes.getJSONObject(i).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = jLegs.getJSONObject(j).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = jSteps.getJSONObject(k).getJSONObject("polyline").getString("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(list.get(l).latitude));
                            hm.put("lng", Double.toString(list.get(l).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    public List<List<HashMap<String,String>>> parseIntructions(JSONObject jObject) {
        List<List<HashMap<String,String>>> instructions = new ArrayList<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = jRoutes.getJSONObject(i).getJSONArray("legs");
                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    List<HashMap<String,String>> path = new ArrayList<>();
                    jSteps = jLegs.getJSONObject(j).getJSONArray("steps");
                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put("instruction", jSteps.getJSONObject(k).getString("html_instructions"));
                        hashMap.put("duration", jSteps.getJSONObject(k).getJSONObject("duration").getString("text"));
                        hashMap.put("distance", jSteps.getJSONObject(k).getJSONObject("distance").getString("text"));
                        path.add(hashMap);
                    }
                    instructions.add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instructions;
    }

    public List<HashMap<String, String>> parseDistanceDuration(JSONObject jObject) {
        List<HashMap<String, String>> instructions = new ArrayList<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = jRoutes.getJSONObject(i).getJSONArray("legs");
                for (int j = 0; j < jLegs.length(); j++){
                    HashMap<String,String> legs = new HashMap<>();
                    legs.put("distance", jLegs.getJSONObject(j).getJSONObject("distance").getString("text"));
                    legs.put("duration", jLegs.getJSONObject(j).getJSONObject("duration").getString("text"));
                    instructions.add(legs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instructions;
    }



    /**
     * Method Courtesy :
     * jeffreysambells.com/2010/05/27
     * /decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
