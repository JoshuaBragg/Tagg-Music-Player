package gg.joshbra.tagg;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayQueue {
    private static ArrayList<SongInfo> currQueue, currQueueShuffled;
    private SongInfo currSong;
    private static int shuffleMode;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final PlayQueue self = new PlayQueue();

    private PlayQueue() {
        currQueue = new ArrayList<>();
        currSong = null;
        shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;
    }

    public static PlayQueue getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public ArrayList<SongInfo> getCurrQueue() {
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ? currQueueShuffled : currQueue;
    }

    public void setCurrQueue(ArrayList<SongInfo> currQueue) {
        PlayQueue.currQueue = currQueue;
        PlayQueue.currQueueShuffled = shuffle(currQueue);
    }

    public SongInfo getCurrSong() {
        return currSong;
    }

    public void setCurrSong(SongInfo currSong) {
        this.currSong = currSong;
    }

    public SongInfo getNextSong() {
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ?
                currQueueShuffled.indexOf(currSong) + 1 == currQueueShuffled.size() ? currQueueShuffled.get(0) : currQueueShuffled.get(currQueueShuffled.indexOf(currSong) + 1) :
                currQueue.indexOf(currSong) + 1 == currQueue.size() ? currQueue.get(0) : currQueue.get(currQueue.indexOf(currSong) + 1);
    }

    public SongInfo getPrevSong() {
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ?
                currQueueShuffled.indexOf(currSong) - 1 < 0 ? currQueueShuffled.get(currQueueShuffled.size() - 1) : currQueueShuffled.get(currQueueShuffled.indexOf(currSong) - 1) :
                currQueue.indexOf(currSong) - 1 < 0 ? currQueue.get(currQueue.size() - 1) : currQueue.get(currQueue.indexOf(currSong) - 1);
    }

    public SongInfo getSongByID(String id) {
        for (SongInfo s : currQueue) {
            if (s.getMediaID().equals(id)) {
                return s;
            }
        }
        return null;
    }

    public String getCurrentMediaId() {
        return currSong.getMediaID();
    }

    public static void setShuffleMode(int mode) {
        shuffleMode = mode;
    }

    public static int getShuffleMode() {
        return shuffleMode;
    }

    private ArrayList<SongInfo> shuffle(ArrayList<SongInfo> queue) {
        // TODO: make shuffling better
        ArrayList<SongInfo> temp = new ArrayList<>(queue);
        Collections.shuffle(temp);
        return temp;
    }

    public static String getRoot() { return "root"; }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (SongInfo song : (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ? currQueueShuffled : currQueue)) {
            result.add(new MediaBrowserCompat.MediaItem(song.getMediaMetadataCompat().getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }
}
