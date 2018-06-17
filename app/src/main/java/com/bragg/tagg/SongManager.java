package com.bragg.tagg;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class SongManager {
    private ArrayList<SongInfo> songs;
    private ArrayList<String> taggs;

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

        // TODO: remove this manual adding of taggs
        databaseHelper.addTagg("T1");
        databaseHelper.addTagg("T2");
        databaseHelper.addTagg("T3");
    }

    public void readSongs() {
        Cursor data = databaseHelper.getSongs();

        songs = new ArrayList<>();

        Log.i("d", "\n_______________________________\n");

        while (data.moveToNext()) {
            SongInfo s = new SongInfo(data.getString(1), data.getString(2), data.getString(3));

            ArrayList<String> taggs = databaseHelper.getSongsRelatedTaggs(data.getString(1), data.getString(2), data.getString(3));

            for (String t : taggs) {
                s.addTagg(t);
                Log.i("d", t + " was added to  song " + data.getString(1));
            }

            songs.add(s);
            Log.i("d", s.toString() + " | | | " + data.getString(1) + " | | | " + data.getColumnName(1));
        }
        data.close();

        // TODO: remove this manual adding of taggs
        Log.i("d", "" + databaseHelper.addSongTaggRelation("juice", "<unknown>", "/storage/emulated/0/Music/juice.mp3", "T1"));
        Log.i("d", "" + databaseHelper.addSongTaggRelation("juice", "<unknown>", "/storage/emulated/0/Music/juice.mp3", "T3"));
        Log.i("d", "" + databaseHelper.addSongTaggRelation("diplo", "<unknown>", "/storage/emulated/0/Music/diplo.mp3", "T1"));
        Log.i("d", "" + databaseHelper.addSongTaggRelation("diplo", "<unknown>", "/storage/emulated/0/Music/diplo.mp3", "T2"));
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
            } else {
                i++;
            }
        }

        for (SongInfo s : songs) {
            if (!this.songs.contains(s)) {
                Log.i("d", s + " was added to songlist");
                addSong(s);
            }
        }
    }

    private void addSong(SongInfo s) {
        Log.i("d", s.getSongName() + " was added " + databaseHelper.addSong(s.getSongName(), s.getArtistName(), s.getSongUrl()));
        this.songs.add(s);
    }

    private void removeSong(SongInfo s) {
        Log.i("d", s.getSongName() + " was removed " + databaseHelper.removeSong(s.getSongName(), s.getArtistName(), s.getSongUrl()));
        this.songs.remove(s);
    }

    public void addTagg(String tagg) {
        if (!taggs.contains(tagg)) {
            taggs.add(tagg);
        }
        databaseHelper.addTagg(tagg);
    }

    public void removeTagg(String tagg) {
        if (taggs.contains(tagg)) {
            taggs.remove(tagg);
        }
        databaseHelper.removeTagg(tagg);
    }

    public void addSongTaggRelation(String song_name, String artist_name, String url, String tagg) {
        databaseHelper.addSongTaggRelation(song_name, artist_name, url, tagg);
    }

    public void removeSongTaggRelation(String song_name, String artist_name, String url, String tagg) {
        databaseHelper.removeSongTaggRelation(song_name, artist_name, url, tagg);
    }

    public ArrayList<SongInfo> getSongs() {
        return songs;
    }
}
