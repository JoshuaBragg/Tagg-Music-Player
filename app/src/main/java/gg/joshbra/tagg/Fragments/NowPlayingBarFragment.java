package gg.joshbra.tagg.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gg.joshbra.tagg.Activities.CurrentlyPlayingActivity;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;

public class NowPlayingBarFragment extends Fragment {
    private MediaControllerCompat mediaController;

    public NowPlayingBarFragment() {
        // Required empty public constructor
    }

    public static NowPlayingBarFragment newInstance() {
        return new NowPlayingBarFragment();
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
    }

    public void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null || state.getState() == PlaybackStateCompat.STATE_PAUSED || state.getState() == PlaybackStateCompat.STATE_STOPPED) {
            ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);
            pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        } else {
            ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);
            pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
        }
        getView().findViewById(R.id.nowPlayingBarFrag).setVisibility(state == null ? View.GONE : View.VISIBLE);
    }

    public void updateMetadata(MediaMetadataCompat metadata) {
        TextView songName = getView().findViewById(R.id.songNameBarTextView);
        TextView artistName = getView().findViewById(R.id.artistNameBarTextView);

        if (metadata == null) {
            songName.setText("");
            artistName.setText("");
            return;
        }

        songName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        artistName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
    }

    public void initNowPlayingBar() {
        mediaController = MediaControllerCompat.getMediaController(getActivity());

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        ImageButton pausePlayBtn = getView().findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.ACTION_PLAY) {
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
                Intent intent = new Intent(getContext(), CurrentlyPlayingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.empty_transition);
            }
        });
    }
}
