package gg.joshbra.tagg;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

public class SeekBarController {
    private SeekBar seekBar;
    private MusicController musicController;
    private SeekBarThread seekBarThread;
    private boolean running;

    public SeekBarController(AppCompatActivity m) {
        seekBarThread = new SeekBarThread();
        seekBar = m.findViewById(R.id.seekBar);
        running = false;
//        musicController = MusicController.getSelf();
//        if (musicController.songLoaded()) {
//            seekBar.setMax(musicController.getDuration());
//            seekBar.setProgress(musicController.getCurrentPosition());
//        }
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
//                    if (b && musicController.songLoaded()) {
//                        musicController.seekTo(seekBar.getProgress());
//                    }
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
//                    if (musicController.songLoaded()) {
//                        seekBar.setMax(musicController.getDuration());
//                        if (Build.VERSION.SDK_INT >= 24) {
//                            seekBar.setProgress(musicController.getCurrentPosition(), true);
//                        } else {
//                            seekBar.setProgress(musicController.getCurrentPosition());
//                        }
//                    }
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
