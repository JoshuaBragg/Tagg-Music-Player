package gg.joshbra.tagg;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PlayQueue {
    private static ArrayList<SongInfo> allSongs, currQueue;
    private SongInfo currSong;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final PlayQueue self = new PlayQueue();

    private PlayQueue() {
        allSongs = new ArrayList<>();
        currQueue = new ArrayList<>();
        currSong = null;
    }

    public static PlayQueue getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public ArrayList<SongInfo> getAllSongs() {
        return allSongs;
    }

    public void setAllSongs(ArrayList<SongInfo> allSongs) {
        PlayQueue.allSongs = allSongs;
    }

    public ArrayList<SongInfo> getCurrQueue() {
        return currQueue;
    }

    public void setCurrQueue(ArrayList<SongInfo> currQueue) {
        PlayQueue.currQueue = currQueue;
    }

    public SongInfo getCurrSong() {
        return currSong;
    }

    public void setCurrSong(SongInfo currSong) {
        this.currSong = currSong;
    }

    public SongInfo getNextSong() {
        currSong = currQueue.indexOf(currSong) + 1 == currQueue.size() ? currQueue.get(0) : currQueue.get(currQueue.indexOf(currSong) + 1);
        return currSong;
    }

    public SongInfo getPrevSong() {
        currSong = currQueue.indexOf(currSong) - 1 < 0 ? currQueue.get(currQueue.size() - 1) : currQueue.get(currQueue.indexOf(currSong) - 1);
        return currSong;
    }

    public SongInfo getSongByID(String id) {
        for (SongInfo s : currQueue) {
            if (s.getMediaID().equals(id)) {
                return s;
            }
        }
        Log.i("d", "oh no that one didnt exist woops " + id);
        return null;
    }

    public String getCurrentMediaId() {
        return currSong.getMediaID();
    }

    public static String getRoot() { return "root"; }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (SongInfo song : currQueue) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            song.getMediaMetadataCompat().getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }
}
