package gg.joshbra.tagg.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class MaxDimensionRecycler extends RecyclerView {
    public MaxDimensionRecycler(Context context) {
        super(context);
    }

    public MaxDimensionRecycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxDimensionRecycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {

        int height = MeasureSpec.makeMeasureSpec((int) Math.round(Resources.getSystem().getDisplayMetrics().heightPixels * (2.0 / 3.0)), MeasureSpec.AT_MOST);

        super.onMeasure(widthSpec, height);
    }
}
