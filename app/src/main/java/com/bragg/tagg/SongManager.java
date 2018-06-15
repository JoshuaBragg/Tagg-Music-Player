package com.bragg.tagg;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

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

        while (data.moveToNext()) {
            SongInfo s = new SongInfo(data.getString(0), data.getString(1), data.getString(2));
            songs.add(s);
            Log.i("d", s.toString());
        }
    }

    public void checkSongs(ArrayList<SongInfo> songs) {
        int i = 0;
        while (i < this.songs.size()) {
            if (!songs.contains(this.songs.get(i))) {
                this.songs.remove(this.songs.get(i));
                removeSong(this.songs.get(i));
            } else {
                i++;
            }
        }

        for (SongInfo s : songs) {
            if (!this.songs.contains(s)) {
                this.songs.add(s);
                addSong(s);
            }
        }

//        for (SongInfo s : this.songs) {
//            Log.i("d", "" + s + "\n" + this.songs.size() + "\n");
//        }
    }

    public void addSong(SongInfo s) {
        boolean insertData = databaseHelper.addData(s.getSongName(), s.getArtistName(), s.getSongUrl());
//        if (insertData) {
//            Log.i("d", "1");
//        } else {
//            Log.i("d", "2");
//        }
    }

    public void removeSong(SongInfo s) {
        databaseHelper.removeData(s.getSongName(), s.getArtistName(), s.getSongUrl());
    }

    public ArrayList<SongInfo> getSongs() {
        return songs;
    }
}
