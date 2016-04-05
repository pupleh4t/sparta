package id.ac.ugm.wg.smartcity.sparta.helper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import id.ac.ugm.wg.smartcity.sparta.R;

/**
 * Created by HermawanRahmatHidaya on 15/01/2016.
 */
public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private static final String TAG_SISA_KAPASITAS_MOBIL = "sisa_kapasitas_mobil";
    private static final String TAG_MAX_KAPASITAS_MOBIL = "max_kapasitas_mobil";
    private static final String TAG_ALIAS = "alias";

    //private final String[] parkingLotArray;
    private final ArrayList<HashMap<String,String>> parkingLotArrayList;
    private HashMap<String, String> parkingLotHashMap;

    public GridAdapter(Context c, ArrayList<HashMap<String, String>> parkingLotArrayList) {
        mContext = c;
        this.parkingLotArrayList = parkingLotArrayList;
    }

    public int getCount() {
        return parkingLotArrayList.size();
    }

    public HashMap<String, String> getItem(int position) {
        parkingLotHashMap = parkingLotArrayList.get(position);
        return parkingLotHashMap;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single, null);

            TextView alias = (TextView) grid.findViewById(R.id.TVGridParkingLotAddress);
            TextView sisa = (TextView) grid.findViewById(R.id.TVGridParkingLotSpaceAvailable);
            TextView max = (TextView) grid.findViewById(R.id.TVGridParkingLotMaxSpaceAvailable);
            RelativeLayout gridCell = (RelativeLayout) grid.findViewById(R.id.ParentLayoutGridCell);

            alias.setText(parkingLotArrayList.get(position).get(TAG_ALIAS));
            sisa.setText(parkingLotArrayList.get(position).get(TAG_SISA_KAPASITAS_MOBIL));
            max.setText(parkingLotArrayList.get(position).get(TAG_MAX_KAPASITAS_MOBIL));

            if(position%4==0)
            {
                gridCell.setBackgroundColor(grid.getResources().getColor(R.color.md_indigo_400));
            }
            else if(position%4==1){
                gridCell.setBackgroundColor(grid.getResources().getColor(R.color.md_indigo_600));
            }
            else if(position%4==2){
                gridCell.setBackgroundColor(grid.getResources().getColor(R.color.md_indigo_600));
            }
            else{
                gridCell.setBackgroundColor(grid.getResources().getColor(R.color.md_indigo_400));
            }
        }
        else{
            grid = (View) convertView;
        }

        return grid;}
}
