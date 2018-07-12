package gg.joshbra.tagg;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {

    private MediaSessionCompat session;
    private PlaybackStateCompat.Builder stateBuilder;
    private MusicController musicController;
    private PlayQueue playQueue;

    final MediaSessionCompat.Callback callback =
            new MediaSessionCompat.Callback() {
                @Override
                public void onPlayFromMediaId(String mediaId, Bundle extras) {
                    session.setActive(true);
                    SongInfo song = playQueue.getSongByID(mediaId);
                    session.setMetadata(song.getMediaMetadataCompat());
                    musicController.playSong(song);
                }

                @Override
                public void onPlay() {
                    if (playQueue.getCurrentMediaId() != null) {
                        onPlayFromMediaId(playQueue.getCurrentMediaId(), null);
                    }
                }

                @Override
                public void onPause() {
                    musicController.pauseSong();
                }

                @Override
                public void onStop() {
                    stopSelf();
                }

                @Override
                public void onSkipToNext() {
                    onPlayFromMediaId(playQueue.getNextSong().getMediaID(), null);
                }

                @Override
                public void onSkipToPrevious() {
                    onPlayFromMediaId(playQueue.getPrevSong().getMediaID(), null);
                }
            };

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

        final MediaNotificationManager mediaNotificationManager = new MediaNotificationManager(this);

        musicController = new MusicController(this, new MusicController.Callback() {
                            @Override
                            public void onPlaybackStatusChanged(PlaybackStateCompat state) {
                                session.setPlaybackState(state);
                                mediaNotificationManager.update(playQueue.getCurrSong().getMediaMetadataCompat(), state, getSessionToken());
                            }
                        });
    }

    @Override
    public void onDestroy() {
        musicController.stopSong();
        session.release();
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(PlayQueue.getRoot(), null);
    }

    @Override
    public void onLoadChildren(final String parentMediaId, final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(PlayQueue.getMediaItems());
    }
}
