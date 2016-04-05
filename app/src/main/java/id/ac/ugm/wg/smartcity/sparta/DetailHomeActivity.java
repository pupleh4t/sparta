package id.ac.ugm.wg.smartcity.sparta;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.app.AppController;

public class DetailHomeActivity extends AppCompatActivity {
    Button BTNOpenMap;
    HashMap<String, String> selectedData;
    TextView TVDepartment, TVFreeCarSpace, TVMaxCarSpace, TVOpenAt, TVCloseAt;
    ImageView SIVcover;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        selectedData = (HashMap<String,String>) intent.getSerializableExtra("selectedLot");

        TVDepartment = (TextView)findViewById(R.id.TVDepartmentName);
        TVFreeCarSpace = (TextView)findViewById(R.id.TVFreeParkingSpace);
        TVMaxCarSpace = (TextView)findViewById(R.id.TVMaxParkingSpace);
        TVOpenAt = (TextView)findViewById(R.id.TVOpenAt);
        TVCloseAt = (TextView)findViewById(R.id.TVCloseAt);
        SIVcover = (ImageView)findViewById(R.id.SIVcover);

        TVDepartment.setText(selectedData.get(AppConfig.TAG_KEY_DESKRIPSI));
        TVFreeCarSpace.setText(new StringBuilder(selectedData.get(AppConfig.TAG_KEY_SISA_KAPASITAS_MOBIL) + " car parking space available"));
        TVMaxCarSpace.setText(new StringBuilder(selectedData.get(AppConfig.TAG_KEY_MAX_KAPASITAS_MOBIL) + " slots capacity"));
        TVOpenAt.setText(new StringBuilder(selectedData.get(AppConfig.TAG_KEY_JAM_BUKA) + " WIB"));
        TVCloseAt.setText(new StringBuilder(selectedData.get(AppConfig.TAG_KEY_JAM_TUTUP) + " WIB"));

        if(imageLoader == null){imageLoader = AppController.getInstance().getImageLoader();}
        String url = selectedData.get(AppConfig.TAG_KEY_LINK_GAMBAR);
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                SIVcover.getLayoutParams().height = getResources().getDisplayMetrics().widthPixels;
                SIVcover.getLayoutParams().width = getResources().getDisplayMetrics().widthPixels;
                SIVcover.requestLayout();
                SIVcover.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(selectedData.get(AppConfig.TAG_KEY_ALIAS));

        BTNOpenMap = (Button) findViewById(R.id.BTNOpenMap);
        BTNOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailHomeActivity.this, ParkNowActivity.class);
                intent.putExtra("selectedLot", selectedData);
                startActivity(intent);
            }
        });
    }

}
