package gg.joshbra.tagg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.Helpers.ThemedNumberPicker;

/**
 * Creates interface for and manages the Sleep Timer
 */
public class SleepTimerController {
    private SleepTimerThread thread;
    private boolean running;

    ////////////////////////////// Singleton ///////////////////////////////

    private static final SleepTimerController self = new SleepTimerController();

    private SleepTimerController() {
        thread = null;
        running = false;
    }

    public static SleepTimerController getSelf() {
        return self;
    }

    ////////////////////////////// Singleton ///////////////////////////////

    /**
     * Will create the appropriate interface for sleep timer
     * @param context The context from which this method was called
     */
    public void create(Context context) {
        if (thread != null && thread.isAlive()) {
            createSnackBar(context);
        } else {
            createSleepTimerDialog(context);
        }
    }

    /**
     * Creates the dialog box allowing user to set the sleep timer
     * @param context The context to display the sleep timer
     */
    private void createSleepTimerDialog(final Context context) {
        final ThemedNumberPicker numberPicker = new ThemedNumberPicker(context);
        numberPicker.setMaxValue(120);
        numberPicker.setMinValue(1);
        numberPicker.setValue(5);
        numberPicker.setDisplayedValues(getMinuteValues());

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startTimer(context, numberPicker.getValue());
            }
        };

        new AlertDialog.Builder(context, R.style.Dialog)
                .setTitle("Sleep Timer")
                .setMessage("Pause playback after:")
                .setView(numberPicker)
                .setPositiveButton("Start", dialogClickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * Creates the snackbar letting the user know how much time remains
     * @param context The context to display the snackbar
     */
    private void createSnackBar(Context context) {
        int[] timeRemaining = thread.getTimeRemaining();
        Snackbar snackbar = Snackbar.make(((AppCompatActivity)context).findViewById(R.id.drawer),
                timeRemaining[0] == 0 ? timeRemaining[1] + " seconds remaining" :
                        timeRemaining[0] + " minutes and " + timeRemaining[1] + " seconds remaining", Snackbar.LENGTH_LONG);

        snackbar.setAction("DISABLE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
            }
        });

        snackbar.show();
    }

    /**
     * Notifies user the timer has started and begins timer
     * @param context The context to notify
     * @param amount The amount of minutes to set timer to
     */
    private void startTimer(Context context, int amount) {
        Toast toast = Toast.makeText(context, "Sleep Timer set for " + amount + " minutes", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) context.getResources().getDimension(R.dimen.toast_offset));
        toast.show();

        beginTimer(amount);
    }

    /**
     * Begins the thread responsible for handling the timer
     * @param amount The amount of minutes to set the timer to
     */
    private void beginTimer(int amount) {
        killThread();
        thread = new SleepTimerThread(amount * 60);
        running = true;
        try {
            if (!thread.isAlive()) {
                running = true;
                thread.start();
            }
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels the timer by killing thread
     */
    private void cancelTimer() {
        killThread();
    }

    /**
     * A thread that keeps track of the time that has passed and pauses playback upon the timer ending
     */
    private class SleepTimerThread extends Thread {
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
            while (running && runTime <= amount) {
                runTime++;
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
            killThread();
        }

        /**
         * Gets the time remaining on the timer
         * @return Returns an int[] where the first element is the number of minutes remaining and the second element is the number of seconds remaining
         */
        int[] getTimeRemaining() {
            return new int[]{((amount - runTime) - (amount - runTime) % 60) / 60, (amount - runTime) % 60};
        }
    }

    /**
     * Kills the thread
     */
    private void killThread() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        running = false;
    }

    /**
     * Returns the values to be shown in the number picker
     * @return Array of strings where each element is as follows : "'index of element + 1' ' minutes'"
     */
    private static String[] getMinuteValues() {
        String[] out = new String[120];
        for (int i = 1; i <= out.length; i++) {
            out[i - 1] = i + " minutes";
        }
        return out;
    }
}
