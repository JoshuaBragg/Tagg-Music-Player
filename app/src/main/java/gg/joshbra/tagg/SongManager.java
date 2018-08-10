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
import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.SongPlayOrderTuple;

/**
 * Stores and manages all the songs and taggs
 */
public class SongManager {
    private ArrayList<SongInfo> allSongs;
    private HashMap<String, Integer> taggs, activeTaggs;
    private HashMap<Integer, SongInfo> allSongsMap;
    private int queueType;

    private PlayQueue playQueue;
    private DatabaseHelper databaseHelper;

    // Different queueTypes
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

    /**
     * Initializes arrays and databaseHelper
     * @param c A context for the databaseHelper to use
     * @param songs The array of all loaded songs from device
     */
    public void initDB(Context c, ArrayList<SongInfo> songs) {
        databaseHelper = new DatabaseHelper(c);

        allSongs = new ArrayList<>();
        allSongsMap = new HashMap<>();

        Collections.sort(songs);
        allSongs = songs;
        for (SongInfo s : songs) {
            allSongsMap.put(s.getMediaID().intValue(), s);
        }

        taggs = databaseHelper.getTaggs();
        activeTaggs = FlagManager.getSelf().getActiveTaggs();

        int queueType = FlagManager.getSelf().getQueueType();
        updateCurrQueue(queueType);
        PlayQueue.getSelf().addIntoQueue(FlagManager.getSelf().getSongsAddedToQueue());
    }

    /**
     * Sets the current queue to allSongs
     */
    private void resetCurrSongs() {
        playQueue.setCurrQueue(allSongs);
    }

    /**
     * Gets the songInfo with the given ID
     * @param ID The ID of the song
     * @return The song with the given ID
     */
    public SongInfo getSongInfoFromID(int ID) {
        return allSongsMap.get(ID);
    }

    /**
     * Get all songs from the current active taggs
     * @return Array of all songs from the current active taggs
     */
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

    /**
     * Updates the currQueue in PlayQueue with the songs from active taggs
     */
    private void updateCurrSongsFromTaggs() {
        playQueue.setCurrQueue(getCurrSongsFromTaggs());
    }

    /**
     * Gets songs sorted by the given sort mode
     * @param sortMode The mode to sort the songs by
     * @return
     */
    public ArrayList<SongInfo> getDateSortedSongs(int sortMode) {
        ArrayList<SongInfo> out = new ArrayList<>(allSongs);
        Collections.sort(out, new SongComparator(sortMode));

        return out;
    }

    /**
     * Updates the currQueue in PlayQueue with date sorted songs in the given mode
     * @param sortMode The mode to sort songs by
     */
    private void updateCurrSongsFromSorted(int sortMode) {
        playQueue.setCurrQueue(getDateSortedSongs(sortMode));
    }

    /**
     * Updates the current queue type and sets the currQueue in PlayQueue
     * @param queueType The queueType
     */
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
        this.queueType = queueType;
    }

    public int getQueueType() {
        return queueType;
    }

    /**
     * Returns an array of all tagg names
     * @return Array of tagg names
     */
    public ArrayList<String> getTaggs() {
        return new ArrayList<>(taggs.keySet());
    }

    /**
     * Creates new tagg with name given
     * @param tagg The name for the new Tagg
     */
    public void addTagg(String tagg) {
        databaseHelper.createTagg(tagg);
        taggs.put(tagg, ((Long) databaseHelper.getTaggID(tagg)).intValue());
    }

    /**
     * Deletes the tagg with the given name
     * @param tagg The name of the tagg to delete
     */
    public void removeTagg(String tagg) {
        taggs.remove(tagg);
        activeTaggs.remove(tagg);
        databaseHelper.deleteTagg(tagg);
    }

    /**
     * Updates a song with the given Taggs
     * @param songInfo The song to update taggs
     * @param updateTaggs The taggs that should be active for this song
     */
    public void updateSongTaggRelations(SongInfo songInfo, ArrayList<String> updateTaggs) {
        for (String tagg : taggs.keySet()) {
            databaseHelper.removeFromTagg(songInfo.getMediaID(), taggs.get(tagg));
        }

        for (String tagg : updateTaggs) {
            databaseHelper.addToTagg(new long[] {songInfo.getMediaID()}, taggs.get(tagg));
        }
    }

    /**
     * Get all taggs associated with this song
     * @param songInfo The song to check
     * @return Array of all tagg names associated with the given song
     */
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

    /**
     * Activate the given Tagg
     * @param tagg The Tagg to activate
     */
    public void activateTagg(String tagg) {
        activeTaggs.put(tagg, taggs.get(tagg));
    }

    /**
     * Deactivate the given tagg
     * @param tagg The Tagg to deactivate
     */
    public void deactivateTagg(String tagg) {
        activeTaggs.remove(tagg);
    }

    /**
     * Return names of all activeTaggs
     * @return Array of all activeTagg names
     */
    public ArrayList<String> getActiveTaggs() {
        return new ArrayList<>(activeTaggs.keySet());
    }

    /**
     * Get the actual HashMap for activeTaggs
     * @return HashMap of tagg names to tagg ID's for active taggs
     */
    public HashMap<String, Integer> getActiveTaggsRaw() { return activeTaggs; }
}
