package gg.joshbra.tagg;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Data structure to store song information
 */
public class SongInfo implements Comparable<SongInfo> {
    private MediaMetadataCompat mediaMetadataCompat;

    public SongInfo(String mediaID, String songName, String artistName, String songUrl, String albumName, long duration, String albumArt, String dateAdded) {
        mediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaID)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistName)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, songUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, dateAdded)
                .build();
    }

    public Long getMediaID() {
        return Long.parseLong(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
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

    public String getAlbumID() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
    }

    public String getDateAdded() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE);
    }

    public MediaMetadataCompat getMediaMetadataCompat() {
        return mediaMetadataCompat;
    }

    public String toString() {
        return getMediaID() + " - " + getSongName();
    }

    /**
     * Determine if two songInfos are equal or if a songInfo is equal to a Long
     * @param o The object to compare to
     * @return True iff the two songInfos have the same mediaID or If the mediaID is the same as the Long passed in
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SongInfo) {
            return ((SongInfo)o).getMediaID().equals(getMediaID());
        } else if (o instanceof Long) {
            return o.equals(getMediaID());
        }
        return false;
    }

    /**
     * Compares two songInfos
     * @param songInfo The songInfo to compare to
     * @return compares the names of the songs
     */
    @Override
    public int compareTo(@NonNull SongInfo songInfo) {
        return getSongName().toUpperCase().compareTo(songInfo.getSongName().toUpperCase());
    }
}
