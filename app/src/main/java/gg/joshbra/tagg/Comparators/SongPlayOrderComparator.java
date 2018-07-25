package gg.joshbra.tagg.Comparators;

import java.util.Comparator;

import gg.joshbra.tagg.Helpers.SongPlayOrderTuple;

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
