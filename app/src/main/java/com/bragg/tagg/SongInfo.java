package com.bragg.tagg;

import java.io.Serializable;

public class SongInfo implements Serializable {
    public String songName, artistName, songUrl;

    public SongInfo(String songName, String artistName, String songUrl) {
        this.songName = songName;
        this.artistName = artistName;
        this.songUrl = songUrl;
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
}
