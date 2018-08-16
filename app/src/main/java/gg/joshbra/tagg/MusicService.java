package gg.joshbra.tagg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;

/**
 * Service that creates interface between MusicController and rest of app
 * Allows music to keep running even if app is closed while service runs in background
 *
 * Implements interface to allow for compatibility with android wear/car
 */
public class MusicService extends MediaBrowserServiceCompat {

    private MediaSessionCompat session;
    private PlaybackStateCompat.Builder stateBuilder;
    private MusicController musicController;
    private PlayQueue playQueue;
    private MediaNotificationManager mediaNotificationManager;

    public static boolean running;
    private static final int PREV_THRESHOLD = 6000;

    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver noisyAudioStreamReceiver = new BecomingNoisyReceiver();

    private AudioManager audioManager;
    private AudioAttributes audioAttributes;
    private AudioFocusRequest audioFocusRequest;

    /**
     * Media Session's callback
     * Called when getTransportControls() is used on MediaController
     */
    final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        /**
         * Plays the given song
         * @param mediaId The ID of the song to be played
         * @param extras Optional extra passed into musicController.playSong()
         */
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            if (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_FAILED) { return; }

            session.setActive(true);
            registerReceiver(noisyAudioStreamReceiver, intentFilter);
            SongInfo song = playQueue.getSongByID(mediaId);
            session.setMetadata(song.getMediaMetadataCompat());
            musicController.playSong(song, extras);
            updateFlags();
        }

        /**
         * Resumes playback
         */
        @Override
        public void onPlay() {
            if (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_FAILED) { return; }

            if (playQueue.getCurrentMediaId() != null) {
                musicController.playSong();
            }
        }

        /**
         * Pauses playback
         */
        @Override
        public void onPause() {
            musicController.pauseSong();
        }

        /**
         * Stops playback
         */
        @Override
        public void onStop() {
            musicController.stopSong();
            stopSelf();
            try {
                unregisterReceiver(noisyAudioStreamReceiver);
            } catch (IllegalArgumentException e) {}
        }

        /**
         * Skips to next song
         * If there is no next song then playback is stopped
         */
        @Override
        public void onSkipToNext() {
            try {
                Bundle extra = new Bundle();
                extra.putInt(MusicController.PLAY_TYPE, MusicController.PLAY_BY_COMPLETION);
                onPlayFromMediaId(playQueue.getNextSong().getMediaID().toString(), extra);
            } catch (NullPointerException e) {
                onStop();
            }
        }

        /**
         * If the song has been playing for more than PREV_THRESHOLD then song is restarted
         * Else the previous song is played
         * If there is no previous song then playback is stopped
         */
        @Override
        public void onSkipToPrevious() {
            if (musicController.getCurrentPosition() > PREV_THRESHOLD) {
                Bundle extra = new Bundle();
                extra.putInt(MusicController.PLAY_TYPE, MusicController.PLAY_BY_COMPLETION);
                onPlayFromMediaId(playQueue.getCurrentMediaId().toString(), extra);
            } else {
                try {
                    Bundle extra = new Bundle();
                    extra.putInt(MusicController.PLAY_TYPE, MusicController.PLAY_BY_COMPLETION);
                    onPlayFromMediaId(playQueue.getPrevSong().getMediaID().toString(), extra);
                } catch (NullPointerException e) {
                    onStop();
                }
            }
        }

        /**
         * Seeks to position in playback
         * @param pos The time to seek to in ms
         */
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            musicController.seekTo((int)pos);
            musicController.updatePlaybackState();
        }

        /**
         * Updates playback state when the repeatMode is changed
         * @param repeatMode The new repeat mode
         */
        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            musicController.updatePlaybackState();
        }

        /**
         * Updates playback state when the shuffleMode is changed
         * @param shuffleMode The new shuffleMode
         */
        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            musicController.updatePlaybackState();
        }
    };

    /**
     * Sets the song preferences
     */
    private void updateFlags() {
        FlagManager.getSelf().setSongPreferences(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        playQueue = PlayQueue.getSelf();

        // Start a new MediaSession
        session = new MediaSessionCompat(this, "MusicService");
        session.setCallback(callback);
        session.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(session.getSessionToken());

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        session.setPlaybackState(stateBuilder.build());

        mediaNotificationManager = new MediaNotificationManager(this);

        // Create Audio Manager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Create MusicController
        musicController = new MusicController(audioManager, new MusicController.Callback() {
                            @Override
                            public void onPlaybackStatusChanged(PlaybackStateCompat state) {
                                session.setPlaybackState(state);
                                if (playQueue.getCurrSong() == null) { return; }
                                mediaNotificationManager.update(playQueue.getCurrSong().getMediaMetadataCompat(), state, getSessionToken());
                            }
                        });

        // Create Audio Attributes
        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        // Create Audio Focus request
        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(musicController)
                .build();

        running = true;
    }

    @Override
    public void onDestroy() {
        musicController.stopSong();
        session.release();
        running = false;
        try {
            unregisterReceiver(mediaNotificationManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(PlayQueue.getRoot(), null);
    }

    @Override
    public void onLoadChildren(final String parentMediaId, final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(PlayQueue.getSelf().getMediaItems());
    }

    /**
     * A receiver that pauses playback on ACTION_AUDIO_BECOMING_NOISY
     * i.e. Playback paused if headphones unplugged, bluetooth disabled etc.
     */
    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                MediaControllerHolder.getMediaController().getTransportControls().pause();
            }
        }
    }
}
