package gg.joshbra.tagg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import gg.joshbra.tagg.Helpers.ThemedNumberPicker;

/**
 * Creates interface for and manages the Sleep Timer
 */
public class SleepTimerController {
    private boolean running;

    public static final String SLEEP_TIMER_AMOUNT = "sleep_timer_amount";

    ////////////////////////////// Singleton ///////////////////////////////

    private static final SleepTimerController self = new SleepTimerController();

    private SleepTimerController() {
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
        if (running) {
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
     * Creates the snackbar letting the user disable sleep timer
     * @param context The context to display the snackbar
     */
    private void createSnackBar(final Context context) {
        Snackbar snackbar = Snackbar.make(((AppCompatActivity)context).findViewById(R.id.drawer), "Sleep Timer is active", Snackbar.LENGTH_LONG);

        snackbar.setAction("DISABLE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer(context);
                Toast toast = Toast.makeText(context, "Sleep Timer Disabled", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) context.getResources().getDimension(R.dimen.toast_offset));
                toast.show();
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

        beginTimer(context, amount);
    }

    /**
     * Begins the service responsible for handling the timer
     * @param amount The amount of minutes to set the timer to
     */
    private void beginTimer(Context context, int amount) {
        Intent serviceIntent = new Intent(context, SleepTimerService.class);
        serviceIntent.putExtra(SLEEP_TIMER_AMOUNT, amount * 60);
        running = true;
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    /**
     * Cancels the timer by killing service
     */
    private void cancelTimer(Context context) {
        Intent serviceIntent = new Intent(context, SleepTimerService.class);
        running = false;
        context.stopService(serviceIntent);
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
