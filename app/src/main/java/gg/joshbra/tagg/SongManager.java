package gg.joshbra.tagg;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class SongManager {
    private ArrayList<SongInfo> allSongs;
    private ArrayList<String> taggs, activeTaggs;

    private PlayQueue playQueue;

    private DatabaseHelper databaseHelper;

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
        playQueue.setCurrQueue(allSongs);

        taggs = new ArrayList<>();
        activeTaggs = new ArrayList<>();
    }

    public void readSongs() {
        Cursor data = databaseHelper.getSongs();

        while (data.moveToNext()) {
            SongInfo s = new SongInfo(data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), Long.parseLong(data.getString(6)), data.getString(7), data.getString(8));

            ArrayList<String> taggs = databaseHelper.getSongsRelatedTaggs(data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), Long.parseLong(data.getString(6)), data.getString(7), data.getString(8));

            for (String t : taggs) {
                s.addTagg(t);
            }

            allSongs.add(s);
        }
        data.close();
    }

    public void readTaggs() {
        Cursor data = databaseHelper.getTaggs();

        while (data.moveToNext()) {
            taggs.add(data.getString(1));
        }

        Collections.sort(taggs);

        data.close();
    }

    public void checkSongsForChanges(ArrayList<SongInfo> songs) {
        Collections.sort(songs);
        Collections.sort(this.allSongs);

        int i = 0;

        while (i < this.allSongs.size()) {
            if (!songs.contains(this.allSongs.get(i))) {
                removeSong(this.allSongs.get(i));
            } else {
                i++;
            }
        }

        for (SongInfo s : songs) {
            if (!this.allSongs.contains(s)) {
                addSong(s);
            }
        }
    }

    public void resetCurrSongs() {
        playQueue.setCurrQueue(allSongs);
    }

    public ArrayList<SongInfo> getCurrSongsFromTaggs() {
        ArrayList<SongInfo> newSongQueue = new ArrayList<>();

        // TODO: instead of iterating through list and calling several times just make databaseHelper method take array of taggs and implode
        for (String tagg : activeTaggs) {
            for (SongInfo s : getTaggsRelatedSongs(tagg)) {
                if (!newSongQueue.contains(s)) {
                    newSongQueue.add(s);
                }
            }
        }

        return newSongQueue;
    }

    public void updateCurrSongsFromTaggs() {
        ArrayList<SongInfo> newSongQueue = new ArrayList<>();

        // TODO: instead of iterating through list and calling several times just make databaseHelper method take array of taggs and implode
        for (String tagg : activeTaggs) {
            for (SongInfo s : getTaggsRelatedSongs(tagg)) {
                if (!newSongQueue.contains(s)) {
                    newSongQueue.add(s);
                }
            }
        }

        playQueue.setCurrQueue(newSongQueue);
    }

    public ArrayList<String> getTaggs() {
        return taggs;
    }

    private void addSong(SongInfo songInfo) {
        databaseHelper.addSong(songInfo.getMediaID(), songInfo.getSongName(), songInfo.getArtistName(), songInfo.getSongUrl(), songInfo.getAlbumName(), songInfo.getDuration(), songInfo.getAlbumArt(), songInfo.getDateAdded());
        this.allSongs.add(songInfo);
    }

    private void removeSong(SongInfo songInfo) {
        databaseHelper.removeSong(songInfo.getMediaID(), songInfo.getSongName(), songInfo.getArtistName(), songInfo.getSongUrl(), songInfo.getAlbumName(), songInfo.getDuration(), songInfo.getAlbumArt(), songInfo.getDateAdded());
        this.allSongs.remove(songInfo);
    }

    public void addTagg(String tagg) {
        if (!taggs.contains(tagg)) {
            taggs.add(tagg);
        }
        databaseHelper.addTagg(tagg);
    }

    public void removeTagg(String tagg) {
        // TODO: also remove all occurrences of this tagg in map
        if (taggs.contains(tagg)) {
            taggs.remove(tagg);
        }
        if (activeTaggs.contains(tagg)) {
            activeTaggs.remove(tagg);
        }
        databaseHelper.removeTagg(tagg);
    }

    public void updateSongTaggRelations(SongInfo songInfo, ArrayList<String> updateTaggs) {
        HashSet<String> updateSet = new HashSet<>(updateTaggs);
        HashSet<String> currSet = new HashSet<>(getSongsRelatedTaggs(songInfo));

        updateSet.retainAll(currSet);

        for (String t : updateTaggs) {
            if (!updateSet.contains(t)) {
                addSongTaggRelation(songInfo, t);
            }
        }

        for (String t : currSet) {
            if (!updateSet.contains(t)) {
                removeSongTaggRelation(songInfo, t);
            }
        }
    }

    public boolean addSongTaggRelation(SongInfo songInfo, String tagg) {
        return databaseHelper.addSongTaggRelation(songInfo.getMediaID(), songInfo.getSongName(), songInfo.getArtistName(), songInfo.getSongUrl(), songInfo.getAlbumName(), songInfo.getDuration(), songInfo.getAlbumArt(), songInfo.getDateAdded(), tagg);
    }

    public boolean removeSongTaggRelation(SongInfo songInfo, String tagg) {
        return databaseHelper.removeSongTaggRelation(songInfo.getMediaID(), songInfo.getSongName(), songInfo.getArtistName(), songInfo.getSongUrl(), songInfo.getAlbumName(), songInfo.getDuration(), songInfo.getAlbumArt(), songInfo.getDateAdded(), tagg);
    }

    private ArrayList<SongInfo> getTaggsRelatedSongs(String tagg) {
        ArrayList<String[]> s = databaseHelper.getTaggsRelatedSongs(tagg);
        ArrayList<SongInfo> out = new ArrayList<>();

        for (String[] ar : s) {
            out.add(new SongInfo(ar[0], ar[1], ar[2], ar[3], ar[4], Long.parseLong(ar[5]), ar[6], ar[7]));
        }

        return out;
    }

    public ArrayList<String> getSongsRelatedTaggs(SongInfo songInfo) {
        return databaseHelper.getSongsRelatedTaggs(songInfo.getMediaID(), songInfo.getSongName(), songInfo.getArtistName(), songInfo.getSongUrl(), songInfo.getAlbumName(), songInfo.getDuration(), songInfo.getAlbumArt(), songInfo.getDateAdded());
    }

    public ArrayList<SongInfo> getAllSongs() {
        return allSongs;
    }

    public void activateTagg(String tagg) {
        if (!activeTaggs.contains(tagg)) {
            activeTaggs.add(tagg);
        }
    }

    public void deactivateTagg(String tagg) {
        if (activeTaggs.contains(tagg)) {
            activeTaggs.remove(tagg);
        }
    }

    public ArrayList<String> getActiveTaggs() {
        return activeTaggs;
    }
}
