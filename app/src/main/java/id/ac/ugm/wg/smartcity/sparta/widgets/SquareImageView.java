package id.ac.ugm.wg.smartcity.sparta.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by root on 14/02/16.
 */
public class SquareImageView extends ImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getMeasuredHeight();
        if (width>height){
            width=height;
        } else {
            height=width;
        }
        setMeasuredDimension(width, width);
    }
}
