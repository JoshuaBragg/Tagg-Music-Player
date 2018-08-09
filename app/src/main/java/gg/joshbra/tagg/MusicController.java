package gg.joshbra.tagg;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;

public class MusicController implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private PlayQueue playQueue;
    private int state;

    private final Callback callback;

    public static final int PLAY_BY_USER = 0, PLAY_BY_COMPLETION = 1;
    public static final String PLAY_TYPE = "play_type";

    public MusicController(Callback callback) {
        mediaPlayer = new MediaPlayer();
        playQueue = PlayQueue.getSelf();
        this.callback = callback;
    }

    public void playSong(SongInfo songInfo, Bundle extras) {
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
                    MediaControllerHolder.getMediaController().getTransportControls().skipToNext();
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

            if (extras != null) {
                int extra = extras.getInt(PLAY_TYPE);
                if (extra == PLAY_BY_USER) {
                    playQueue.setCurrSong(songInfo);
                }
            }

            state = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playSong() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.start();
        state = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }

    public void pauseSong() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.pause();
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
        if (mediaPlayer == null) { return; }
        mediaPlayer.seekTo(ms);
    }

    public int getCurrentPosition() {
        if (mediaPlayer == null) {
            return -1;
        }
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
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
