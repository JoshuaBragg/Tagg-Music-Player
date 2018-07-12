package gg.joshbra.tagg.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gg.joshbra.tagg.Activities.CurrentlyPlayingActivity;
import gg.joshbra.tagg.MusicController;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;

import java.util.Observable;
import java.util.Observer;

public class NowPlayingBarFragment extends Fragment implements Observer {
    private MusicController musicController;

    public NowPlayingBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        musicController = MusicController.getSelf();
//        musicController.attach(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_now_playing_bar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setOnClickListeners();

//        if (musicController.getCurrSong() == null) { return; }
//
//        TextView songName = getView().findViewById(R.id.songNameBarTextView);
//        TextView artistName = getView().findViewById(R.id.artistNameBarTextView);
//
//        songName.setText( musicController.getCurrSong().getSongName() );
//        artistName.setText( musicController.getCurrSong().getArtistName() );
//
//        update(new Observable(), musicController.isPlaying());
    }

    @Override
    public void update(Observable observable, Object o) {
        if (getView() == null) { return; }

        if (o instanceof SongInfo) {
            TextView songName = getView().findViewById(R.id.songNameBarTextView);
            TextView artistName = getView().findViewById(R.id.artistNameBarTextView);

            songName.setText( ((SongInfo)o).getSongName() );
            artistName.setText( ((SongInfo)o).getArtistName() );
        }
        else if (o instanceof Boolean) {
            boolean playing = (Boolean)o;
            ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);
            if (playing) {
                pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }

    private void setOnClickListeners() {
        ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicController == null) {
                    return;
                }
                if (musicController.isPlaying()) {
                    musicController.pauseSong();
                } else {
                    musicController.playSong();
                }
            }
        });

        RelativeLayout botBar = getView().findViewById(R.id.botBar);

        botBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (!musicController.songLoaded()) { return; }
//                Intent intent = new Intent(getContext(), CurrentlyPlayingActivity.class);
//                startActivity(intent);
//                getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.empty_transition);
            }
        });
    }
}
