package gg.joshbra.tagg.Helpers;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Helper class that provides pixel values for scaling grid of albums to different screen sizes
 */
public class AlbumGridScaler {
    public static final int NUM_COL = 2;
    private static final double SPACING = .02;

    // Instances of this class cannot be made
    private AlbumGridScaler() {}

    /**
     * Returns the width of a Album tile for grid view
     * @param context A context required of obtaining displayMetrics
     * @return Width of album tile for grid view in pixels
     */
    public static double getItemWidthPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int pixelWidth = displayMetrics.widthPixels;
        double availSpace = pixelWidth * (1.0 - SPACING);
        return availSpace / 2.0;
    }

    /**
     * Returns the spacing amount between Album tiles in grid
     * @param context A context required of obtaining displayMetrics
     * @return Spacing between album tiles in pixels
     */
    public static double getItemSpacingPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int pixelWidth = displayMetrics.widthPixels;
        return pixelWidth * SPACING;
    }
}
