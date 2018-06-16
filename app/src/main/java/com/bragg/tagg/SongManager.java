package com.bragg.tagg;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class SongManager {
    private ArrayList<SongInfo> songs;

    private DatabaseHelper databaseHelper;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final SongManager self = new SongManager();

    private SongManager() {
    }

    public static SongManager getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public void initDB(Context c) {
        databaseHelper = new DatabaseHelper(c);
    }

    public void readSongs() {
        Cursor data = databaseHelper.getData();

        songs = new ArrayList<>();

        Log.i("d", "\n_______________________________\n");

        while (data.moveToNext()) {
            SongInfo s = new SongInfo(data.getString(1), data.getString(2), data.getString(3));
            songs.add(s);
            Log.i("d", s.toString() + "|||" + data.getString(1) + "|||" + data.getColumnName(1));
        }
    }

    public void checkSongsForChanges(ArrayList<SongInfo> songs) {
        Collections.sort(songs);
        Collections.sort(this.songs);

        for (SongInfo s : this.songs) {
            Log.i("d", s.toString() + " is in database");
        }

        for (SongInfo s : songs) {
            Log.i("d", s.toString() + " is on device");
        }

        int i = 0;

        while (i < this.songs.size()) {
            if (!songs.contains(this.songs.get(i))) {
                Log.i("d", this.songs.get(i) + " was removed from songlist");
                removeSong(this.songs.get(i));
                this.songs.remove(this.songs.get(i));
            } else {
                i++;
            }
        }

        for (SongInfo s : songs) {
            if (!this.songs.contains(s)) {
                Log.i("d", s + " was added to songlist");
                this.songs.add(s);
                addSong(s);
            }
        }
    }

    public void addSong(SongInfo s) {
        Log.i("d", s.getSongName() + " was added " + databaseHelper.addData(s.getSongName(), s.getArtistName(), s.getSongUrl()));
    }

    public void removeSong(SongInfo s) {
        Log.i("d", s.getSongName() + " was removed " + databaseHelper.removeData(s.getSongName(), s.getArtistName(), s.getSongUrl()));
    }

    public ArrayList<SongInfo> getSongs() {
        return songs;
    }
}
