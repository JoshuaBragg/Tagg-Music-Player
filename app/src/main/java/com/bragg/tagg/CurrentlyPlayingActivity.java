package com.bragg.tagg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

public class CurrentlyPlayingActivity extends AppCompatActivity implements Obs {

    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_playing_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mediaController = MediaController.getSelf();
        mediaController.attach(this);

        HashMap state = mediaController.getState();

        if ((boolean)state.get("playing")) {
            findViewById(R.id.pausePlayBtn).setBackgroundResource(R.drawable.baseline_pause_white_18);
        }
        ((TextView)findViewById(R.id.songNameTextView)).setText((String)state.get("songName"));
        ((TextView)findViewById(R.id.artistNameTextView)).setText((String)state.get("artistName"));


        final Button pausePlayBtn = findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaController.songLoaded()) {
                    return;
                }
                if (mediaController.isPlaying()) {
                    mediaController.pauseSong();
                } else {
                    mediaController.playSong();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        mediaController.detach(this);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof SongInfo) {
            TextView songName = findViewById(R.id.songNameTextView);
            TextView artistName = findViewById(R.id.artistNameTextView);

            songName.setText( ((SongInfo)data).getSongName() );
            artistName.setText( ((SongInfo)data).getArtistName() );
        }
        else if (data instanceof Boolean) {
            boolean playing = (Boolean)data;
            Button pausePlayBtn = findViewById(R.id.pausePlayBtn);
            if (playing) {
                pausePlayBtn.setBackgroundResource(R.drawable.baseline_pause_white_18);
            } else {
                pausePlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_white_18);
            }
        }
    }
}
