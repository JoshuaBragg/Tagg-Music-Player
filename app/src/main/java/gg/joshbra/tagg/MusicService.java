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

public class MusicService extends MediaBrowserServiceCompat {

    private MediaSessionCompat session;
    private PlaybackStateCompat.Builder stateBuilder;
    private MusicController musicController;
    private PlayQueue playQueue;
    private MediaNotificationManager mediaNotificationManager;
    public static boolean running;

    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver noisyAudioStreamReceiver = new BecomingNoisyReceiver();

    private AudioManager audioManager;
    private AudioAttributes audioAttributes;
    private AudioFocusRequest audioFocusRequest;

    final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
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

        @Override
        public void onPlay() {
            if (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_FAILED) { return; }

            if (playQueue.getCurrentMediaId() != null) {
                musicController.playSong();
            }
        }

        @Override
        public void onPause() {
            musicController.pauseSong();
        }

        @Override
        public void onStop() {
            musicController.stopSong();
            stopSelf();
            try {
                unregisterReceiver(noisyAudioStreamReceiver);
            } catch (IllegalArgumentException e) {}
        }

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

        @Override
        public void onSkipToPrevious() {
            if (musicController.getCurrentPosition() > 6000) {
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

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            musicController.seekTo((int)pos);
            musicController.updatePlaybackState();
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            musicController.updatePlaybackState();
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            musicController.updatePlaybackState();
        }
    };

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

        musicController = new MusicController(new MusicController.Callback() {
                            @Override
                            public void onPlaybackStatusChanged(PlaybackStateCompat state) {
                                session.setPlaybackState(state);
                                if (playQueue.getCurrSong() == null) { return; }
                                mediaNotificationManager.update(playQueue.getCurrSong().getMediaMetadataCompat(), state, getSessionToken());
                            }
                        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

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

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                MediaControllerHolder.getMediaController().getTransportControls().pause();
            }
        }
    }
}
