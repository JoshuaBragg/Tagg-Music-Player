package gg.joshbra.tagg;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;

public class PlayQueue {
    private static ArrayList<SongInfo> currQueue, currQueueShuffled;
    private SongInfo currSong;
    private static int shuffleMode;
    private static int repeatMode;
    private int index = -1, shuffledIndex = -1;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final PlayQueue self = new PlayQueue();

    private PlayQueue() {
        currQueue = new ArrayList<>();
        currSong = null;
        shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;
        repeatMode = PlaybackStateCompat.REPEAT_MODE_ALL;
    }

    public static PlayQueue getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public ArrayList<SongInfo> getCurrQueue() {
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ? currQueueShuffled : currQueue;
    }

    public int getSongIndex(SongInfo songInfo) {
        return currQueue.indexOf(songInfo);
    }

    public void setCurrQueue(ArrayList<SongInfo> currQueue) {
        PlayQueue.currQueue = currQueue;
        shuffle();
    }

    public SongInfo getCurrSong() {
        return currSong;
    }

    public void setCurrSong(SongInfo currSong) {
        this.currSong = currSong;
        index = currQueue.indexOf(currSong);
        shuffledIndex = currQueueShuffled.indexOf(currSong);
    }

    public SongInfo getNextSong() {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ? shuffledIndex == -1 : index == -1) {
            return null;
        }
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE || currSong == null) {
            return currSong;
        }
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
            return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ?
                    shuffledIndex + 1 == currQueueShuffled.size() ? null : currQueueShuffled.get(++shuffledIndex) :
                    index + 1 == currQueue.size() ? null : currQueue.get(++index);
        }
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ?
                shuffledIndex + 1 == currQueueShuffled.size() ? currQueueShuffled.get(0) : currQueueShuffled.get(++shuffledIndex) :
                index + 1 == currQueue.size() ? currQueue.get(0) : currQueue.get(++index);
    }

    public SongInfo getPrevSong() {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ? shuffledIndex == -1 : index == -1) {
            return null;
        }
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE || currSong == null) {
            return currSong;
        }
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
            return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ?
                    shuffledIndex - 1 < 0 ? null : currQueueShuffled.get(--shuffledIndex) :
                    index - 1 < 0 ? null : currQueue.get(--index);
        }
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL ?
                shuffledIndex - 1 < 0 ? currQueueShuffled.get(currQueueShuffled.size() - 1) : currQueueShuffled.get(--shuffledIndex) :
                index - 1 < 0 ? currQueue.get(currQueue.size() - 1) : currQueue.get(--index);
    }

    public SongInfo getSongByID(String id) {
        for (SongInfo s : currQueue) {
            if (s.getMediaID().toString().equals(id)) {
                return s;
            }
        }
        return null;
    }

    public Long getCurrentMediaId() {
        if (currSong == null) { return null; }
        return currSong.getMediaID();
    }

    public static void setRepeatMode(int mode) {
        repeatMode = mode;
    }

    public static int getRepeatMode() {
        return repeatMode;
    }

    public static int getNextRepeatMode() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE)
            return PlaybackStateCompat.REPEAT_MODE_ALL;
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)
            return PlaybackStateCompat.REPEAT_MODE_ONE;
        return  PlaybackStateCompat.REPEAT_MODE_NONE;
    }

    public static void setShuffleMode(int mode) {
        shuffleMode = mode;
    }

    public static int getShuffleMode() {
        return shuffleMode;
    }

    public static void shuffle() {
        currQueueShuffled = new ArrayList<>(currQueue);
        Collections.shuffle(currQueueShuffled);
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
