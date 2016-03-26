package id.ac.ugm.wg.smartcity.sparta;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;

import id.ac.ugm.wg.smartcity.sparta.helper.SQLiteHandler;
import id.ac.ugm.wg.smartcity.sparta.helper.SessionManager;
import id.ac.ugm.wg.smartcity.sparta.widgets.CustomGridView;
import id.ac.ugm.wg.smartcity.sparta.helper.GridAdapter;

public class HomeActivity extends AppCompatActivity {
    CollapsingToolbarLayout collapsingToolbar;
    CustomGridView gridView;
    TextView TVNama, TVEmail;
    String[] parkingLotArray;
    SessionManager session;
    SQLiteHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gridView = (CustomGridView)findViewById(R.id.gridview);
        gridView.setExpanded(true);
        final ArrayList<String> parkingLotList = new ArrayList<>();
        parkingLotList.add("KPFT");
        parkingLotList.add("DTAP");   //Departemen Teknik Arsitektur dan Perencanaan Wilayah Tata Kota
        parkingLotList.add("DTK");    //Departemen Teknik Kimia
        parkingLotList.add("DTNTF");  //Departemen Teknik Fisika dan Nuklir
        parkingLotList.add("DTETI");  //Departemen Teknik Elektro dan Teknologi Informasi
        parkingLotList.add("DTGL");   //Departemen Teknik Geologi
        parkingLotList.add("DTMI");   //Departemen Teknik Mesin dan Industri
        parkingLotList.add("DTGD");   //Departemen Teknik Geodesi
        parkingLotList.add("DTSL");   //Departemen Teknik Sipil
        parkingLotList.add("TUGU");

        parkingLotArray = new String[parkingLotList.size()];
        parkingLotList.toArray(parkingLotArray);
        GridAdapter gridAdapter = new GridAdapter(this, parkingLotArray);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "You Clicked at " + parkingLotArray[position], Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, DetailHomeActivity.class);
                startActivity(intent);
            }
        });

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

    private void logoutUser(){
        session.setLogin(false);
        db.deleteUsers();

        // launching the login Activity
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
