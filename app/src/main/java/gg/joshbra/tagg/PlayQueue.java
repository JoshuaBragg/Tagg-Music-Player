package gg.joshbra.tagg;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gg.joshbra.tagg.Comparators.SongPlayOrderTripletComparator;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.Helpers.SongPlayOrderTriplet;

/**
 * Stores and provides an interface for obtaining songs in their order
 */
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

    /**
     * Sets the current song
     * @param songInfo The song to set the current song to
     */
    public void setCurrSong(SongInfo songInfo) {
        currSong = currQueue.get(getSongIndex(songInfo));
    }

    /**
     * If the song at the position given in the current queue is the same as the songInfo then that
     * Is set as the current song, if not then the first occurrence of songInfo in the queue is set
     * to the current song
     * @param songInfo The song to search for
     * @param position the position to check
     */
    public void setCurrSong(SongInfo songInfo, int position) {
        if (position != MusicController.DEFAULT_SONG_POSIITON && currQueue.get(position).getSongInfo().equals(songInfo)) {
            currSong = currQueue.get(position);
            return;
        }
        setCurrSong(songInfo);
    }

    /**
     * Gets the index of the song given
     * @param songInfo The songInfo to search for
     * @return Returns the index of this song in the queue
     */
    public int getSongIndex(SongInfo songInfo) {
        // TODO: consider going back to indexOffset since that may be a faster method.
        for (int i = 0; i < currQueue.size(); i++) {
            if (currQueue.get(i).getSongInfo().equals(songInfo)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the current queue
     * @param currQueue The songs the queue should consist of
     */
    public void setCurrQueue(ArrayList<SongInfo> currQueue) {
        ArrayList<SongPlayOrderTriplet> triplets = new ArrayList<>();

        for (int i = 0; i < currQueue.size(); i++) {
            triplets.add(new SongPlayOrderTriplet(currQueue.get(i), i, i));
        }

        shuffle(triplets);

        this.currQueue = triplets;
        addedToQueue = new ArrayList<>();
    }

    /**
     * Shuffles the order of the currQueue's shuffledOrders
     * @param triplets The array of SongPlayOrderTriples to shuffle
     */
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

    /**
     * Inserts the given song next into the queue
     * @param songInfo The song to be inserted into the queue
     */
    public void insertSongNextInQueue(SongInfo songInfo) {
        int indexSeq = currSong.getOrderSeq() + 1;
        int indexShuffle = currSong.getOrderShuffle() + 1;

        for (SongPlayOrderTriplet s : currQueue) {
            if (s.getOrderSeq() >= indexSeq) {
                s.setOrderSeq(s.getOrderSeq() + 1);
            }
            if (s.getOrderShuffle() >= indexShuffle) {
                s.setOrderShuffle(s.getOrderShuffle() + 1);
            }
        }

        SongPlayOrderTriplet triplet = new SongPlayOrderTriplet(songInfo, indexSeq, indexShuffle);

        currQueue.add(indexSeq, triplet);

        addedToQueue.add(triplet);
    }

    /**
     * Adds all songs given into the queue
     * @param songs The songs to be added into queue
     */
    public void addIntoQueue(ArrayList<SongPlayOrderTriplet> songs) {
        // TODO: make this run in faster time
        for (SongPlayOrderTriplet triplet : songs) {
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderSeq() >= triplet.getOrderSeq()) {
                    s.setOrderSeq(s.getOrderSeq() + 1);
                }
            }
            try {
                currQueue.add(triplet.getOrderSeq(), triplet);
            } catch (IndexOutOfBoundsException e) {}
        }

        shuffle(currQueue);
    }

    /**
     * Gets the order of the current song
     * @return The position of the current song in the queue (will return the position depending on the shuffle mode)
     */
    public int getCurrSongOrder() {
        if (currSong == null) { return -1; }
        return shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE ? currSong.getOrderSeq() : currSong.getOrderShuffle();
    }

    /**
     * Get the current song
     * @return Returns the current song
     */
    public SongInfo getCurrSong() {
        if (currSong == null) { return null; }
        return currSong.getSongInfo();
    }

    /**
     * Gets all the songs that have been added to the queue
     * @return Array of songs added to the queue
     */
    public ArrayList<SongPlayOrderTriplet> getSongsAddedToQueue() {
        return addedToQueue;
    }

    /**
     * Gets the next song to be played and updates the currSong to the return value
     * @return The song to be played next
     */
    public SongInfo getNextSong() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            return currSong.getSongInfo();
        }
        return next();
    }

    /**
     * Gets the prev song played and updates the currSong to the return value
     * @return The previous song in the queue
     */
    public SongInfo getPrevSong() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            return currSong.getSongInfo();
        }
        return prev();
    }

    /**
     * Helper method that actually finds next song
     * Has to take into account the shuffle and repeat modes to find the next song
     * @return The next song
     */
    private SongInfo next() {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                if (currSong.getOrderSeq() + 1 == currQueue.size()) {
                    for (SongPlayOrderTriplet s : currQueue) {
                        if (s.getOrderSeq() == 0) {
                            currSong = s;
                            return currSong.getSongInfo();
                        }
                    }
                    // Song should have been found, if not stop playback
                    return null;
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
                    for (SongPlayOrderTriplet s : currQueue) {
                        if (s.getOrderShuffle() == 0) {
                            currSong = s;
                            return currSong.getSongInfo();
                        }
                    }
                    // Song should have been found, if not stop playback
                    return null;
                }
            }
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE && currSong.getOrderShuffle() + 1 == currQueue.size()) {
                return null;
            }
            for (SongPlayOrderTriplet s : currQueue) {
                if (s.getOrderShuffle() == currSong.getOrderShuffle() + 1) {
                    currSong = s;
                    return currSong.getSongInfo();
                }
            }
        }
        return null;
    }

    /**
     * Helper method that actually finds prev song
     * Has to take into account the shuffle and repeat modes to find the prev song
     * @return The prev song
     */
    private SongInfo prev() {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                if (currSong.getOrderSeq() - 1 < 0) {
                    for (SongPlayOrderTriplet s : currQueue) {
                        if (s.getOrderSeq() == currQueue.size() - 1) {
                            currSong = s;
                            return currSong.getSongInfo();
                        }
                    }
                    // Song should have been found, if not stop playback
                    return null;
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
                    for (SongPlayOrderTriplet s : currQueue) {
                        if (s.getOrderShuffle() == currQueue.size() - 1) {
                            currSong = s;
                            return currSong.getSongInfo();
                        }
                    }
                    // Song should have been found, if not stop playback
                    return null;
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

    /**
     * Given a songID return the songInfo associated with this ID
     * @param id The ID to find
     * @return The song with the given ID or null if no song with such an ID is found
     */
    public SongInfo getSongByID(String id) {
        for (SongPlayOrderTriplet s : currQueue) {
            if (s.getSongInfo().getMediaID().toString().equals(id)) {
                return s.getSongInfo();
            }
        }
        return null;
    }

    /**
     * Gets the MediaID of the current song
     * @return The MediaID of currSong
     */
    public Long getCurrentMediaId() {
        if (currSong == null) { return null; }
        return currSong.getSongInfo().getMediaID();
    }

    /**
     * Sets the repeatMode
     * @param mode The new repeat mode
     */
    public void setRepeatMode(int mode) {
        repeatMode = mode;
        MediaControllerHolder.getMediaController().getTransportControls().setRepeatMode(mode);
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    /**
     * Gets the next repeat mode
     * @return The next sequential repeat mode
     */
    public int getNextRepeatMode() {
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE)
            return PlaybackStateCompat.REPEAT_MODE_ALL;
        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)
            return PlaybackStateCompat.REPEAT_MODE_ONE;
        return  PlaybackStateCompat.REPEAT_MODE_NONE;
    }

    /**
     * Sets the shuffleMode
     * @param mode The new shuffle mode
     */
    public void setShuffleMode(int mode) {
        shuffleMode = mode;
        shuffle(currQueue);
        MediaControllerHolder.getMediaController().getTransportControls().setShuffleMode(mode);
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    /**
     * Converts array of songplayordertriplets to array of songInfos
     * @param songPlayOrderTriplets The array to be converted
     * @return The converted array
     */
    public static ArrayList<SongInfo> tripletToSongInfoConvert(ArrayList<SongPlayOrderTriplet> songPlayOrderTriplets) {
        ArrayList<SongInfo> out = new ArrayList<>();
        for (SongPlayOrderTriplet songPlayOrderTriplet : songPlayOrderTriplets) {
            out.add(songPlayOrderTriplet.getSongInfo());
        }
        return out;
    }

    public static ArrayList<SongInfo> tripletToSongInfoConvert(ArrayList<SongPlayOrderTriplet> songPlayOrderTriplets, SongPlayOrderTripletComparator songPlayOrderTripletComparator) {
        Collections.sort(songPlayOrderTriplets, songPlayOrderTripletComparator);
        return tripletToSongInfoConvert(songPlayOrderTriplets);
    }

    public ArrayList<SongPlayOrderTriplet> getCurrQueue() {
        return currQueue;
    }

    /**
     * Returns "root"
     * @return "root"
     */
    public static String getRoot() { return "root"; }

    /**
     * Gets a List of all mediaItems
     * @return List of mediaItems
     */
    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (SongPlayOrderTriplet song : currQueue) {
            result.add(new MediaBrowserCompat.MediaItem(song.getSongInfo().getMediaMetadataCompat().getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }
}
