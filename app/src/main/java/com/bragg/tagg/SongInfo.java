package com.bragg.tagg;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class SongInfo implements Comparable<SongInfo> {
    private String songName, artistName, songUrl;
    private ArrayList<String> taggs;

    public SongInfo(String songName, String artistName, String songUrl) {
        this.songName = songName;
        this.artistName = artistName;
        this.songUrl = songUrl;
        this.taggs = new ArrayList<>();
    }

    public void removeTagg(String tagg) {
        if (taggs.contains(tagg)) {
            taggs.remove(tagg);
        }
    }

    public void addTagg(String tagg) {
        if (!taggs.contains(tagg)) {
            taggs.add(tagg);
        }
    }

    public boolean hasTagg(String tagg) {
        return taggs.contains(tagg);
    }

    public ArrayList<String> getTaggs() {
        return taggs;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public String toString() {
        return songName + " - " + artistName + " - " + songUrl;
    }

    public boolean equals(Object o) {
        return o instanceof SongInfo && ((SongInfo)o).getSongName().equals(songName) && ((SongInfo)o).getArtistName().equals(artistName) && ((SongInfo)o).getSongUrl().equals(songUrl);
    }

    @Override
    public int compareTo(@NonNull SongInfo songInfo) {
        return songName.toUpperCase().compareTo(songInfo.getSongName().toUpperCase());
    }
}
