package id.ac.ugm.wg.smartcity.sparta.helper.adapters;

import id.ac.ugm.wg.smartcity.sparta.helper.DirectionTools;

/**
 * Created by HermawanRahmatHidaya on 03/04/2016.
 */
public class DirectionInstructions {
    private String instruction, duration, distance;

    public DirectionInstructions() {

    }

    public DirectionInstructions(String instruction, String duration, String distance){
        this.instruction = instruction;
        this.duration = duration;
        this.distance = distance;
    }

    public void setInstruction(String instruction){this.instruction=instruction;}
    public void setDuration(String duration){this.duration=duration;}
    public void setDistance(String distance){this.distance=distance;}

    public String getInstruction(){return this.instruction;}
    public String getDuration(){return this.duration;}
    public String getDistance(){return this.distance;}
}
