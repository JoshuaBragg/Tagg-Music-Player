package gg.joshbra.tagg;

import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.SeekBar;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;

/**
 * The controller and thread for the seekbar
 */
public class SeekBarController {
    private SeekBar seekBar;
    private MediaControllerCompat mediaController;
    private SeekBarThread seekBarThread;
    private boolean running;
    private Handler handler;

    public SeekBarController(ConstraintLayout r, CurrentlyPlayingSheet currentlyPlayingSheet) {
        seekBarThread = new SeekBarThread();
        seekBar = r.findViewById(R.id.seekBar);
        running = false;
        mediaController = MediaControllerHolder.getMediaController();
        handler = currentlyPlayingSheet.getHandler();
    }

    /**
     * Starts the thread
     */
    public void startThread() {
        try {
            if (!seekBarThread.isAlive())
                running = true;
                seekBarThread.start();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        }
    }

    // TODO: possibly make seekbar poll for updates more frequently for smoother transition

    /**
     * The thread that updates the seekbar every second
     */
    public class SeekBarThread extends Thread{
        @Override
        public void run() {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                /**
                 * When the progress is changed update playback and the time displayed
                 * @param seekBar The seekbar that was changed
                 * @param i The progress
                 * @param b True iff the user changed the progress of the bar
                 */
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    // Only update the mediaPlayer position if the user moved seekBar and song is loaded
                    if (b && mediaController.getPlaybackState() != null && mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                        mediaController.getTransportControls().seekTo((long)i);
                        // Update the time shown in CurrentlyPlayingSheet
                        Message msg = new Message();
                        msg.arg1 = ((Long)mediaController.getPlaybackState().getPosition()).intValue();
                        handler.sendMessage(msg);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // User started touching seekBar
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // User stopped touching seekBar
                }
            });

            // Poll every second for changes and update seekbar and time shown accordingly
            while (running) {
                try {
                    if (mediaController.getPlaybackState() != null && mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                        seekBar.setMax(Math.round(PlayQueue.getSelf().getCurrSong().getDuration()));
                        seekBar.setProgress(Math.round(mediaController.getPlaybackState().getPosition()), true);
                        Message msg = new Message();
                        msg.arg1 = ((Long)mediaController.getPlaybackState().getPosition()).intValue();
                        handler.sendMessage(msg);
                    }
                    try {
                        Thread.interrupted();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.interrupted();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    killThread();  // Commit suicide
                }
            }
        }
    }

    /**
     * Kills the thread
     */
    public void killThread() {
        running = false;
        seekBarThread.interrupt();
        running = false;
    }
}
