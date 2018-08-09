package gg.joshbra.tagg.Helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.HashMap;

public class DatabaseHelper {
    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    public HashMap<String, Integer> getTaggs() {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[]{ MediaStore.Audio.PlaylistsColumns.NAME, BaseColumns._ID },
                null, null,
                MediaStore.Audio.PlaylistsColumns.NAME);

        if (cursor != null && cursor.moveToFirst()) {
            HashMap<String, Integer> out = new HashMap<>();
            do {
                out.put(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.NAME)), cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
            } while (cursor.moveToNext());
            cursor.close();
            return out;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Creates new playlist with title 'name'
     */
    public long createTagg(String name) {
        if (name != null && name.length() > 0) {
            ContentResolver resolver = context.getContentResolver();
            String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = ?";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection, selection, new String[]{name}, null);

            if (cursor == null) { return -1; }

            if (cursor.getCount() <= 0) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            cursor.close();
            return -1;
        }
        return -1;
    }

    /**
     * Gets playlist length
     */
    private int getTaggLength(long taggID) {
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", taggID),
                new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID,}, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);

        if (c != null) {
            int out = c.getCount();
            c.close();
            return out;
        }

        return 0;
    }

    /**
     * Makes cursor for a playlist
     */
    public Cursor getTaggSongCursor(int taggID) {
        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " = 1 AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", taggID),
                new String[]{
                        MediaStore.Audio.Playlists.Members.AUDIO_ID,
                        MediaStore.Audio.Playlists.Members.PLAY_ORDER
                }, selection, null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }

    /**
     * Given a cursor querying a playlist, returns the id's of songs within that playlist
     */
    public int[][] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return new int[][]{};
        }
        int len = cursor.getCount();
        int[] list = new int[len];
        int[] orders = new int[len];
        cursor.moveToFirst();
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getInt(0);
            orders[i] = cursor.getInt(1);
            cursor.moveToNext();
        }
        cursor.close();
        return new int[][]{list, orders};
    }

    public int[] getSongListFromTagg(int taggID) {
        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " = 1 AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", taggID),
                new String[]{
                        MediaStore.Audio.Playlists.Members.AUDIO_ID
                }, selection, null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);

        if (cursor == null) { return new int[]{}; }

        int len = cursor.getCount();
        int[] out = new int[len];

        cursor.moveToFirst();

        for (int i = 0; i < len; i++) {
            out[i] = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return out;
    }

    /**
     * Gets the id for a given playlist
     */
    public long getTaggID(String name) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[]{ BaseColumns._ID },
                MediaStore.Audio.PlaylistsColumns.NAME + " = ?", new String[]{ name },
                MediaStore.Audio.PlaylistsColumns.NAME);
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        return id;
    }

    /**
     * Returns the name of the playlist with the given ID
     */
    public String getTaggName(long id) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.PlaylistsColumns.NAME},
                BaseColumns._ID + " = ?",
                new String[]{Long.toString(id)},
                null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        }
        // nothing found
        return null;
    }

    /**
     * Adds song to playlist
     */
    public void addToTagg(long[] ids, long taggID) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", taggID);

        ContentValues[] contentValues = new ContentValues[ids.length];

        for (int i = 0; i < ids.length; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, getTaggLength(taggID) + 1);
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[i]);
        }

        resolver.bulkInsert(uri, contentValues);
    }

    /**
     * Removes song from playlist
     */
    public void removeFromTagg(long id, long taggID) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", taggID);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ?", new String[] { Long.toString(id) });
    }

    /**
     * Deletes tagg
     */
    public void deleteTagg(String taggName) {
        Long playlistid = getTaggID(taggName);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, MediaStore.Audio.Playlists._ID + " = ?", new String[] {playlistid.toString()});
    }
}
