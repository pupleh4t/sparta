package id.ac.ugm.wg.smartcity.sparta.app;

import java.security.DomainCombiner;

/**
 * Created by HermawanRahmatHidaya on 24/01/2016.
 */
public class AppConfig {
    // Main Link
    private static String URL_DOMAIN = "http://smartcity.wg.ugm.ac.id/webapp/sparta3";

    // Link Login
    public static String URL_LOGIN = URL_DOMAIN + "/user/login";

    // Link Register
    public static String URL_REGISTER = URL_DOMAIN + "/user/register";

    // Link Get Lahan Data
    public static String URL_LAHAN_DATA = URL_DOMAIN + "/data/search/";

    // Link Get Slot Data
    public static String URL_SLOT_DATA = URL_DOMAIN + "/data/slot";

    // Link PostLatLng
    public static String URL_POST_LATLNG = URL_DOMAIN + "/temp/latlng";

    // Link PostLatLng
    public static String URL_POST_LATLNG2 = URL_DOMAIN + "/temp/multilatlng";

    // Identifier JSON variables
    public static final String TAG_KEY_ID_LAHAN = "id_lahan";
    public static final String TAG_KEY_DESKRIPSI = "deskripsi";
    public static final String TAG_KEY_ALIAS = "alias";
    public static final String TAG_KEY_LATITUDE = "latitude";
    public static final String TAG_KEY_LONGITUDE = "longitude";
    public static final String TAG_KEY_SISA_KAPASITAS_MOBIL = "sisa_kapasitas_mobil";
    //public static final String TAG_KEY_SISA_KAPASITAS_MOTOR = "sisa_kapasitas_mobil";
    public static final String TAG_KEY_MAX_KAPASITAS_MOBIL = "max_kapasitas_mobil";
    public static final String TAG_KEY_MAX_KAPASITAS_MOTOR = "max_kapasitas_motor";
    public static final String TAG_KEY_JAM_BUKA = "jam_buka";
    public static final String TAG_KEY_JAM_TUTUP = "jam_tutup";
    public static final String TAG_KEY_LINK_GAMBAR = "link_gambar";

    public static String TAG_SLOT_JSON_ARRAY = "slot_data";
    public static String TAG_SLOT_ID = "id_slot";
    public static String TAG_SLOT_STATUS = "status";
    public static String TAG_SLOT_LATITUDE = "latitude";
    public static String TAG_SLOT_LONGITUDE = "longitude";

}