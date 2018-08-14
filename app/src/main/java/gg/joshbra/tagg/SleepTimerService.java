package gg.joshbra.tagg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import gg.joshbra.tagg.Activities.SplashActivity;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;

/**
 * A service that keeps track of progress on sleep timer
 */
public class SleepTimerService extends Service {
    private boolean running;
    private int serviceID;
    private NotificationCompat.Builder notificationBuilder;

    private static final int NOTIFICATION_ID = 413;
    private static final int REQUEST_CODE = 101;
    private static final String CHANNEL_ID = "sleep_timer_channel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceID = startId;

        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,REQUEST_CODE, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        SleepTimerThread thread = new SleepTimerThread(intent.getIntExtra(SleepTimerController.SLEEP_TIMER_AMOUNT, -1));

        int[] timeRemaining = thread.getTimeRemaining();

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sleep Timer Enabled")
                .setContentText("Playback will pause in " + (timeRemaining[0] == 0 ? timeRemaining[1] + " seconds" :
                        timeRemaining[0] + " minutes and " + timeRemaining[1] + " seconds"))
                .setSmallIcon(R.drawable.ic_timer_white_24dp)
                .setContentIntent(pendingIntent);

        thread.start();

        createChannel();

        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        return START_NOT_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Sleep Timer", NotificationManager.IMPORTANCE_LOW);
        // Configure the notification channel.
        channel.setDescription("Sleep Timer");
        channel.setShowBadge(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }

    /**
     * A thread that keeps track of the time that has passed and pauses playback upon the timer ending
     */
    private class SleepTimerThread extends Thread implements Runnable{
        private int runTime;
        private int amount;

        /**
         * Creates the thread
         * @param amount The amount of time in seconds to set the timer to
         */
        SleepTimerThread(int amount) {
            this.amount = amount;
            runTime = 0;
        }

        /**
         * Keep track of elapsed time and increment internal timer
         */
        @Override
        public void run() {
            while (running && runTime < amount) {
                runTime++;
                int[] timeRemaining = getTimeRemaining();
                notificationBuilder.setContentText("Playback will pause in " + (timeRemaining[0] == 0 ? timeRemaining[1] + " seconds" :
                        timeRemaining[0] + " minutes and " + timeRemaining[1] + " seconds"));
                ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
                try {
                    Thread.interrupted();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.interrupted();
                }
            }
            if (running) {
                MediaControllerHolder.getMediaController().getTransportControls().pause();
            }
            stopSelf(serviceID);
        }

        /**
         * Gets the time remaining on the timer
         * @return Returns an int[] where the first element is the number of minutes remaining and the second element is the number of seconds remaining
         */
        int[] getTimeRemaining() {
            return new int[]{((amount - runTime) - (amount - runTime) % 60) / 60, (amount - runTime) % 60};
        }
    }
}
