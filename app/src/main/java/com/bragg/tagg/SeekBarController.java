package com.bragg.tagg;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

import java.util.Observable;

public class SeekBarController {
    private SeekBar seekBar;
    private MediaController mediaController;
    private SeekBarThread seekBarThread;
    private boolean running;

    public SeekBarController(AppCompatActivity m) {
        seekBarThread = new SeekBarThread();
        seekBar = m.findViewById(R.id.seekBar);
        running = false;
        mediaController = MediaController.getSelf();
        if (mediaController.songLoaded()) {
            seekBar.setMax(mediaController.getDuration());
            seekBar.setProgress(mediaController.getCurrentPosition());
        }
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
                    if (b && mediaController.songLoaded()) {
                        mediaController.seekTo(seekBar.getProgress());
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
                    Thread.sleep(1000);
                    if (mediaController.songLoaded()) {
                        seekBar.setMax(mediaController.getDuration());
                        if (Build.VERSION.SDK_INT >= 24) {
                            seekBar.setProgress(mediaController.getCurrentPosition(), true);
                        } else {
                            seekBar.setProgress(mediaController.getCurrentPosition());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
