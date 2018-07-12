package gg.joshbra.tagg.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import gg.joshbra.tagg.MusicController;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SeekBarController;
import gg.joshbra.tagg.SongInfo;

import java.util.Observable;
import java.util.Observer;

public class CurrentlyPlayingActivity extends AppCompatActivity implements Observer {

    private MusicController musicController;
    private SeekBarController seekBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_playing_view);

//        musicController = MusicController.getSelf();
//        musicController.attach(this);
//
//        HashMap state = musicController.getState();

//        if ((boolean)state.get("playing")) {
//            ((ImageButton)findViewById(R.id.pausePlayBtn)).setImageResource(R.drawable.ic_pause_white_24dp);
//        }
//        ((TextView)findViewById(R.id.songNameTextView)).setText((String)state.get("songName"));
//        ((TextView)findViewById(R.id.artistNameTextView)).setText((String)state.get("artistName"));

//        seekBarController = new SeekBarController(this);
//        seekBarController.startThread();

        setClickListeners();
    }

    public void setClickListeners() {
        ImageButton pausePlayBtn = findViewById(R.id.pausePlayBtn);

//        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!musicController.songLoaded()) { return; }
//
//                if (musicController.isPlaying()) {
//                    musicController.pauseSong();
//                } else {
//                    musicController.playSong();
//                }
//            }
//        });
//
//        ImageButton skipNextBtn = findViewById(R.id.skipNextBtn);
//
//        skipNextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!musicController.songLoaded()) { return; }
//
//                musicController.nextSong();
//            }
//        });
//
//        ImageButton skipPrevBtn = findViewById(R.id.skipPrevBtn);
//
//        skipPrevBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!musicController.songLoaded()) { return; }
//
//                musicController.prevSong();
//            }
//        });
    }

    @Override
    public void finish() {
        super.finish();
        musicController.detach(this);
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
            ImageButton pausePlayBtn = findViewById(R.id.pausePlayBtn);
            if (playing) {
                pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }
}
