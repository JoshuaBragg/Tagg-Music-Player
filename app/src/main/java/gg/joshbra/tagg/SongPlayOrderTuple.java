package gg.joshbra.tagg;

public class SongPlayOrderTuple {
    public final SongInfo songInfo;
    public final int playOrder;

    public SongPlayOrderTuple(SongInfo songInfo, int playOrder) {
        this.songInfo = songInfo;
        this.playOrder = playOrder;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SongPlayOrderTuple && this.songInfo.equals(((SongPlayOrderTuple) obj).songInfo) && this.playOrder == ((SongPlayOrderTuple) obj).playOrder;
    }
}
