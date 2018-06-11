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

    private ArrayList<SongInfo> songs;
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
        HashMap out = new HashMap();
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
    }

    public static MediaController getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    public void setSongs(ArrayList<SongInfo> songs) {
        this.songs = songs;
    }

    protected void playSong(SongInfo songInfo) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (songs.indexOf(currSong) != songs.size() - 1) {
                        playSong(songs.get(songs.indexOf(currSong) + 1));
                    } else {
                        playSong(songs.get(0));
                    }
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

    public void seekTo(int ms) {
        mediaPlayer.seekTo(ms);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    protected void playSong() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            notifyAllObservers(true);
        }
    }

    protected void pauseSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            notifyAllObservers(false);
        }
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
