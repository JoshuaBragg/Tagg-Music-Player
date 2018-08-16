package gg.joshbra.tagg;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;

/**
 * Responsible for controlling the mediaPlayer and all music playback
 */
public class MusicController implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private PlayQueue playQueue;
    private int state;

    private final Callback callback;

    private boolean awaitingFocus;
    private boolean headphonesWerePluggedIn;
    private AudioManager audioManager;

    // The two ways songs can be played, needed to prevent obscure bug with currSong in playQueue
    // and the way ArrayList.contains works
    public static final int PLAY_BY_USER = 0, PLAY_BY_COMPLETION = 1;
    public static final String PLAY_TYPE = "play_type";

    public MusicController(AudioManager audioManager, Callback callback) {
        mediaPlayer = new MediaPlayer();
        playQueue = PlayQueue.getSelf();
        this.callback = callback;
        this.audioManager = audioManager;
        awaitingFocus = false;
        headphonesWerePluggedIn = false;
    }

    /**
     * Play the song provided
     * @param songInfo The song to be played
     * @param extras May contain data about how the song has begun playing (PLAY_BY_USER or PLAY_BY_COMPLETION)
     */
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
                    awaitingFocus = false;
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

    /**
     * Resumes the current song
     */
    public void playSong() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.start();
        awaitingFocus = false;
        state = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }

    /**
     * Pauses the current song
     */
    public void pauseSong() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.pause();
        state = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    /**
     * Stops playback
     */
    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        state = PlaybackStateCompat.STATE_STOPPED;
        updatePlaybackState();
    }

    /**
     * Seeks to position
     * @param ms The position in ms to seek to
     */
    public void seekTo(int ms) {
        if (mediaPlayer == null) { return; }
        mediaPlayer.seekTo(ms);
    }

    /**
     * The mediaPlayers current position
     * @return Returns the currentPosition of playback or -1 if mediaPlayer is null
     */
    public int getCurrentPosition() {
        if (mediaPlayer == null) {
            return -1;
        }
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * The duration of the current song
     * @return Returns the duration of the current song or -1 if mediaPlayer is null
     */
    public int getDuration() {
        if (mediaPlayer == null) {
            return -1;
        }
        return mediaPlayer.getDuration();
    }

    /**
     * Gets if the mediaPlayer is currently playing
     * @return True iff mediaPlayer.isPlaying()
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Handles audio focus changes
     * i.e. If another app requests audio focus it will pause playback or lower playback volume during notification noises
     * @param focusChange The focus type we have
     */
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

        if (canDuck) {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                state = PlaybackStateCompat.STATE_PLAYING;
                updatePlaybackState();
                mediaPlayer.setVolume(0.2f, 0.2f);
            }
        } else if (gotFullFocus) {
            if (mediaPlayer != null && awaitingFocus) {
                if (headphonesWerePluggedIn && !audioManager.isWiredHeadsetOn()) {
                    return;
                }
                mediaPlayer.start();
                awaitingFocus = false;
                state = PlaybackStateCompat.STATE_PLAYING;
                updatePlaybackState();
                mediaPlayer.setVolume(1.0f, 1.0f);
            }
        } else if (state == PlaybackStateCompat.STATE_PLAYING) {
            mediaPlayer.pause();
            awaitingFocus = true;
            headphonesWerePluggedIn = audioManager.isWiredHeadsetOn();
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

    /**
     * Similar to getCurrentPosition
     * @return Returns currentPosition or 0 if mediaPlayer is null
     */
    public int getCurrentStreamPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    /**
     * Called when the playback state has changed
     * Notifies the callback that the state has changed
     */
    public void updatePlaybackState() {
        if (callback == null) {
            return;
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());

        stateBuilder.setState(state, getCurrentStreamPosition(), 1.0f, SystemClock.elapsedRealtime());
        callback.onPlaybackStatusChanged(stateBuilder.build());
    }
}
