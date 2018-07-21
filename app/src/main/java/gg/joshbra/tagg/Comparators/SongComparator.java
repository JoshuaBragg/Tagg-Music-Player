package gg.joshbra.tagg.Comparators;

import java.util.Comparator;

import gg.joshbra.tagg.SongInfo;

public class SongComparator implements Comparator<SongInfo> {
    public final static int SORT_ALPH_ASC = 0;
    public final static int SORT_ALPH_DESC = 1;
    public final static int SORT_DATE_ASC = 2;
    public final static int SORT_DATE_DESC = 3;

    private int sortMode;

    public SongComparator(int sortMode) {
        this.sortMode = sortMode;
    }

    @Override
    public int compare(SongInfo o1, SongInfo o2) {
        switch (sortMode) {
            case (SORT_ALPH_ASC):
                return o1.getSongName().toUpperCase().compareTo(o2.getSongName().toUpperCase());
            case (SORT_ALPH_DESC):
                return o2.getSongName().toUpperCase().compareTo(o1.getSongName().toUpperCase());
            case (SORT_DATE_ASC):
                return Integer.parseInt(o1.getDateAdded()) > Integer.parseInt(o2.getDateAdded()) ? 1 : -1;
            case (SORT_DATE_DESC):
                return Integer.parseInt(o1.getDateAdded()) > Integer.parseInt(o2.getDateAdded()) ? -1 : 1;
        }
        return 0;
    }
}
