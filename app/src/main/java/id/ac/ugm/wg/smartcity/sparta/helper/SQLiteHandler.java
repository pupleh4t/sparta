package id.ac.ugm.wg.smartcity.sparta.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HermawanRahmatHidaya on 25/01/2016.
 */
public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static Variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "smartparkdb";

    // Login table name
    private static final String TABLE_USER = "usercredential";

    // Login Table Columns Names
    private static final String KEY_ID = "id_user";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "unique_id";
    private static final String KEY_CREATED_AT= "created_at";

    // Database Name
    private static final String TABLE_LAHAN = "lahanparkir";

    // Lahan Table Columns Names
    private static final String KEY_ID_LAHAN = "id_lahan";
    private static final String KEY_DESKRIPSI = "deskripsi";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_MAX_KAPASITAS_MOBIL = "max_kapasitas_mobil";
    private static final String KEY_MAX_KAPASITAS_MOTOR = "max_kapasitas_motor";
    private static final String KEY_JAM_BUKA = "jam_buka";
    private static final String KEY_JAM_TUTUP = "jam_tutup";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_LAHAN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LAHAN + "("
                + KEY_ID_LAHAN + " INTEGER PRIMARY KEY, "
                + KEY_DESKRIPSI + " TEXT, "
                + KEY_LATITUDE + " DOUBLE, "
                + KEY_LONGITUDE + " DOUBLE, "
                + KEY_MAX_KAPASITAS_MOBIL + " INTEGER, "
                + KEY_MAX_KAPASITAS_MOTOR + " INTEGER, "
                + KEY_JAM_BUKA + " TEXT, "
                + KEY_JAM_TUTUP + "TEXT"
                + ")";
        db.execSQL(CREATE_LAHAN_TABLE);
        Log.d(TAG, "Database tables created");
    }

    // Upgrading Database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older database if existed
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_LAHAN);

        // Creates Tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);
        values.put(KEY_UID, uid);
        values.put(KEY_CREATED_AT, created_at);

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close();

        Log.d(TAG, "New user inserted into SQLite: " + id);
    }

    /**
     * Getting user from database
     */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM "+TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to First Row
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // Return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Recreate database Delete all tables and create them again
     */
    public void deleteUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete all rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from SQLite");
    }

    public void addLahan(HashMap<String, String> dataLahan){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID_LAHAN, dataLahan.get(KEY_ID_LAHAN));
        contentValues.put(KEY_DESKRIPSI, dataLahan.get(KEY_DESKRIPSI));
        contentValues.put(KEY_LATITUDE, dataLahan.get(KEY_LATITUDE));
        contentValues.put(KEY_LONGITUDE, dataLahan.get(KEY_LONGITUDE));
        contentValues.put(KEY_MAX_KAPASITAS_MOBIL, dataLahan.get(KEY_MAX_KAPASITAS_MOBIL));
        contentValues.put(KEY_MAX_KAPASITAS_MOTOR, dataLahan.get(KEY_MAX_KAPASITAS_MOTOR));
        contentValues.put(KEY_JAM_BUKA, dataLahan.get(KEY_JAM_BUKA));
        contentValues.put(KEY_JAM_TUTUP, dataLahan.get(KEY_JAM_TUTUP));

        long id = db.insert(TABLE_LAHAN, null, contentValues);
        db.close();

        // Log into debug level
        Log.d(TAG, "New data lahan inserted into SQLite : ");
    }

    public HashMap<String, String> getLahan(int id_lahan){
        HashMap<String, String> result = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LAHAN + " WHERE " + KEY_ID_LAHAN + " = " + id_lahan;

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        result.put(KEY_ID_LAHAN, cursor.getString(0));
        result.put(KEY_DESKRIPSI, cursor.getString(1));
        result.put(KEY_LATITUDE, cursor.getString(2));
        result.put(KEY_LONGITUDE, cursor.getString(3));
        result.put(KEY_MAX_KAPASITAS_MOBIL, cursor.getString(4));
        result.put(KEY_MAX_KAPASITAS_MOTOR, cursor.getString(5));
        result.put(KEY_JAM_BUKA, cursor.getString(6));
        result.put(KEY_JAM_TUTUP, cursor.getString(7));

        cursor.close();
        db.close();

        Log.d(TAG, "Fetching lahanparkir from SQLite : " + result.toString());

        return result;
    }

    public void deleteLahan(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LAHAN, null, null);
        db.close();

        Log.d(TAG, "Deleting table Lahanparkir from SQLiteDatabase");
    }
}
