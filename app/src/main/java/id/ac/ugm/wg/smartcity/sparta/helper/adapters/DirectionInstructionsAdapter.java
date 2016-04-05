package id.ac.ugm.wg.smartcity.sparta.helper.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import id.ac.ugm.wg.smartcity.sparta.R;

/**
 * Created by HermawanRahmatHidaya on 04/04/2016.
 */
public class DirectionInstructionsAdapter extends RecyclerView.Adapter<DirectionInstructionsAdapter.MyViewHolder> {

    private List<DirectionInstructions> directionInstructionsList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView TVinstruction, TVdistance, TVduration;

        public MyViewHolder(View view) {
            super(view);
            TVinstruction = (TextView) view.findViewById(R.id.TVInstructions);
            TVdistance = (TextView) view.findViewById(R.id.TVDistance);
            TVduration = (TextView) view.findViewById(R.id.TVDuration);
        }
    }

    public DirectionInstructionsAdapter(List<DirectionInstructions> directionInstructionsList){
        this.directionInstructionsList = directionInstructionsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.instruction_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DirectionInstructions directionInstructions = directionInstructionsList.get(position);
        holder.TVinstruction.setText(directionInstructions.getInstruction());
        holder.TVdistance.setText(directionInstructions.getDistance());
        holder.TVduration.setText(directionInstructions.getDuration());
    }

    @Override
    public int getItemCount() {
        return directionInstructionsList.size();
    }
}
