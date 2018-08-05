package gg.joshbra.tagg.Helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class AlbumArtRetriever {
    private static ContentResolver contentResolver;

    public static void setContentResolver(ContentResolver contentResolver) {
        AlbumArtRetriever.contentResolver = contentResolver;
    }

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
