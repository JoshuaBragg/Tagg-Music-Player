package gg.joshbra.tagg;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import gg.joshbra.tagg.Activities.AlbumSongListActivity;
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
    public static final int TYPE_ALL_SONGS = 0, TYPE_TAGG = 1, TYPE_RECENT = 2, TYPE_ALBUM = 3;

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
        HashMap<String, Integer> temp = FlagManager.getSelf().getActiveTaggs();
        activeTaggs = new HashMap<>();
        for (String s : temp.keySet()) {
            if (taggs.containsKey(s)) {
                activeTaggs.put(s, temp.get(s));
            }
        }

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
     * Gets the songs from the given album
     * @param album The album to retrieve songs for
     * @return An array of songs that belong to the given album
     */
    public ArrayList<SongInfo> getAlbumSongs(AlbumInfo album) {
        ArrayList<SongInfo> out = new ArrayList<>();

        for (SongInfo song : allSongs) {
            if (song.getAlbumID().equals(String.valueOf(album.getAlbumID()))) {
                out.add(song);
            }
        }

        return out;
    }

    /**
     * Updates the current play queue to the songs from the current album
     *
     * If the current album is null then the play queue is reset to use all songs
     */
    private void updateCurrSongsFromAlbum() {
        if (AlbumSongListActivity.getCurrAlbum() == null) {
            resetCurrSongs();
            return;
        }
        playQueue.setCurrQueue(getAlbumSongs(AlbumSongListActivity.getCurrAlbum()));
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
            case (TYPE_ALBUM):
                updateCurrSongsFromAlbum();
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
     * Renames Tagg
     * @param oldTaggName The name of the Tagg to change
     * @param newTaggName The name to update the Tagg to
     */
    public void renameTagg(String oldTaggName, String newTaggName) {
        databaseHelper.renameTagg(newTaggName, taggs.get(oldTaggName));

        for (String taggName : taggs.keySet()) {
            if (taggName.equals(oldTaggName)) {
                taggs.put(newTaggName, taggs.get(taggName));
                taggs.remove(taggName);
                break;
            }
        }

        for (String taggName : activeTaggs.keySet()) {
            if (taggName.equals(oldTaggName)) {
                activeTaggs.put(newTaggName, activeTaggs.get(taggName));
                activeTaggs.remove(taggName);
                break;
            }
        }
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

    /**
     * Given a Tagg name returns a string of details in the format "num songs | duration of tagg"
     * @param taggName The tagg to retrieve details for
     * @return A parsed string of tagg details
     */
    public String getTaggDetailsString(String taggName) {
        int[] songIDs = databaseHelper.getSongListFromTagg(taggs.get(taggName));

        // To number of songs associated with this Tagg
        int numSongs = songIDs.length;

        // taggLength is duration in ms
        int taggLength = 0;
        for (int id : songIDs) {
            taggLength += allSongsMap.get(id).getDuration();
        }

        // taggLength now in seconds
        taggLength = taggLength / 1000;
        int m = ((taggLength - (taggLength % 60)) / 60);

        String taggLengthOut;

        if (numSongs == 0) {
            // There are no songs
            taggLengthOut = "0 minutes";
        } else if (m == 0) {
            // The length of songs is less than one minute
            taggLengthOut = "< 1 minute";
        } else if (m > 59) {
            // The length of songs is over an hour so we must calculate the amount of hours
            int totalMin = m;
            m = m % 60;
            int h = ((totalMin - (totalMin % 60))/60);
            taggLengthOut = h + " hour" + (h == 1 ? "" : "s") + (m == 0 ? "" : " and " + m + " minute" + (m == 1 ? "" : "s"));
        } else {
            // The length of songs is less than hour and greater than one minute
            taggLengthOut = m + " minute" + (m == 1 ? "" : "s");
        }

        return numSongs + " songs | " + taggLengthOut;
    }
}
