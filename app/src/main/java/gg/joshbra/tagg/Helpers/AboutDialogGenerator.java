package gg.joshbra.tagg.Helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import gg.joshbra.tagg.R;

public class AboutDialogGenerator {
    private AboutDialogGenerator() {}

    public static void createDialog(final Context context) {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                try {
                    intent.setData(Uri.parse("http://www.joshbra.gg"));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(context, "Webpage could not be opened", Toast.LENGTH_SHORT).show();
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
