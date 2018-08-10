package gg.joshbra.tagg.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import gg.joshbra.tagg.Helpers.AlbumArtRetriever;
import gg.joshbra.tagg.Helpers.FlagManager;

/**
 * Simple activity used to launch the app and provide some visual for UX purposes while the app loads
 * This load time can depend on speed of device and the amount of songs so this Activity prevents a
 * user unfriendly white screen from appearing while app launches
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes a few things before anything should occur in the app
        FlagManager.getSelf().fetchFlags(this);
        FlagManager.getSelf().fetchSongPreferences(this);
        AlbumArtRetriever.setContentResolver(getContentResolver());

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}