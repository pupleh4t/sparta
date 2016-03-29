package id.ac.ugm.wg.smartcity.sparta.app;

import java.security.DomainCombiner;

/**
 * Created by HermawanRahmatHidaya on 24/01/2016.
 */
public class AppConfig {
    // Main Link
//    private static String URL_DOMAIN = "http://192.168.43.133/sparta3";
    private static String URL_DOMAIN = "http://smartcity.wg.ugm.ac.id/webapp/sparta3";

    // Link Login
    public static String URL_LOGIN = URL_DOMAIN + "/user/login";

    // Link Register
    public static String URL_REGISTER = URL_DOMAIN + "/user/register";

    // Link Get Lahan Data
    public static String URL_LAHAN_DATA = URL_DOMAIN + "/data/search/";

    // Link Get Slot Data
    public static String URL_SLOT_DATA = URL_DOMAIN + "/data/slot";

    // Link Get Area Data
    public static String URL_AREA_DATA = URL_DOMAIN + "/data/area";

    // Link PostLatLng
    public static String URL_POST_LATLNG = URL_DOMAIN + "/temp/latlng";

    // Link PostLatLng
    public static String URL_POST_LATLNG2 = URL_DOMAIN + "/temp/multilatlng";

}