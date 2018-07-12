package gg.joshbra.tagg;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import gg.joshbra.tagg.Activities.MainActivity;

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    private static final String ACTION_PAUSE = "com.example.android.musicplayercodelab.pause";
    private static final String ACTION_PLAY = "com.example.android.musicplayercodelab.play";
    private static final String ACTION_NEXT = "com.example.android.musicplayercodelab.next";
    private static final String ACTION_PREV = "com.example.android.musicplayercodelab.prev";

    private final MusicService musicService;

    private final NotificationManager notificationManager;

    private final NotificationCompat.Action playAction;
    private final NotificationCompat.Action pauseAction;
    private final NotificationCompat.Action nextAction;
    private final NotificationCompat.Action prevAction;

    private boolean started;

    public MediaNotificationManager(MusicService service) {
        musicService = service;

        String pkg = musicService.getPackageName();
        PendingIntent playIntent =
                PendingIntent.getBroadcast(
                        musicService,
                        REQUEST_CODE,
                        new Intent(ACTION_PLAY).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseIntent =
                PendingIntent.getBroadcast(
                        musicService,
                        REQUEST_CODE,
                        new Intent(ACTION_PAUSE).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent nextIntent =
                PendingIntent.getBroadcast(
                        musicService,
                        REQUEST_CODE,
                        new Intent(ACTION_NEXT).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent prevIntent =
                PendingIntent.getBroadcast(
                        musicService,
                        REQUEST_CODE,
                        new Intent(ACTION_PREV).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        playAction =
                new NotificationCompat.Action(
                        R.drawable.ic_play_arrow_white_24dp,
                        "Play",
                        playIntent);
        pauseAction =
                new NotificationCompat.Action(
                        R.drawable.ic_pause_white_24dp,
                        "Pause",
                        pauseIntent);
        nextAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_next_white_24dp,
                        "Next",
                        nextIntent);
        prevAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_previous_white_24dp,
                        "Previous",
                        prevIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);

        musicService.registerReceiver(this, filter);

        notificationManager =
                (NotificationManager) musicService.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        notificationManager.cancelAll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_PAUSE:
                musicService.callback.onPause();
                break;
            case ACTION_PLAY:
                musicService.callback.onPlay();
                break;
            case ACTION_NEXT:
                musicService.callback.onSkipToNext();
                break;
            case ACTION_PREV:
                musicService.callback.onSkipToPrevious();
                break;
        }
    }

    public void update(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token) {
        Log.i("d", "updating not");

        if (state == null || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
            musicService.stopForeground(true);
            try {
                musicService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore receiver not registered
            }
            musicService.stopSelf();
            return;
        }
        if (metadata == null) {
            return;
        }

        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(musicService);
        MediaDescriptionCompat description = metadata.getDescription();

        notificationBuilder
                .setStyle(
                        new NotificationCompat.MediaStyle()
                                .setMediaSession(token)
                                .setShowActionsInCompactView(0, 1, 2))
                .setColor(
                        musicService.getApplication().getResources().getColor(R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                //.setLargeIcon(MusicLibrary.getAlbumBitmap(musicService, description.getMediaId()))
                .setOngoing(isPlaying)
                .setWhen(isPlaying ? System.currentTimeMillis() - state.getPosition() : 0)
                .setShowWhen(isPlaying)
                .setUsesChronometer(isPlaying);

        // If skip to next action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(prevAction);
        }

        notificationBuilder.addAction(isPlaying ? pauseAction : playAction);

        // If skip to prev action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(nextAction);
        }

        Notification notification = notificationBuilder.build();

        if (isPlaying && !started) {
            musicService.startService(new Intent(musicService.getApplicationContext(), MusicService.class));
            Log.i("d", "woopty wop");
            musicService.startForeground(NOTIFICATION_ID, notification);
            Log.i("d", "may i have some looops borhter");
            started = true;
        } else {
            if (!isPlaying) {
                musicService.stopForeground(false);
                started = false;
            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(musicService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                musicService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}