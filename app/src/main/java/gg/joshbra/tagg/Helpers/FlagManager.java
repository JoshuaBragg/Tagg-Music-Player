package gg.joshbra.tagg.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.SongManager;

/**
 * Manages the SharedPreferences and is responsible for retrieving ans saving Flags/Data between app launches
 */
public class FlagManager {
    private int[] flags;
    private String[] songPreferences;

    private static final String PREF_NAME = "flags";
    private static final String[] FLAG_NAMES = new String[]{"shuffle_state", "repeat_state"};
    private static final int[] DEFAULT_FLAG_VAL = new int[]{PlaybackStateCompat.SHUFFLE_MODE_NONE, PlaybackStateCompat.REPEAT_MODE_ALL};

    private static final String[] SONG_PREF_NAMES = new String[]{"active_taggs", "current_song", "added_to_queue", "curr_queue_type"};
    private static final String[] DEFAULT_SONG_PREF_VAL = new String[]{"", String.valueOf(-1), "", String.valueOf(SongManager.TYPE_ALL_SONGS)};

    ////////////////////////////// Singleton ///////////////////////////////

    private static final FlagManager self = new FlagManager();

    private FlagManager() {
        flags = new int[FLAG_NAMES.length];
        songPreferences = new String[SONG_PREF_NAMES.length];
    }

    public static FlagManager getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    /**
     * Fetches the flags from SharedPreferences
     * @param activity An activity to obtain shared preferences from
     */
    public void fetchFlags(AppCompatActivity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        for (int i = 0; i < FLAG_NAMES.length; i++) {
            flags[i] = preferences.getInt(FLAG_NAMES[i], DEFAULT_FLAG_VAL[i]);
        }
    }

    /**
     * Sets the flags to the values that are currently active
     * @param activity An activity to obtain shared preferences from
     */
    public void setFlags(AppCompatActivity activity) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();

        int[] flags = new int[]{PlayQueue.getSelf().getShuffleMode(), PlayQueue.getSelf().getRepeatMode()};

        for (int i = 0; i < flags.length; i++) {
            editor.putInt(FLAG_NAMES[i], flags[i]);
        }

        editor.apply();
    }

    /**
     * Get flags
     * @return int[] of flag values
     */
    public int[] getFlags() {
        return flags;
    }

    /**
     * Fetches song preferences
     * @param activity The activity to get shared preferences from
     */
    public void fetchSongPreferences(AppCompatActivity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        for (int i = 0; i < SONG_PREF_NAMES.length; i++) {
            songPreferences[i] = preferences.getString(SONG_PREF_NAMES[i], DEFAULT_SONG_PREF_VAL[i]);
        }
    }

    /**
     * Sets the song preferences
     * @param o An instance of either an AppCompatActivity or MediaBrowserServiceCompat
     */
    public void setSongPreferences(Object o) {
        SharedPreferences.Editor editor;
        if (o instanceof MediaBrowserServiceCompat) {
            editor = PreferenceManager.getDefaultSharedPreferences((MediaBrowserServiceCompat) o).edit();
        } else if (o instanceof AppCompatActivity) {
            editor = PreferenceManager.getDefaultSharedPreferences((AppCompatActivity) o).edit();
        } else {
            return;
        }

        // Storing active Taggs

        HashMap<String, Integer> activeTaggs = SongManager.getSelf().getActiveTaggsRaw();

        JSONArray activeTaggJSONArray = new JSONArray();

        for (String key : activeTaggs.keySet()) {
            JSONArray temp = new JSONArray();
            temp.put(key);
            temp.put(activeTaggs.get(key));
            activeTaggJSONArray.put(temp);
        }

        String serializedActiveTaggs = activeTaggJSONArray.toString();

        editor.putString(SONG_PREF_NAMES[0], serializedActiveTaggs);

        // Storing the song that is currently playing

        editor.putString(SONG_PREF_NAMES[1], String.valueOf(PlayQueue.getSelf().getCurrentMediaId()));

        // Storing the songs that were added to the queue

        ArrayList<SongPlayOrderTriplet> songsAddedToQueue = PlayQueue.getSelf().getSongsAddedToQueue();

        JSONArray songAddedToQueueJSONArray = new JSONArray();

        for (SongPlayOrderTriplet songPlayOrderTriplet : songsAddedToQueue) {
            JSONArray temp = new JSONArray();
            temp.put(songPlayOrderTriplet.getSongInfo().getMediaID());
            temp.put(songPlayOrderTriplet.getOrderSeq());
            temp.put(songPlayOrderTriplet.getOrderShuffle());
            songAddedToQueueJSONArray.put(temp);
        }

        String serializedSongsAddedToQueue = songAddedToQueueJSONArray.toString();

        editor.putString(SONG_PREF_NAMES[2], serializedSongsAddedToQueue);

        // Storing which queueType is currently active

        editor.putString(SONG_PREF_NAMES[3], String.valueOf(SongManager.getSelf().getQueueType()));

        editor.apply();
    }

    /**
     * Gets the active taggs from last time the app was open
     * @return HashMap of Tagg name and taggID pairs of the active taggs
     */
    public HashMap<String, Integer> getActiveTaggs() {
        String serializedActiveTaggs = songPreferences[0];
        if (serializedActiveTaggs.equals(DEFAULT_SONG_PREF_VAL[0])) {
            return new HashMap<>();
        }

        try {
            JSONArray activeTaggJSONArray = new JSONArray(serializedActiveTaggs);
            HashMap<String, Integer> out = new HashMap<>();
            for (int i = 0; i < activeTaggJSONArray.length(); i++) {
                JSONArray temp = activeTaggJSONArray.getJSONArray(i);
                out.put(temp.getString(0), temp.getInt(1));
            }
            return out;
        } catch (JSONException e) {
            return new HashMap<>();
        }
    }

    /**
     * Gets the song that was played last
     * @return The songID of the last played song
     */
    public Long getCurrSong() {
        return Long.valueOf(songPreferences[1]);
    }

    /**
     * Gets the songs that were added to the queue
     * @return ArrayList of songs that were added to queue
     */
    public ArrayList<SongPlayOrderTriplet> getSongsAddedToQueue() {
        String serializedSongsAddedToQueue = songPreferences[2];

        if (serializedSongsAddedToQueue.equals(DEFAULT_SONG_PREF_VAL[2])) {
            return new ArrayList<>();
        }

        try {
            JSONArray songsAddedToQueueJSONArray = new JSONArray(serializedSongsAddedToQueue);
            ArrayList<SongPlayOrderTriplet> out = new ArrayList<>();
            for (int i = 0; i < songsAddedToQueueJSONArray.length(); i++) {
                JSONArray temp = songsAddedToQueueJSONArray.getJSONArray(i);
                out.add(new SongPlayOrderTriplet(SongManager.getSelf().getSongInfoFromID(temp.getInt(0)), temp.getInt(1), temp.getInt(2)));
            }
            return out;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the queueType
     * @return The queueType from last time the app was open
     */
    public int getQueueType() {
        return Integer.valueOf(songPreferences[3]);
    }
}

