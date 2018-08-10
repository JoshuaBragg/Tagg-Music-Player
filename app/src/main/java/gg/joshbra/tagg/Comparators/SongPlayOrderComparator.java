package gg.joshbra.tagg.Comparators;

import java.util.Comparator;

import gg.joshbra.tagg.Helpers.SongPlayOrderTuple;

/**
 * Comparator for SongPlayOrderTuple's
 *
 * Sorts by play order
 */
public class SongPlayOrderComparator implements Comparator<SongPlayOrderTuple> {
    @Override
    public int compare(SongPlayOrderTuple o1, SongPlayOrderTuple o2) {
        if (o1.playOrder > o2.playOrder) {
            return 1;
        } else if (o1.playOrder < o2.playOrder) {
            return -1;
        }
        return o1.songInfo.compareTo(o2.songInfo);
    }
}
