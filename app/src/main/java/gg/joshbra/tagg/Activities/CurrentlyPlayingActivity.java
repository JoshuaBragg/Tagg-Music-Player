package gg.joshbra.tagg.Activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import gg.joshbra.tagg.CurrentPlaybackNotifier;
import gg.joshbra.tagg.MediaControllerHolder;
import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SeekBarController;

public class CurrentlyPlayingActivity extends AppCompatActivity implements Observer {

    private MediaControllerCompat mediaController;
    private SeekBarController seekBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_playing_view);

        mediaController = MediaControllerHolder.getMediaController();

        if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            ((ImageButton)findViewById(R.id.pausePlayBtn)).setImageResource(R.drawable.ic_pause_white_24dp);
        }
        ((TextView)findViewById(R.id.songNameTextView)).setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        ((TextView)findViewById(R.id.artistNameTextView)).setText(mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        ((TextView)findViewById(R.id.totalTimeTextView)).setText(parseTime(PlayQueue.getSelf().getCurrSong().getDuration().intValue()));

        if (PlayQueue.getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            ImageButton shuffleBtn = findViewById(R.id.shuffleBtn);
            shuffleBtn.setColorFilter(getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
        }

        seekBarController = new SeekBarController(this);
        seekBarController.startThread();

        CurrentPlaybackNotifier.getSelf().attach(this);

        setOnClickListeners();
    }

    public void setOnClickListeners() {
        ImageButton pausePlayBtn = findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
            }
        });

        ImageButton skipNextBtn = findViewById(R.id.skipNextBtn);

        skipNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                mediaController.getTransportControls().skipToNext();
            }
        });

        ImageButton skipPrevBtn = findViewById(R.id.skipPrevBtn);

        skipPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                mediaController.getTransportControls().skipToPrevious();
            }
        });

        final ImageButton shuffleBtn = findViewById(R.id.shuffleBtn);

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayQueue.getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    PlayQueue.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                    shuffleBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                } else {
                    PlayQueue.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    shuffleBtn.setColorFilter(getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                    PlayQueue.shuffle();
                }
            }
        });

        final ImageButton repeatBtn = findViewById(R.id.repeatBtn);

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayQueue.setRepeatMode(PlayQueue.getNextRepeatMode());

                if (PlayQueue.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                    repeatBtn.setColorFilter(getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                } else if (PlayQueue.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_NONE) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                    repeatBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                } else if (PlayQueue.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                    repeatBtn.setColorFilter(getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });
    }

    public String parseTime(int time) {
        time = time / 1000;
        String s = (time % 60) + "";
        if (s.length() == 1) { s = "0" + s; }
        String m = ((time - (time % 60))/60) + "";

        return m + ":" + s;
    }

    @Override
    protected void onStop() {
        super.onStop();
        overridePendingTransition(R.anim.empty_transition, R.anim.slide_out_down);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        seekBarController.killThread();
        CurrentPlaybackNotifier.getSelf().detach(this);
    }

    private void updateCurrTime(int time) {
        ((TextView)findViewById(R.id.currentTimeTextView)).setText(parseTime(time));
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof PlaybackStateCompat) {
            PlaybackStateCompat state = (PlaybackStateCompat)o;
            ImageButton pausePlayBtn = findViewById(R.id.pausePlayBtn);

            if (state.getState() == PlaybackStateCompat.STATE_PAUSED || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
                pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
            }
        } else if (o instanceof MediaMetadataCompat) {
            MediaMetadataCompat metadata = (MediaMetadataCompat)o;

            TextView songName = findViewById(R.id.songNameTextView);
            TextView artistName = findViewById(R.id.artistNameTextView);

            songName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            ((TextView)findViewById(R.id.totalTimeTextView)).setText(parseTime(PlayQueue.getSelf().getCurrSong().getDuration().intValue()));
        } else if (o instanceof Integer) {

        }
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<CurrentlyPlayingActivity> myClassWeakReference;

        public MyHandler(CurrentlyPlayingActivity myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            CurrentlyPlayingActivity myClass = myClassWeakReference.get();
            if (myClass != null) {
                myClass.updateCurrTime(msg.arg1);
            }
        }
    }

    public Handler getHandler() {
        return new MyHandler(this);
    }
}
