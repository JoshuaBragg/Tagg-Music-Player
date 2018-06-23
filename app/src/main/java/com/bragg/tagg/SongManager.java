package com.bragg.tagg;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Collections;

public class SongManager {
    private ArrayList<SongInfo> allSongs, currSongs;
    private ArrayList<String> taggs, activeTaggs;

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
        activeTaggs = new ArrayList<>();

        // TODO: remove this manual adding of taggs
        addTagg("T1");
        addTagg("T2");
        addTagg("T3");
    }

    public void readSongs() {
        Cursor data = databaseHelper.getSongs();

        while (data.moveToNext()) {
            SongInfo s = new SongInfo(data.getString(1), data.getString(2), data.getString(3), data.getString(4));

            ArrayList<String> taggs = databaseHelper.getSongsRelatedTaggs(data.getString(1), data.getString(2), data.getString(3), data.getString(4));

            for (String t : taggs) {
                s.addTagg(t);
            }

            allSongs.add(s);
        }
        data.close();

        // TODO: remove this manual adding of taggs
        addSongTaggRelation("juice", "<unknown>", "/storage/emulated/0/Music/juice.mp3", "1527637530", "T1");
        addSongTaggRelation("juice", "<unknown>", "/storage/emulated/0/Music/juice.mp3", "1527637530", "T3");
        addSongTaggRelation("diplo", "<unknown>", "/storage/emulated/0/Music/diplo.mp3", "1527637530", "T1");
        addSongTaggRelation("diplo", "<unknown>", "/storage/emulated/0/Music/diplo.mp3", "1527637530", "T2");
    }

    public void readTaggs() {
        Cursor data = databaseHelper.getTaggs();

        while (data.moveToNext()) {
            taggs.add(data.getString(1));
        }

        data.close();
    }

    public void checkSongsForChanges(ArrayList<SongInfo> songs) {
        Collections.sort(songs);
        Collections.sort(this.allSongs);

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

    public void updateCurrSongs() {
        ArrayList<SongInfo> newCurrSongs = new ArrayList<>();

        // TODO: instead of iterating through list and calling several times just make databaseHelper method take array of taggs and implode
        for (String tagg : activeTaggs) {
            for (SongInfo s : getTaggsRelatedSongs(tagg)) {
                if (!newCurrSongs.contains(s)) {
                    newCurrSongs.add(s);
                }
            }
        }

        currSongs = newCurrSongs;
    }

    private void setCurrSongs(ArrayList<SongInfo> currSongs) {
        this.currSongs = currSongs;
    }

    public ArrayList<SongInfo> getCurrSongs() {
        return currSongs;
    }

    public ArrayList<String> getTaggs() {
        return taggs;
    }

    private void addSong(SongInfo s) {
        databaseHelper.addSong(s.getSongName(), s.getArtistName(), s.getSongUrl(), s.getDateAdded());
        this.allSongs.add(s);
    }

    private void removeSong(SongInfo s) {
        databaseHelper.removeSong(s.getSongName(), s.getArtistName(), s.getSongUrl(), s.getDateAdded());
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

    public boolean addSongTaggRelation(String song_name, String artist_name, String url, String dateAdded, String tagg) {
        return databaseHelper.addSongTaggRelation(song_name, artist_name, url, dateAdded, tagg);
    }

    public boolean removeSongTaggRelation(String song_name, String artist_name, String url, String dateAdded, String tagg) {
        return databaseHelper.removeSongTaggRelation(song_name, artist_name, url, dateAdded, tagg);
    }

    private ArrayList<SongInfo> getTaggsRelatedSongs(String tagg) {
        ArrayList<String[]> s = databaseHelper.getTaggsRelatedSongs(tagg);
        ArrayList<SongInfo> out = new ArrayList<>();

        for (String[] ar : s) {
            out.add(new SongInfo(ar[0], ar[1], ar[2], ar[3]));
        }

        return out;
    }

    public ArrayList<SongInfo> getAllSongs() {
        return allSongs;
    }

    public void activateTagg(String tagg) {
        if (!activeTaggs.contains(tagg)) {
            activeTaggs.add(tagg);
        }
    }

    public void deactivateTagg(String tagg) {
        if (activeTaggs.contains(tagg)) {
            activeTaggs.remove(tagg);
        }
    }

    public ArrayList<String> getActiveTaggs() {
        return activeTaggs;
    }
}
