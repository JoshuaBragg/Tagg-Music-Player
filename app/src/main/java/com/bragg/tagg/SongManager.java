package com.bragg.tagg;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class SongManager {
    private ArrayList<SongInfo> allSongs, currSongs;
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

        allSongs = new ArrayList<>();
        currSongs = allSongs;

        taggs = new ArrayList<>();

        // TODO: remove this manual adding of taggs
        addTagg("T1");
        addTagg("T2");
        addTagg("T3");
    }

    public void readSongs() {
        Cursor data = databaseHelper.getSongs();

        while (data.moveToNext()) {
            SongInfo s = new SongInfo(data.getString(1), data.getString(2), data.getString(3));

            ArrayList<String> taggs = databaseHelper.getSongsRelatedTaggs(data.getString(1), data.getString(2), data.getString(3));

            for (String t : taggs) {
                s.addTagg(t);
            }

            allSongs.add(s);
        }
        data.close();

        databaseHelper.addSongTaggRelation("juice", "<unknown>", "/storage/emulated/0/Music/juice.mp3", "T1");
        databaseHelper.addSongTaggRelation("juice", "<unknown>", "/storage/emulated/0/Music/juice.mp3", "T3");
        databaseHelper.addSongTaggRelation("diplo", "<unknown>", "/storage/emulated/0/Music/diplo.mp3", "T1");
        databaseHelper.addSongTaggRelation("diplo", "<unknown>", "/storage/emulated/0/Music/diplo.mp3", "T2");

        // TODO: remove this manual adding of taggs
    }

    public void checkSongsForChanges(ArrayList<SongInfo> songs) {
        Collections.sort(songs);
        Collections.sort(this.allSongs);

        for (SongInfo s : this.allSongs) {
        }

        for (SongInfo s : songs) {
        }

        int i = 0;

        while (i < this.allSongs.size()) {
            if (!songs.contains(this.allSongs.get(i))) {
                removeSong(this.allSongs.get(i));
            } else {
                i++;
            }
        }

        for (SongInfo s : songs) {
            if (!this.allSongs.contains(s)) {
                addSong(s);
            }
        }
    }

    private void setCurrSongs(ArrayList<SongInfo> currSongs) {
        this.currSongs = currSongs;
    }

    public ArrayList<SongInfo> getCurrSongs() {
        return currSongs;
    }

    private void addSong(SongInfo s) {
        databaseHelper.addSong(s.getSongName(), s.getArtistName(), s.getSongUrl());
        this.allSongs.add(s);
    }

    private void removeSong(SongInfo s) {
        databaseHelper.removeSong(s.getSongName(), s.getArtistName(), s.getSongUrl());
        this.allSongs.remove(s);
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

    public ArrayList<SongInfo> getAllSongs() {
        return allSongs;
    }
}
