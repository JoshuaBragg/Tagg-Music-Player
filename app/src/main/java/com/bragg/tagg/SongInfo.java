package com.bragg.tagg;

import java.io.Serializable;
import java.util.ArrayList;

public class SongInfo implements Serializable {
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
}
