package gg.joshbra.tagg;

import android.os.Build;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

public class SeekBarController {
    private SeekBar seekBar;
    private MediaControllerCompat mediaController;
    private SeekBarThread seekBarThread;
    private boolean running;

    public SeekBarController(AppCompatActivity m) {
        seekBarThread = new SeekBarThread();
        seekBar = m.findViewById(R.id.seekBar);
        running = false;
        mediaController = MediaControllerHolder.getMediaController();
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
