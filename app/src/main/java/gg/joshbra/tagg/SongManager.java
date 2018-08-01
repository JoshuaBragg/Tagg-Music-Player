package gg.joshbra.tagg;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import gg.joshbra.tagg.Comparators.SongComparator;
import gg.joshbra.tagg.Comparators.SongPlayOrderComparator;
import gg.joshbra.tagg.Helpers.DatabaseHelper;
import gg.joshbra.tagg.Helpers.SongPlayOrderTuple;

public class SongManager {
    private ArrayList<SongInfo> allSongs;
    private HashMap<String, Integer> taggs, activeTaggs;
    private HashMap<Integer, SongInfo> allSongsMap;

    private PlayQueue playQueue;
    private DatabaseHelper databaseHelper;

    public static final int TYPE_ALL_SONGS = 0, TYPE_TAGG = 1, TYPE_RECENT = 2;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final SongManager self = new SongManager();

    private SongManager() {
        playQueue = PlayQueue.getSelf();
    }

    public static SongManager getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public void initDB(Context c) {
        databaseHelper = new DatabaseHelper(c);

        allSongs = new ArrayList<>();
        allSongsMap = new HashMap<>();
        playQueue.setCurrQueue(allSongs);

        taggs = databaseHelper.getTaggs();
        activeTaggs = new HashMap<>();
    }

    public void setSongList(ArrayList<SongInfo> songs) {
        Collections.sort(songs);
        allSongs = songs;
        for (SongInfo s : songs) {
            allSongsMap.put(s.getMediaID().intValue(), s);
        }
    }

    public void resetCurrSongs() {
        playQueue.setCurrQueue(allSongs);
    }

    public ArrayList<SongInfo> getCurrSongsFromTaggs() {
        HashSet<SongPlayOrderTuple> newSongQueue = new HashSet<>();

        for (String tagg : activeTaggs.keySet()) {
            Cursor cursor = databaseHelper.getTaggSongCursor(activeTaggs.get(tagg));
            int[][] ret = databaseHelper.getSongListForCursor(cursor);
            int[] ids = ret[0];
            int[] orders = ret[1];
            for (int i = 0; i < ids.length; i++) {
                SongPlayOrderTuple tuple = new SongPlayOrderTuple(allSongsMap.get(ids[i]), orders[i]);
                newSongQueue.add(tuple);
            }
            cursor.close();
        }

        ArrayList<SongPlayOrderTuple> temp = new ArrayList<>(newSongQueue);
        Collections.sort(temp, new SongPlayOrderComparator());

        ArrayList<SongInfo> out = new ArrayList<>();
        for (SongPlayOrderTuple t : temp) {
            out.add(t.songInfo);
        }

        return out;
    }

    private void updateCurrSongsFromTaggs() {
        HashSet<SongPlayOrderTuple> newSongQueue = new HashSet<>();

        for (String tagg : activeTaggs.keySet()) {
            Cursor cursor = databaseHelper.getTaggSongCursor(activeTaggs.get(tagg));
            int[][] ret = databaseHelper.getSongListForCursor(cursor);
            int[] ids = ret[0];
            int[] orders = ret[1];
            for (int i = 0; i < ids.length; i++) {
                SongPlayOrderTuple tuple = new SongPlayOrderTuple(allSongsMap.get(ids[i]), orders[i]);
                newSongQueue.add(tuple);
            }
            cursor.close();
        }

        ArrayList<SongPlayOrderTuple> temp = new ArrayList<>(newSongQueue);
        Collections.sort(temp, new SongPlayOrderComparator());

        ArrayList<SongInfo> out = new ArrayList<>();
        for (SongPlayOrderTuple t : temp) {
            out.add(t.songInfo);
        }

        playQueue.setCurrQueue(out);
    }

    public ArrayList<SongInfo> getDateSortedSongs(int sortMode) {
        ArrayList<SongInfo> out = new ArrayList<>(allSongs);
        Collections.sort(out, new SongComparator(sortMode));

        return out;
    }

    private void updateCurrSongsFromSorted(int sortMode) {
        ArrayList<SongInfo> out = new ArrayList<>(allSongs);
        Collections.sort(out, new SongComparator(sortMode));

        playQueue.setCurrQueue(out);
    }

    public void updateCurrQueue(int queueType) {
        switch (queueType) {
            case (TYPE_ALL_SONGS):
                resetCurrSongs();
                break;
            case (TYPE_TAGG):
                updateCurrSongsFromTaggs();
                break;
            case (TYPE_RECENT):
                updateCurrSongsFromSorted(SongComparator.SORT_DATE_DESC);
                break;
        }
    }

    public ArrayList<String> getTaggs() {
        return new ArrayList<>(taggs.keySet());
    }

    public void addTagg(String tagg) {
        databaseHelper.createTagg(tagg);
        taggs.put(tagg, ((Long) databaseHelper.getTaggID(tagg)).intValue());
    }

    public void removeTagg(String tagg) {
        taggs.remove(tagg);
        activeTaggs.remove(tagg);
        databaseHelper.deleteTagg(tagg);
    }

    public void updateSongTaggRelations(SongInfo songInfo, ArrayList<String> updateTaggs) {
        for (String tagg : taggs.keySet()) {
            databaseHelper.removeFromTagg(songInfo.getMediaID(), taggs.get(tagg));
        }

        for (String tagg : updateTaggs) {
            databaseHelper.addToTagg(new long[] {songInfo.getMediaID()}, taggs.get(tagg));
        }
    }

    public void addSongTaggRelation(SongInfo songInfo, String tagg) {
        databaseHelper.addToTagg(new long[] {songInfo.getMediaID()}, taggs.get(tagg));
    }

    public void removeSongTaggRelation(SongInfo songInfo, String tagg) {
        databaseHelper.removeFromTagg(songInfo.getMediaID(), taggs.get(tagg));
    }

    public ArrayList<String> getSongsRelatedTaggs(SongInfo songInfo) {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> taggList = new ArrayList<>();

        for (String t : taggs.keySet()) {
            int[] songs = databaseHelper.getSongListFromTagg(taggs.get(t));
            for (int i : songs) {
                ids.add(i);
                taggList.add(t);
            }
        }

        ArrayList<String> out = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            if (allSongsMap.get(ids.get(i)).equals(songInfo)) {
                out.add(taggList.get(i));
            }
        }

        return out;
    }

    public ArrayList<SongInfo> getAllSongs() {
        return allSongs;
    }

    public void activateTagg(String tagg) {
        activeTaggs.put(tagg, taggs.get(tagg));
    }

    public void deactivateTagg(String tagg) {
        activeTaggs.remove(tagg);
    }

    public ArrayList<String> getActiveTaggs() {
        return new ArrayList<>(activeTaggs.keySet());
    }
}
