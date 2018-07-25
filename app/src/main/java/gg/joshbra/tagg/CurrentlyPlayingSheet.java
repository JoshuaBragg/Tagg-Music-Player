package gg.joshbra.tagg;

import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;

public class CurrentlyPlayingSheet implements Observer {
    private MediaControllerCompat mediaController;
    private SeekBarController seekBarController;
    private RelativeLayout relativeLayout;
    private BottomSheetBehavior bottomSheetBehavior;

    public CurrentlyPlayingSheet(final RelativeLayout relativeLayout) {
        this.relativeLayout = relativeLayout;

        mediaController = MediaControllerHolder.getMediaController();

        if (PlayQueue.getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            ImageButton shuffleBtn = relativeLayout.findViewById(R.id.shuffleBtn);
            shuffleBtn.setColorFilter(relativeLayout.getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
        }

        seekBarController = new SeekBarController(relativeLayout, this);
        seekBarController.startThread();

        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // do nothing
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                LinearLayout peekBar = relativeLayout.findViewById(R.id.peekBar);
                peekBar.setAlpha(fadeCurve(slideOffset));
            }
        });

        CurrentPlaybackNotifier.getSelf().attach(this);

        setOnClickListeners();
    }

    private float fadeCurve(float slideOffset) {
        return slideOffset < .625 ? ((float)1.6 * slideOffset - (float)1.0) * ((float)1.6 * slideOffset - (float)1.0) : 0;
    }

    public void setOnClickListeners() {
        ImageButton pausePlayBtn = relativeLayout.findViewById(R.id.pausePlayBtn);

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

        ImageButton pausePlayBtnPeek = relativeLayout.findViewById(R.id.pausePlayBtnPeek);

        pausePlayBtnPeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) { return; }

                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
            }
        });

        ImageButton skipNextBtn = relativeLayout.findViewById(R.id.skipNextBtn);

        skipNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                mediaController.getTransportControls().skipToNext();
            }
        });

        ImageButton skipPrevBtn = relativeLayout.findViewById(R.id.skipPrevBtn);

        skipPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                mediaController.getTransportControls().skipToPrevious();
            }
        });

        final ImageButton shuffleBtn = relativeLayout.findViewById(R.id.shuffleBtn);

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayQueue.getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    PlayQueue.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                    shuffleBtn.setColorFilter(relativeLayout.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                } else {
                    PlayQueue.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    shuffleBtn.setColorFilter(relativeLayout.getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                    PlayQueue.shuffle();
                }
            }
        });

        final ImageButton repeatBtn = relativeLayout.findViewById(R.id.repeatBtn);

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayQueue.setRepeatMode(PlayQueue.getNextRepeatMode());

                if (PlayQueue.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                    repeatBtn.setColorFilter(relativeLayout.getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                } else if (PlayQueue.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_NONE) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                    repeatBtn.setColorFilter(relativeLayout.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                } else if (PlayQueue.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                    repeatBtn.setColorFilter(relativeLayout.getResources().getColor(R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });

        LinearLayout peekBar = relativeLayout.findViewById(R.id.peekBar);
        peekBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null)) {
                    return;
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

    public void destroy() {
        seekBarController.killThread();
        CurrentPlaybackNotifier.getSelf().detach(this);
    }

    private void updateCurrTime(int time) {
        ((TextView)relativeLayout.findViewById(R.id.currentTimeTextView)).setText(parseTime(time));
    }

    @Override
    public void update(Observable observable, Object o) {
        if (MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null) {
            relativeLayout.findViewById(R.id.pausePlayBtnPeek).setVisibility(View.VISIBLE);
        }

        if (o instanceof PlaybackStateCompat) {
            PlaybackStateCompat state = (PlaybackStateCompat)o;
            ImageButton pausePlayBtn = relativeLayout.findViewById(R.id.pausePlayBtn);
            ImageButton pausePlayBtnPeek = relativeLayout.findViewById(R.id.pausePlayBtnPeek);

            if (state.getState() == PlaybackStateCompat.STATE_PAUSED || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
                pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                pausePlayBtnPeek.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                pausePlayBtnPeek.setImageResource(R.drawable.ic_pause_white_24dp);
            }
        } else if (o instanceof MediaMetadataCompat) {
            MediaMetadataCompat metadata = (MediaMetadataCompat)o;

            TextView songName = relativeLayout.findViewById(R.id.songNameTextView);
            TextView artistName = relativeLayout.findViewById(R.id.artistNameTextView);

            songName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            TextView songNamePeek = relativeLayout.findViewById(R.id.songNameTextViewPeek);
            TextView artistNamePeek = relativeLayout.findViewById(R.id.artistNameTextViewPeek);

            songNamePeek.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistNamePeek.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            ((TextView)relativeLayout.findViewById(R.id.totalTimeTextView)).setText(parseTime(PlayQueue.getSelf().getCurrSong().getDuration().intValue()));
        }
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<CurrentlyPlayingSheet> myClassWeakReference;

        public MyHandler(CurrentlyPlayingSheet myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            CurrentlyPlayingSheet myClass = myClassWeakReference.get();
            if (myClass != null) {
                myClass.updateCurrTime(msg.arg1);
            }
        }
    }

    public Handler getHandler() {
        return new MyHandler(this);
    }

    public interface UsesCurrentlyPlayingSheet {
        public void nothing(View v);
        public boolean onKeyDown(int keyCode, KeyEvent event);
    }
}
