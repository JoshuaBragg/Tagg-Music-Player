package gg.joshbra.tagg.Comparators;

import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Comparator;

import gg.joshbra.tagg.Helpers.SongPlayOrderTriplet;

/**
 * Comparator for SongPlayOrderTriplets, can compare in either shuffled or not shuffled modes
 */
public class SongPlayOrderTripletComparator implements Comparator<SongPlayOrderTriplet> {
    private int sortMode;

    public SongPlayOrderTripletComparator(int mode) {
        sortMode = mode;
    }

    @Override
    public int compare(SongPlayOrderTriplet o1, SongPlayOrderTriplet o2) {
        if (sortMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            if (o1.getOrderSeq() > o2.getOrderSeq()) {
                return 1;
            } else if (o1.getOrderSeq() < o2.getOrderSeq()) {
                return -1;
            }
        } else if (sortMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            if (o1.getOrderShuffle() > o2.getOrderShuffle()) {
                return 1;
            } else if (o1.getOrderShuffle() < o2.getOrderShuffle()) {
                return -1;
            }
        }
        return 0;
    }
}
