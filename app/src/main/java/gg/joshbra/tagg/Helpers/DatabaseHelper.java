package gg.joshbra.tagg.Helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.HashMap;

/**
 * Interface between Tagg and SQL databases that store songs/playlists/albums
 */
public class DatabaseHelper {
    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    /**
     * Gets all Taggs from MediaStore.Audio.PlaylistColumns
     * @return Returns HashMap of Tagg name to Tagg ID pairs
     */
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
     * Creates new Tagg with title 'name'
     * @param name The title of the new Tagg
     * @return
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
     * Get the amount of songs associated with a given Tagg
     * @param taggID The ID of the Tagg to query
     * @return The number of songs associated with this Tagg
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
     * Gets cursor for a Tagg
     * @param taggID The ID of the Tagg to query
     * @return A cursor that can iterate through a Tagg's songs
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
     * Given a cursor querying a Tagg, returns the id's of songs associated with that Tagg
     * @param cursor A cursor obtained from a Tagg
     * @return Returns two int[] within int[][], one for song ID's and the other for the play orders
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

    /**
     * Gets song list for a given Tagg
     * @param taggID The ID of the Tagg to be queried
     * @return Returns an int[] of song ID's associated with this Tagg
     */
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
     * Given a Tagg name returns the taggID
     * @param name The name of the Tagg to query
     * @return The ID of the given Tagg
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
     * Given a taggID returns the Tagg name
     * @param id The taggID to be queried
     * @return The name of the Tagg with the ID given
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
     * Adds songs to Tagg
     * @param ids Array of song ids to add to Tagg
     * @param taggID The ID of the tagg to add songs to
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
     * Removes song from Tagg
     * @param id The id of the song to remove
     * @param taggID The taggID of the Tagg to remove the song from
     */
    public void removeFromTagg(long id, long taggID) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", taggID);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ?", new String[] { Long.toString(id) });
    }

    /**
     * Permanently removes the Tagg from the device
     * @param taggName The name of the tagg to remove
     */
    public void deleteTagg(String taggName) {
        Long playlistid = getTaggID(taggName);
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, MediaStore.Audio.Playlists._ID + " = ?", new String[] {playlistid.toString()});
    }

    public void renameTagg(String newTaggName, long taggID) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        String where = MediaStore.Audio.Playlists._ID + " = ?";
        String[] whereVal = { Long.toString(taggID) };
        values.put(MediaStore.Audio.Playlists.NAME, newTaggName);
        resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, where, whereVal);
    }
}
