package gg.joshbra.tagg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import gg.joshbra.tagg.Activities.MainActivity;
import gg.joshbra.tagg.Activities.SplashActivity;

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service will not get killed during playback.
 *
 * Modified minorly from example found at:
 *      https://github.com/googlecodelabs/android-music-player
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "media_playback_channel";

    private static final String ACTION_PAUSE = "action_pause";
    private static final String ACTION_PLAY = "action_play";
    private static final String ACTION_NEXT = "action_next";
    private static final String ACTION_PREV = "action_prev";

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

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Media playback", NotificationManager.IMPORTANCE_LOW);
        // Configure the notification channel.
        channel.setDescription("Media playback controls");
        channel.setShowBadge(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
    }

    public void update(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(musicService, CHANNEL_ID);
        MediaDescriptionCompat description = metadata.getDescription();

        notificationBuilder
                .setStyle(new MediaStyle().setMediaSession(token).setShowActionsInCompactView(0, 1, 2))
                .setColor(ContextCompat.getColor(musicService, R.color.colorOffWhite))
                .setSmallIcon(R.drawable.tagg_icon_very_small)
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
            if (Build.VERSION.SDK_INT >= 26) {
                musicService.startForegroundService(new Intent(musicService.getApplicationContext(), MusicService.class));
            } else {
                musicService.startService(new Intent(musicService.getApplicationContext(), MusicService.class));
            }
            musicService.startForeground(NOTIFICATION_ID, notification);
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
        Intent openUI = new Intent(musicService, SplashActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                musicService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}