package gg.joshbra.tagg;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;

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
        //observers = new ArrayList<>();
        handler = currentlyPlayingSheet.getHandler();
        // seekBar.setMax(Math.round(PlayQueue.getSelf().getCurrSong().getDuration()));
        // seekBar.setProgress(Math.round(mediaController.getPlaybackState().getPosition()));
    }

    public void startThread() {
        try {
            if (!seekBarThread.isAlive())
                running = true;
                seekBarThread.start();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        }
    }

    public class SeekBarThread extends Thread{
        @Override
        public void run() {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    // Only update the mediaPlayer position if the user moved seekBar and song is loaded
                    if (b && mediaController.getPlaybackState() != null && mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                        mediaController.getTransportControls().seekTo((long)i);
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

            while (running) {
                try {
                    if (mediaController.getPlaybackState() != null && mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_NONE) {
                        seekBar.setMax(Math.round(PlayQueue.getSelf().getCurrSong().getDuration()));
                        if (Build.VERSION.SDK_INT >= 24) {
                            seekBar.setProgress(Math.round(mediaController.getPlaybackState().getPosition()), true);
                        } else {
                            seekBar.setProgress(Math.round(mediaController.getPlaybackState().getPosition()));
                        }
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

    public void killThread() {
        running = false;
        seekBarThread.interrupt();
        running = false;
    }
}
