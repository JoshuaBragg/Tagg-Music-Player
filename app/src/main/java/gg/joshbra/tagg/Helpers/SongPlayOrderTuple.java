package gg.joshbra.tagg.Helpers;

import android.util.Log;

import gg.joshbra.tagg.SongInfo;

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
        Log.e("d", " I am comparing " + songInfo + " to " + obj.toString());
        return obj instanceof SongPlayOrderTuple && this.songInfo.equals(((SongPlayOrderTuple) obj).songInfo);
    }

    @Override
    public int hashCode() {
        return songInfo.getMediaID().intValue();
    }
}
