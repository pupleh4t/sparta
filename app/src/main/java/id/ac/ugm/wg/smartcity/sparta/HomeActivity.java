package id.ac.ugm.wg.smartcity.sparta;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.InstrumentationInfo;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.app.AppController;
import id.ac.ugm.wg.smartcity.sparta.helper.SQLiteHandler;
import id.ac.ugm.wg.smartcity.sparta.helper.SessionManager;
import id.ac.ugm.wg.smartcity.sparta.widgets.CustomGridView;
import id.ac.ugm.wg.smartcity.sparta.helper.GridAdapter;

public class HomeActivity extends AppCompatActivity {
    CollapsingToolbarLayout collapsingToolbar;
    CustomGridView gridView;
    TextView TVNama, TVEmail;
    ArrayList<HashMap<String,String>> parkingLotArrayList;
    SessionManager session;
    SQLiteHandler db;
    private ProgressDialog pDialog;

    private static final String TAG_KEY_ID_LAHAN = "id_lahan";
    private static final String TAG_KEY_DESKRIPSI = "deskripsi";
    private static final String TAG_KEY_ALIAS = "alias";
    private static final String TAG_KEY_LATITUDE = "latitude";
    private static final String TAG_KEY_LONGITUDE = "longitude";
    private static final String TAG_KEY_SISA_KAPASITAS_MOBIL = "sisa_kapasitas_mobil";
    //private static final String TAG_KEY_SISA_KAPASITAS_MOTOR = "sisa_kapasitas_mobil";
    private static final String TAG_KEY_MAX_KAPASITAS_MOBIL = "max_kapasitas_mobil";
    private static final String TAG_KEY_MAX_KAPASITAS_MOTOR = "max_kapasitas_motor";
    private static final String TAG_KEY_JAM_BUKA = "jam_buka";
    private static final String TAG_KEY_JAM_TUTUP = "jam_tutup";
    private static final String TAG_KEY_LINK_GAMBAR = "link_gambar";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        gridView = (CustomGridView)findViewById(R.id.gridview);
        gridView.setExpanded(true);

        getParkLotData();

        collapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.transparent));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddParkActivity.class);
                startActivity(intent);
            }
        });

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if(!session.isLoggedIn()){
            logoutUser();
        }

        // Fetching user details from SQLite
        HashMap<String,String> user = db.getUserDetails();
        String name = user.get("name");
        String email = user.get("email");
        TVNama = (TextView)findViewById(R.id.TVnamaProfil);
        TVEmail = (TextView)findViewById(R.id.TVjabatanProfil);
        TVNama.setText(name);
        TVEmail.setText(email);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getParkLotData(){
        showDialog();
        JsonObjectRequest getParkLotData = new JsonObjectRequest(Request.Method.GET, AppConfig.URL_LAHAN_DATA, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try{
                    parkingLotArrayList = new ArrayList<HashMap<String, String>>();
                    JSONArray arrayLahan = response.getJSONArray("data");
                    for(int i=0; i<arrayLahan.length(); i++){
                        JSONObject item = (JSONObject)arrayLahan.get(i);

                        HashMap<String, String> lahan = new HashMap<>();
                        lahan.put(TAG_KEY_ID_LAHAN, item.getString(TAG_KEY_ID_LAHAN));
                        lahan.put(TAG_KEY_DESKRIPSI, item.getString(TAG_KEY_DESKRIPSI));
                        lahan.put(TAG_KEY_ALIAS, item.getString(TAG_KEY_ALIAS));
                        lahan.put(TAG_KEY_LATITUDE, item.getString(TAG_KEY_LATITUDE));
                        lahan.put(TAG_KEY_LONGITUDE, item.getString(TAG_KEY_LONGITUDE));
                        lahan.put(TAG_KEY_SISA_KAPASITAS_MOBIL, item.getString(TAG_KEY_SISA_KAPASITAS_MOBIL));
                        lahan.put(TAG_KEY_MAX_KAPASITAS_MOBIL, item.getString(TAG_KEY_MAX_KAPASITAS_MOBIL));
                        lahan.put(TAG_KEY_MAX_KAPASITAS_MOTOR, item.getString(TAG_KEY_MAX_KAPASITAS_MOTOR));
                        lahan.put(TAG_KEY_JAM_BUKA, item.getString(TAG_KEY_JAM_BUKA));
                        lahan.put(TAG_KEY_JAM_TUTUP, item.getString(TAG_KEY_JAM_TUTUP));
                        lahan.put(TAG_KEY_LINK_GAMBAR, item.getString(TAG_KEY_LINK_GAMBAR));

                        parkingLotArrayList.add(lahan);
                    }

                    GridAdapter gridAdapter = new GridAdapter(getApplicationContext(), parkingLotArrayList);
                    gridView.setAdapter(gridAdapter);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(getApplicationContext(), "You Clicked at " + parkingLotArrayList.get(position).get(TAG_KEY_ALIAS), Toast.LENGTH_SHORT).show();
                            HashMap<String, String> selectedLot = parkingLotArrayList.get(position);
                            Intent intent = new Intent(HomeActivity.this, DetailHomeActivity.class);
                            intent.putExtra("selectedLot", selectedLot);
                            startActivity(intent);
                        }
                    });
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        AppController.getInstance().addToRequestQueue(getParkLotData);
    }

    private void logoutUser(){
        session.setLogin(false);
        db.deleteUsers();

        // launching the login Activity
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
