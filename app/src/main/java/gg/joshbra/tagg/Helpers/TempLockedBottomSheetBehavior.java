package gg.joshbra.tagg.Helpers;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;

/**
 * Custom BottomSheetBehavior that locks the BottomSheet if no song is loaded
 * Is responsible for keeping the now playing bar unopenable before a song has begun playing
 * @param <V>
 */
public class TempLockedBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    public TempLockedBottomSheetBehavior() {
        super();
    }

    public TempLockedBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        if (!(MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null)) {
            return false;
        }

        return super.onInterceptTouchEvent(parent, child, event);
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        if (!(MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null)) {
            return false;
        }

        return super.onTouchEvent(parent, child, event);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
        if (!(MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null)) {
            return false;
        }

        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }
}
