package gg.joshbra.tagg.Helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.widget.NumberPicker;

import gg.joshbra.tagg.R;

/**
 * A number picker whose style has been changed to change text colour
 */
public class ThemedNumberPicker extends NumberPicker {
    public ThemedNumberPicker(Context context) {
        this(context, null);
    }

    public ThemedNumberPicker(Context context, AttributeSet attrs) {
        super(new ContextThemeWrapper(context, R.style.Picker), attrs);
    }
}
