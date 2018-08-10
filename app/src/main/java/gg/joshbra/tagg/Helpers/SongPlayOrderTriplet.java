package gg.joshbra.tagg.Helpers;

import gg.joshbra.tagg.SongInfo;

/**
 * A data structure to hold a Song and its play order in both regular and shuffled playback modes
 */
public class SongPlayOrderTriplet {
    private SongInfo songInfo;
    private int orderSeq, orderShuffle;

    public SongPlayOrderTriplet(SongInfo songInfo, int orderSeq, int orderShuffle) {
        this.songInfo = songInfo;
        this.orderSeq = orderSeq;
        this.orderShuffle = orderShuffle;
    }

    public SongInfo getSongInfo() {
        return songInfo;
    }

    public int getOrderSeq() {
        return orderSeq;
    }

    public void setOrderSeq(int orderSeq) {
        this.orderSeq = orderSeq;
    }

    public int getOrderShuffle() {
        return orderShuffle;
    }

    public void setOrderShuffle(int orderShuffle) {
        this.orderShuffle = orderShuffle;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SongPlayOrderTriplet && songInfo.equals(((SongPlayOrderTriplet) obj).getSongInfo())
                && orderSeq == ((SongPlayOrderTriplet) obj).getOrderSeq() && orderShuffle == ((SongPlayOrderTriplet) obj).getOrderShuffle();
    }

    @Override
    public String toString() {
        return orderSeq + " - " + orderShuffle + " - " + songInfo.toString();
    }
}
