package gg.joshbra.tagg.Helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

import gg.joshbra.tagg.R;

/**
 * Helper to create about dialog from any context
 */
public class AboutDialogGenerator {
    private AboutDialogGenerator() {}

    /**
     * Create and display about dialog
     * @param context The context to display the dialog
     */
    public static void createDialog(final Context context) {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                try {
                    intent.setData(Uri.parse("http://www.joshbra.gg"));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException exception) {
                    Toast toast = Toast.makeText(context, "Webpage could not be opened", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) context.getResources().getDimension(R.dimen.toast_offset));
                    toast.show();
                }
            }
        };

        new AlertDialog.Builder(context, R.style.Dialog)
                .setTitle("About Tagg Music Player")
                .setMessage("Created and Developed by Joshua Bragg")
                .setPositiveButton("More from Josh", dialogListener)
                .create()
                .show();
    }
}
