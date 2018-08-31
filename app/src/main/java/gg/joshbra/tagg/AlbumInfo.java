package gg.joshbra.tagg;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;

/**
 * Model that stores the information for an Album
 */
public class AlbumInfo implements Comparable<AlbumInfo> {
    private MediaMetadataCompat mediaMetadataCompat;

    public AlbumInfo(String albumID, String albumName, String albumArtist, String albumArt, long numSongs) {
        mediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, albumID)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumName)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, albumArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArt)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, numSongs)
                .build();
    }

    public long getAlbumID() {
        return Long.parseLong(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
    }

    public String getAlbumName() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
    }

    public String getAlbumArtist() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST);
    }

    public String getAlbumArt() {
        return mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
    }

    public long getNumSongs() {
        return mediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AlbumInfo && getAlbumID() == ((AlbumInfo) obj).getAlbumID();
    }

    @Override
    public int compareTo(@NonNull AlbumInfo o) {
        return getAlbumName().toUpperCase().compareTo(o.getAlbumName().toUpperCase());
    }
}
