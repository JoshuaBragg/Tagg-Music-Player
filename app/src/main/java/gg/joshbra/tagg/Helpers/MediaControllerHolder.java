package gg.joshbra.tagg.Helpers;

import android.support.v4.media.session.MediaControllerCompat;

/**
 * Simple holder for the current MediaController
 */
public class MediaControllerHolder {
    private static MediaControllerCompat mediaController;

    public static void setMediaController(MediaControllerCompat mediaController) {
        MediaControllerHolder.mediaController = mediaController;
    }

    public static MediaControllerCompat getMediaController() {
        return MediaControllerHolder.mediaController;
    }
}
