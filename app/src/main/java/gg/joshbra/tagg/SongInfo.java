package gg.joshbra.tagg;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;

import java.security.PublicKey;
import java.util.ArrayList;

public class SongInfo implements Comparable<SongInfo> {
    private MediaMetadataCompat mediaMetadataCompat;
    private ArrayList<String> taggs;

    public SongInfo(String mediaID, String songName, String artistName, String songUrl, String albumName, Long duration, String albumArt, String dateAdded) {
        mediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaID)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistName)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, songUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration / 1000)
                .putString( MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,  albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, dateAdded)
                .build();
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

    public String getMediaID() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
    }

    public String getSongName() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    public String getArtistName() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    public String getSongUrl() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    public String getAlbumName() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
    }

    public Long getDuration() {
        return mediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    public String getAlbumArt() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
    }

    public String getDateAdded() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE);
    }

    public MediaMetadataCompat getMediaMetadataCompat() {
        return mediaMetadataCompat;
    }

    public String toString() {
        return getSongName() + " - " + getArtistName() + " - " + getSongUrl();
    }

    public boolean equals(Object o) {
        return o instanceof SongInfo && ((SongInfo)o).getSongName().equals(getSongName()) && ((SongInfo)o).getArtistName().equals(getArtistName()) && ((SongInfo)o).getSongUrl().equals(getSongUrl());
    }

    @Override
    public int compareTo(@NonNull SongInfo songInfo) {
        return getMediaID().toUpperCase().compareTo(songInfo.getMediaID().toUpperCase());
    }
}
