package id.ac.ugm.wg.smartcity.sparta.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import id.ac.ugm.wg.smartcity.sparta.R;

/**
 * Created by HermawanRahmatHidaya on 15/01/2016.
 */
public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] parkingLotArray;

    public GridAdapter(Context c, String[] parkingLotArray) {
        mContext = c;
        this.parkingLotArray = parkingLotArray;
    }

    public int getCount() {
        return parkingLotArray.length;
    }

    public Object getItem(int position) {
        return null;
    }

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
            TextView textView = (TextView) grid.findViewById(R.id.TVGridParkingLotAddress);
            textView.setText(parkingLotArray[position]);
            RelativeLayout gridCell = (RelativeLayout) grid.findViewById(R.id.ParentLayoutGridCell);
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
