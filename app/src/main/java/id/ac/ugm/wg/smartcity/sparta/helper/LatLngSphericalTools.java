package id.ac.ugm.wg.smartcity.sparta.helper;

import android.test.suitebuilder.annotation.LargeTest;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by redh4t on 18/03/16.
 */
public class LatLngSphericalTools {

    LatLng midLatLng;
    double R = 6371000;
    double Rarea = 0;

    ArrayList<LatLng> ArrayListLatLng;

    /**
     * Library of geodesy functions for operations on a spherical earth model.
     *
     * @module   latlon-spherical
     * @requires dms
     */

    /**
     * Creates a LatLon point on the earth's surface at the specified latitude / longitude.
     *
     * @constructor
     * @param {number} lat - Latitude in degrees.
     * @param {number} lon - Longitude in degrees.
     *
     * @example
     *     var p1 = new LatLon(52.205, 0.119);
     */

    public LatLngSphericalTools(){
    }

    public double getDistance(LatLng fromLatLng, LatLng toLatLng){
        double φ1 = Math.toRadians(fromLatLng.latitude);
        double φ2 = Math.toRadians(toLatLng.latitude);
        double λ1 = Math.toRadians(fromLatLng.longitude);
        double λ2 = Math.toRadians(toLatLng.longitude);

        double Δφ = φ2 - φ1;
        double Δλ = λ2 - λ1;

        double a = (Math.sin(Δφ/2) * Math.sin(Δφ/2))
                + (Math.cos(φ1) * Math.cos(φ2)
                * Math.sin(Δλ/2) * Math.sin(Δλ/2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = R * c;

        return d;
    }

    public LatLng getMidPoint(ArrayList<LatLng> arrayLatLng){
        this.ArrayListLatLng = arrayLatLng;

        double totalWeight = arrayLatLng.size();

        double x, y, z;

        double X = 0;
        double Y = 0;
        double Z = 0;

        for(int i=0; i<totalWeight; i++){
            LatLng latLng = arrayLatLng.get(i);
            x = Math.cos(Math.toRadians(latLng.latitude)) * Math.cos(Math.toRadians(latLng.longitude));
            y = Math.cos(Math.toRadians(latLng.latitude)) * Math.sin(Math.toRadians(latLng.longitude));
            z = Math.sin(Math.toRadians(latLng.latitude));

            X = X + x;
            Y = Y + y;
            Z = Z + z;
        }

        X = X / totalWeight;
        Y = Y / totalWeight;
        Z = Z / totalWeight;

        double longitude = Math.atan2(Y, X);
        double hyp = Math.sqrt((X * X) + (Y * Y));
        double latitude = Math.atan2(Z, hyp);

        latitude = Math.toDegrees(latitude);
        longitude = Math.toDegrees(longitude);

        midLatLng = new LatLng(latitude,longitude);

        return midLatLng;
    }

    public double getRangeMidPoint(){
        double Rmax = 0;

        for (int i=0; i<ArrayListLatLng.size(); i++){
            LatLng toLatLng = ArrayListLatLng.get(i);
            double Rtemp = this.getDistance(this.midLatLng, toLatLng);
            if (Rtemp>Rmax){
                Rmax = Rtemp;
            }
        }

        Rarea = Rmax;

        return Rarea;
    }
}
