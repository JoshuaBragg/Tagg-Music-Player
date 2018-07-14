package gg.joshbra.tagg.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import gg.joshbra.tagg.Activities.CurrentlyPlayingActivity;
import gg.joshbra.tagg.Activities.MainActivity;
import gg.joshbra.tagg.CurrentPlaybackNotifier;
import gg.joshbra.tagg.MediaControllerHolder;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;

public class NowPlayingBarFragment extends Fragment implements Observer {
    private MediaControllerCompat mediaController;
    private PlaybackStateCompat state;

    public NowPlayingBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_now_playing_bar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mediaController = MediaControllerHolder.getMediaController();
        CurrentPlaybackNotifier.getSelf().attach(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof PlaybackStateCompat) {
            PlaybackStateCompat state = (PlaybackStateCompat)o;

            this.state = state;

            ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);

            if (state.getState() == PlaybackStateCompat.STATE_PAUSED || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
                pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
            }
        } else if (o instanceof MediaMetadataCompat) {
            MediaMetadataCompat metadata = (MediaMetadataCompat)o;

            TextView songName = getView().findViewById(R.id.songNameBarTextView);
            TextView artistName = getView().findViewById(R.id.artistNameBarTextView);

            songName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        }
    }

    public void initNowPlayingBar() {
        mediaController = MediaControllerHolder.getMediaController();

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == null || state.getState() == PlaybackStateCompat.STATE_NONE) { return; }
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
            }
        });

        RelativeLayout botBar = getView().findViewById(R.id.botBar);

        botBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == null || state.getState() == PlaybackStateCompat.STATE_NONE) { return; }

                Intent intent = new Intent(getContext(), CurrentlyPlayingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.empty_transition);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CurrentPlaybackNotifier.getSelf().detach(this);
    }
}
