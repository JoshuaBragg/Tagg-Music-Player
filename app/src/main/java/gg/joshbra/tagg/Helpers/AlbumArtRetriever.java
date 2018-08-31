package gg.joshbra.tagg.Helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Helper to get the AlbumArt for a given song
 */
public class AlbumArtRetriever {
    private static ContentResolver contentResolver;

    // Instances of this class cannot be made
    private AlbumArtRetriever() {}

    /**
     * Sets the content resolver to be used in the future, must be set before getAlbumArt is called
     * @param contentResolver A content resolver
     */
    public static void setContentResolver(ContentResolver contentResolver) {
        AlbumArtRetriever.contentResolver = contentResolver;
    }

    /**
     * Given an AlbumID, returns either the path to a songs album art
     * @param albumID The albumID to retrieve albumArt for
     * @return Returns the path to the albumArt or null if no album with that ID exists
     */
    public static String getAlbumArt(int albumID) {
        if (contentResolver == null) {
            return null;
        }

        Cursor albumCursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, MediaStore.Audio.Albums._ID + " = ?", new String[] {String.valueOf(albumID)}, null);

        String albumArt;

        if (albumCursor != null && albumCursor.moveToFirst()) {
            albumArt = albumCursor.getString(1);
            albumCursor.close();
        } else {
            albumArt = null;
        }

        return albumArt;
    }
}
