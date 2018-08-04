package gg.joshbra.tagg.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import gg.joshbra.tagg.Helpers.FlagManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FlagManager.getSelf().fetchFlags(this);
        FlagManager.getSelf().fetchSongPreferences(this);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}