package com.bragg.tagg;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class CurrentlyPlayingView extends AppCompatActivity {

    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_playing_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mediaController = new MediaController();
        ArrayList<SongInfo> songs = (ArrayList<SongInfo>) getIntent().getSerializableExtra("songs");
        SerMediaPlayer serMediaPlayer = (SerMediaPlayer) getIntent().getSerializableExtra("mediaPlayer");
        SongInfo currSong = (SongInfo) getIntent().getSerializableExtra("currSong");
        mediaController.dupMediaController(this, songs, serMediaPlayer, currSong);

        final Button pausePlayBtn = findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaController.songLoaded()) {
                    return;
                }
                if (mediaController.isPlaying()) {
                    mediaController.pauseSong();
                    pausePlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_white_18);
                } else {
                    mediaController.playSong();
                    pausePlayBtn.setBackgroundResource(R.drawable.baseline_pause_white_18);
                }
            }
        });
    }
}
