package com.bragg.tagg;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MediaController extends Observable {

    private ArrayList<SongInfo> songs, prevSongs;
    private MediaPlayer mediaPlayer;
    private SongInfo currSong;

    ////////////////////////////// Observer  ///////////////////////////////

    private List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer) { observers.add(observer); }

    public void detach(Observer observer) { observers.remove(observer); }

    private void notifyAllObservers(Boolean playing) {
        for (Observer o : observers) {
            o.update(this, playing);
        }
    }

    private void notifyAllObservers(SongInfo song) {
        for (Observer o : observers) {
            o.update(this, song);
        }
    }

    public HashMap getState() {
        HashMap<String, Object> out = new HashMap<>();
        out.put("playing", isPlaying());
        out.put("songName", currSong != null ? currSong.getSongName() : "");
        out.put("artistName", currSong != null ? currSong.getArtistName() : "");
        return out;
    }

    ////////////////////////////// Observer  ///////////////////////////////

    ////////////////////////////// Singleton ///////////////////////////////

    private static final MediaController self = new MediaController();

    private MediaController() {
        mediaPlayer = new MediaPlayer();
        prevSongs = new ArrayList<>();
    }

    public static MediaController getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public void setSongs(ArrayList<SongInfo> songs) {
        this.songs = songs;
        this.prevSongs = new ArrayList<>();
    }

    public void playSongFromUser(SongInfo songInfo) {
        setSongs(SongManager.getSelf().getCurrSongs());
        playSong(songInfo);
    }

    public void playSong(SongInfo songInfo) {
        try {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextSong();
                }
            });

            mediaPlayer.setDataSource(songInfo.getSongUrl());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            currSong = songInfo;

            notifyAllObservers(currSong);
            notifyAllObservers(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playSong() {
        if (!songLoaded()) { return; }

        mediaPlayer.start();
        notifyAllObservers(true);
    }

    public void pauseSong() {
        if (!songLoaded()) { return; }

        mediaPlayer.pause();
        notifyAllObservers(false);
    }

    public void nextSong() {
        if (!songLoaded()) { return; }

        prevSongs.add(currSong);

        if (songs.indexOf(currSong) != songs.size() - 1) {
            playSong(songs.get(songs.indexOf(currSong) + 1));
        } else {
            playSong(songs.get(0));
        }
    }

    public void prevSong() {
        if (!songLoaded()) { return; }

        if (getCurrentPosition() > 6000 || prevSongs.size() == 0) {
            seekTo(0);
        }
        else {
            SongInfo prev = prevSongs.get(prevSongs.size() - 1);
            prevSongs.remove(prevSongs.size() - 1);
            playSong(prev);
        }
    }

    public void seekTo(int ms) {
        mediaPlayer.seekTo(ms);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public ArrayList<SongInfo> getSongs() {
        return songs;
    }

    public SongInfo getCurrSong() {
        return currSong;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean songLoaded() {
        return currSong != null;
    }
}
