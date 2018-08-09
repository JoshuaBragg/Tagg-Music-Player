package gg.joshbra.tagg;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.Helpers.SongPlayOrderTriplet;

public class PlayQueue {
    private ArrayList<SongPlayOrderTriplet> currQueue, addedToQueue;
    private SongPlayOrderTriplet currSong;
    private int shuffleMode;
    private int repeatMode;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final PlayQueue self = new PlayQueue();

    private PlayQueue() {
        currQueue = new ArrayList<>();
        addedToQueue = new ArrayList<>();
        currSong = null;
        shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;
        repeatMode = PlaybackStateCompat.REPEAT_MODE_ALL;
    }

    public static PlayQueue getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public void setCurrSong(SongInfo songInfo) {
        currSong = currQueue.get(getSongIndex(songInfo));
    }

    public int getSongIndex(SongInfo songInfo) {
        // TODO: consider going back to indexOffset since that may be a faster method.
        for (int i = 0; i < currQueue.size(); i++) {
            if (currQueue.get(i).getSongInfo().equals(songInfo)) {
                return i;
            }
        }
        return -1;
    }

    public void setCurrQueue(ArrayList<SongInfo> currQueue) {
        ArrayList<SongPlayOrderTriplet> triplets = new ArrayList<>();

        for (int i = 0; i < currQueue.size(); i++) {
            triplets.add(new SongPlayOrderTriplet(currQueue.get(i), i, i));
        }

        shuffle(triplets);

        this.currQueue = triplets;
        addedToQueue = new ArrayList<>();
    }

    private void shuffle(ArrayList<SongPlayOrderTriplet> triplets) {
        ArrayList<Integer> orders = new ArrayList<>();
        for (int i = 0; i < triplets.size(); i++) {
            orders.add(i);
        }
        Collections.shuffle(orders);

        for (int i = 0; i < triplets.size(); i++) {
            triplets.get(i).setOrderShuffle(orders.get(i));
        }
    }

    public void insertSongNextInQueue(SongInfo songInfo) {
        int index = currQueue.indexOf(currSong) + 1;

        for (SongPlayOrderTriplet s : currQueue) {
            if (s.getOrderSeq() >= index) {
                s.setOrderSeq(s.getOrderSeq() + 1);
            }
        }

        SongPlayOrderTriplet triplet = new SongPlayOrderTriplet(songInfo, index, index);

        currQueue.add(index, triplet);

        addedToQueue.add(triplet);

        shuffle(currQueue);
    }

    public void addIntoQueue(ArrayList<SongPlayOrderTriplet> songs) {
        // TODO: make this run in faster time
        for (SongPlayOrderTriplet triplet : songs) {
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderSeq() >= triplet.getOrderSeq()) {
                    s.setOrderSeq(s.getOrderSeq() + 1);
                }
            }
            currQueue.add(triplet.getOrderSeq(), triplet);
        }

        shuffle(currQueue);
    }

    public SongInfo getCurrSong() {
        if (currSong == null) { return null; }
        return currSong.getSongInfo();
    }

    public ArrayList<SongPlayOrderTriplet> getSongsAddedToQueue() {
        return addedToQueue;
    }

    public SongInfo getNextSong() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            return currSong.getSongInfo();
        }
        return next();
    }

    public SongInfo getPrevSong() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            return currSong.getSongInfo();
        }
        return prev();
    }

    private SongInfo next() {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                if (currSong.getOrderSeq() + 1 == currQueue.size()) {
                    currSong = currQueue.get(0);
                    return currQueue.get(0).getSongInfo();
                }
            }
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE && currSong.getOrderSeq() + 1 == currQueue.size()) {
                return null;
            }
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderSeq() == currSong.getOrderSeq() + 1) {
                    currSong = s;
                    return s.getSongInfo();
                }
            }
        } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                if (currSong.getOrderShuffle() + 1 == currQueue.size()) {
                    currSong = currQueue.get(0);
                    return currQueue.get(0).getSongInfo();
                }
            }
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE && currSong.getOrderShuffle() + 1 == currQueue.size()) {
                return null;
            }
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderShuffle() == currSong.getOrderShuffle() + 1) {
                    currSong = s;
                    return s.getSongInfo();
                }
            }
        }
        return null;
    }

    private SongInfo prev() {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                if (currSong.getOrderSeq() - 1 < 0) {
                    currSong = currQueue.get(currQueue.size() - 1);
                    return currQueue.get(currQueue.size() - 1).getSongInfo();
                }
            }
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE && currSong.getOrderSeq() - 1 < 0) {
                return null;
            }
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderSeq() == currSong.getOrderSeq() - 1) {
                    currSong = s;
                    return s.getSongInfo();
                }
            }
        } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                if (currSong.getOrderShuffle() - 1 < 0) {
                    currSong = currQueue.get(currQueue.size() - 1);
                    return currQueue.get(currQueue.size() - 1).getSongInfo();
                }
            }
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE && currSong.getOrderShuffle() - 1 < 0) {
                return null;
            }
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderShuffle() == currSong.getOrderShuffle() - 1) {
                    currSong = s;
                    return s.getSongInfo();
                }
            }
        }
        return null;
    }

    public SongInfo getSongByID(String id) {
        for (SongPlayOrderTriplet s : currQueue) {
            if (s.getSongInfo().getMediaID().toString().equals(id)) {
                return s.getSongInfo();
            }
        }
        return null;
    }

    public Long getCurrentMediaId() {
        if (currSong == null) { return null; }
        return currSong.getSongInfo().getMediaID();
    }

    public void setRepeatMode(int mode) {
        repeatMode = mode;
        MediaControllerHolder.getMediaController().getTransportControls().setRepeatMode(mode);
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public int getNextRepeatMode() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE)
            return PlaybackStateCompat.REPEAT_MODE_ALL;
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)
            return PlaybackStateCompat.REPEAT_MODE_ONE;
        return  PlaybackStateCompat.REPEAT_MODE_NONE;
    }

    public void setShuffleMode(int mode) {
        shuffleMode = mode;
        shuffle(currQueue);
        MediaControllerHolder.getMediaController().getTransportControls().setShuffleMode(mode);
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    private ArrayList<SongInfo> tripletToSongInfoConvert(ArrayList<SongPlayOrderTriplet> songPlayOrderTriplets) {
        ArrayList<SongInfo> out = new ArrayList<>();
        for (SongPlayOrderTriplet songPlayOrderTriplet : songPlayOrderTriplets) {
            out.add(songPlayOrderTriplet.getSongInfo());
        }
        return out;
    }

    public static String getRoot() { return "root"; }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (SongPlayOrderTriplet song : currQueue) {
            result.add(new MediaBrowserCompat.MediaItem(song.getSongInfo().getMediaMetadataCompat().getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }
}
