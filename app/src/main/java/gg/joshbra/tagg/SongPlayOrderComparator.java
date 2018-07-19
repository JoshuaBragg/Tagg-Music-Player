package gg.joshbra.tagg;

import java.util.Comparator;

public class SongPlayOrderComparator implements Comparator<SongPlayOrderTuple> {
    @Override
    public int compare(SongPlayOrderTuple o1, SongPlayOrderTuple o2) {
        if (o1.playOrder > o2.playOrder) {
            return 1;
        } else if (o1.playOrder < o2.playOrder) {
            return -1;
        }
        return 0;
    }
}
