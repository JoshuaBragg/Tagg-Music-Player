package gg.joshbra.tagg.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class FlagManager {
    private int[] flags;

    private static final String PREF_NAME = "flags";
    private static final String[] FLAG_NAMES = new String[]{"shuffle_state", "repeat_state"};
    private static final int[] DEFAULT_VAL = new int[]{PlaybackStateCompat.SHUFFLE_MODE_NONE, PlaybackStateCompat.REPEAT_MODE_ALL};


    ////////////////////////////// Singleton ///////////////////////////////

    private static final FlagManager self = new FlagManager();

    private FlagManager() {
        flags = new int[FLAG_NAMES.length];
    }

    public static FlagManager getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public void fetchFlags(AppCompatActivity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        for (int i = 0; i < FLAG_NAMES.length; i++) {
            flags[i] = preferences.getInt(FLAG_NAMES[i], DEFAULT_VAL[i]);
        }
    }

    public void setFlags(AppCompatActivity activity, int[] flags) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();

        for (int i = 0; i < flags.length; i++) {
            editor.putInt(FLAG_NAMES[i], flags[i]);
        }

        editor.apply();
    }

    public int[] getFlags() {
        return flags;
    }
}

