package id.ac.ugm.wg.smartcity.sparta;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.helper.DepartmentAutoCompleteAdapter;
import id.ac.ugm.wg.smartcity.sparta.widgets.DelayAutoCompleteTextView;
import id.ac.ugm.wg.smartcity.sparta.helper.GeoAutoCompleteAdapter;
import id.ac.ugm.wg.smartcity.sparta.helper.GeoSearchResult;

public class TestActivity extends AppCompatActivity {
    private static String TAG = "Debug";
    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private SlidingUpPanelLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        geo_autocomplete = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete);
        if (geo_autocomplete != null) {
            geo_autocomplete.setThreshold(THRESHOLD);
            geo_autocomplete.setAdapter(new DepartmentAutoCompleteAdapter(this)); // 'this' is Activity instance

            geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    HashMap<String, String> result = (HashMap<String, String>) adapterView.getItemAtPosition(position);
                    geo_autocomplete.setText(result.get(AppConfig.TAG_KEY_DESKRIPSI));
                }
            });

            geo_autocomplete.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    }
                    return false;
                }
            });
        }


        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        Button btnShow = (Button)findViewById(R.id.btnShow);
        Button btnHide = (Button)findViewById(R.id.btnHide);
        if (btnShow != null) {
            btnShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    } else {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Hahaha", Snackbar.LENGTH_SHORT).show();
                }
            });
        }


    }
}
