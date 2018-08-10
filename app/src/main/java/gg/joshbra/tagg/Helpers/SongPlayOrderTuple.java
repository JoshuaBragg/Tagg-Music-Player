package gg.joshbra.tagg.Helpers;

import gg.joshbra.tagg.SongInfo;

/**
 * A data structure to store a songInfo and the playOrder from the playlist order in SQL database
 */
public class SongPlayOrderTuple {
    public final SongInfo songInfo;
    public final int playOrder;

    public SongPlayOrderTuple(SongInfo songInfo, int playOrder) {
        this.songInfo = songInfo;
        this.playOrder = playOrder;
    }

    @Override
    public String toString() {
        return songInfo.toString() + " - order: " + playOrder;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SongPlayOrderTuple && this.songInfo.equals(((SongPlayOrderTuple) obj).songInfo);
    }

    @Override
    public int hashCode() {
        return songInfo.getMediaID().intValue();
    }
}
