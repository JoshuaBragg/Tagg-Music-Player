package gg.joshbra.tagg.Helpers;

import java.util.ArrayList;

import gg.joshbra.tagg.SongInfo;

/**
 * Helper class to search for songs
 */
public class SongFinder {
    // This class cannot and should not be instantiated
    private SongFinder() {}

    /**
     * Given a query and array of songs, return the songs who's titles contain the query
     * @param query The query to search for
     * @param songs The array of songs to search in
     * @return Array of all songs which name's contain the query
     */
    public static ArrayList<SongInfo> searchByContains(String query, ArrayList<SongInfo> songs) {
        if (songs == null) {
            return new ArrayList<>();
        }

        ArrayList<SongInfo> out = new ArrayList<>();

        String queryUpper = query.toUpperCase();

        for (SongInfo s : songs) {
            if (s.getSongName().toUpperCase().contains(queryUpper)) {
                out.add(s);
            }
        }

        return out;
    }
}
