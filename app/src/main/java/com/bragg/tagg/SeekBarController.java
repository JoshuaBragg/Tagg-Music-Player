package com.bragg.tagg;
import android.os.Build;
import android.widget.SeekBar;

public class SeekBarController {
    private SeekBar seekBar;
    private MediaController mediaController;
    private SeekBarThread seekBarThread;

    public SeekBarController(MediaController mediaController, MainActivity m) {
        this.mediaController = mediaController;
        seekBarThread = new SeekBarThread();
        seekBar = m.findViewById(R.id.seekBar);
    }

    public void startThread() {
        if (!seekBarThread.isAlive())
            seekBarThread.start();
    }

    public void setProgress(int amount) {
        seekBar.setProgress(amount);
    }

    public void setMax(int amount) {
        seekBar.setMax(amount);
    }

    public class SeekBarThread extends Thread{
        @Override
        public void run() {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    // Only update the mediaPlayer position if the user moved seekBar
                    if (b) {
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

            while (true) {
                try {
                    Thread.sleep(1000);
                    if (mediaController.songLoaded()) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            seekBar.setProgress(mediaController.getCurrentPosition(), true);
                        } else {
                            seekBar.setProgress(mediaController.getCurrentPosition());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
