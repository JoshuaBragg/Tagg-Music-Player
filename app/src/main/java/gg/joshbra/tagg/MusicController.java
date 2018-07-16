package gg.joshbra.tagg;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MusicController extends Observable implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private PlayQueue playQueue;

    private int state;

    private final Callback callback;
    private final AudioManager audioManager;
    private final Context context;

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

    ////////////////////////////// Observer  ///////////////////////////////

    public MusicController(Context context, Callback callback) {
        mediaPlayer = new MediaPlayer();
        playQueue = PlayQueue.getSelf();
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.callback = callback;
    }

    public void playSong(SongInfo songInfo) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            try {
                mediaPlayer.reset();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playSong(playQueue.getNextSong());
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

            playQueue.setCurrSong(songInfo);
            notifyAllObservers(playQueue.getCurrSong());
            notifyAllObservers(true);
            state = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playSong() {
        mediaPlayer.start();
        notifyAllObservers(true);
        state = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }

    public void pauseSong() {
        mediaPlayer.pause();
        notifyAllObservers(false);
        state = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        state = PlaybackStateCompat.STATE_STOPPED;
        updatePlaybackState();
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

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        boolean gotFullFocus = false;
        boolean canDuck = false;
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            gotFullFocus = true;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
        }

        if (gotFullFocus || canDuck) {
            if (mediaPlayer != null) {
                // TODO: Figure ou thwat removing if statement did
                mediaPlayer.start();
                state = PlaybackStateCompat.STATE_PLAYING;
                updatePlaybackState();
                float volume = canDuck ? 0.2f : 1.0f;
                mediaPlayer.setVolume(volume, volume);
            }
        } else if (state == PlaybackStateCompat.STATE_PLAYING) {
            mediaPlayer.pause();
            state = PlaybackStateCompat.STATE_PAUSED;
            updatePlaybackState();
        }
    }

    public interface Callback {
        void onPlaybackStatusChanged(PlaybackStateCompat state);
    }

    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                        | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        if (isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    public int getCurrentStreamPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public void updatePlaybackState() {
        if (callback == null) {
            return;
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());

        stateBuilder.setState(state, getCurrentStreamPosition(), 1.0f, SystemClock.elapsedRealtime());
        callback.onPlaybackStatusChanged(stateBuilder.build());
    }
}
