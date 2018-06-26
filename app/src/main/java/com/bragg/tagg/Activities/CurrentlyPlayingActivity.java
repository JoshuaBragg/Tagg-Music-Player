package com.bragg.tagg.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bragg.tagg.MediaController;
import com.bragg.tagg.R;
import com.bragg.tagg.SeekBarController;
import com.bragg.tagg.SongInfo;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class CurrentlyPlayingActivity extends AppCompatActivity implements Observer {

    private MediaController mediaController;
    private SeekBarController seekBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_playing_view);

        mediaController = MediaController.getSelf();
        mediaController.attach(this);

        HashMap state = mediaController.getState();

        if ((boolean)state.get("playing")) {
            findViewById(R.id.pausePlayBtn).setBackgroundResource(R.drawable.baseline_pause_white_18);
        }
        ((TextView)findViewById(R.id.songNameTextView)).setText((String)state.get("songName"));
        ((TextView)findViewById(R.id.artistNameTextView)).setText((String)state.get("artistName"));

        seekBarController = new SeekBarController(this);
        seekBarController.startThread();

        setClickListeners();
    }

    public void setClickListeners() {
        Button pausePlayBtn = findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaController.songLoaded()) { return; }

                if (mediaController.isPlaying()) {
                    mediaController.pauseSong();
                } else {
                    mediaController.playSong();
                }
            }
        });

        Button skipNextBtn = findViewById(R.id.skipNextBtn);

        skipNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaController.songLoaded()) { return; }

                mediaController.nextSong();
            }
        });

        Button skipPrevBtn = findViewById(R.id.skipPrevBtn);

        skipPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaController.songLoaded()) { return; }

                mediaController.prevSong();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        mediaController.detach(this);
        seekBarController.killThread();
        overridePendingTransition(R.anim.empty_transition, R.anim.slide_out_down);
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
