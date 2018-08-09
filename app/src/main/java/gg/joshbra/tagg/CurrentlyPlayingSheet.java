package gg.joshbra.tagg;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import gg.joshbra.tagg.Fragments.BottomSongMenuDialogFragment;
import gg.joshbra.tagg.Fragments.BottomTaggUpdateFragment;
import gg.joshbra.tagg.Helpers.AlbumArtRetriever;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;

public class CurrentlyPlayingSheet implements Observer {
    private MediaControllerCompat mediaController;
    private SeekBarController seekBarController;
    private ConstraintLayout constraintLayout;
    private BottomSheetBehavior bottomSheetBehavior;

    private final int ALBUM_ART_SIZE = (int) Math.round(Resources.getSystem().getDisplayMetrics().widthPixels * (5.0 / 9.0));

    public CurrentlyPlayingSheet(final ConstraintLayout constraintLayout) {
        this.constraintLayout = constraintLayout;

        mediaController = MediaControllerHolder.getMediaController();

        if (PlayQueue.getSelf().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            ImageButton shuffleBtn = constraintLayout.findViewById(R.id.shuffleBtn);
            shuffleBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
        }

        ImageView albumArtImageView = constraintLayout.findViewById(R.id.albumArtImageView);
        String albumPath = null;
        if (PlayQueue.getSelf().getCurrSong() != null) {
            albumPath = AlbumArtRetriever.getAlbumArt(Integer.valueOf(PlayQueue.getSelf().getCurrSong().getAlbumID()));
        }
        if (albumPath != null) {
            Drawable image = Drawable.createFromPath(albumPath);
            albumArtImageView.getLayoutParams().height = ALBUM_ART_SIZE;
            albumArtImageView.getLayoutParams().width = ALBUM_ART_SIZE;
            albumArtImageView.setImageDrawable(image);
        } else {
            albumArtImageView.getLayoutParams().height = ALBUM_ART_SIZE;
            albumArtImageView.getLayoutParams().width = ALBUM_ART_SIZE;
            albumArtImageView.setImageDrawable(ContextCompat.getDrawable(constraintLayout.getContext(), R.drawable.ic_album_white_24dp));
        }

        seekBarController = new SeekBarController(constraintLayout, this);
        seekBarController.startThread();

        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // do nothing
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                ConstraintLayout peekBar = constraintLayout.findViewById(R.id.peekBar);
                peekBar.setAlpha(fadeCurve(slideOffset));
            }
        });

        CurrentPlaybackNotifier.getSelf().attach(this);

        setOnClickListeners();
    }

    private float fadeCurve(float slideOffset) {
        return slideOffset < .625 ? ((float)1.6 * slideOffset - (float)1.0) * ((float)1.6 * slideOffset - (float)1.0) : 0;
    }

    private void setOnClickListeners() {
        ImageButton pausePlayBtn = constraintLayout.findViewById(R.id.pausePlayBtn);

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

        ImageButton pausePlayBtnPeek = constraintLayout.findViewById(R.id.pausePlayBtnPeek);

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

        ImageButton skipNextBtn = constraintLayout.findViewById(R.id.skipNextBtn);

        skipNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                mediaController.getTransportControls().skipToNext();
            }
        });

        ImageButton skipPrevBtn = constraintLayout.findViewById(R.id.skipPrevBtn);

        skipPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController.getPlaybackState() == null || mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) { return; }

                mediaController.getTransportControls().skipToPrevious();
            }
        });

        final ImageButton shuffleBtn = constraintLayout.findViewById(R.id.shuffleBtn);

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayQueue.getSelf().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    PlayQueue.getSelf().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                    shuffleBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                } else {
                    PlayQueue.getSelf().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    shuffleBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });

        final ImageButton repeatBtn = constraintLayout.findViewById(R.id.repeatBtn);

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayQueue.getSelf().setRepeatMode(PlayQueue.getSelf().getNextRepeatMode());

                if (PlayQueue.getSelf().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                    repeatBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                } else if (PlayQueue.getSelf().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_NONE) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                    repeatBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                } else if (PlayQueue.getSelf().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                    repeatBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });

        ConstraintLayout peekBar = constraintLayout.findViewById(R.id.peekBar);

        peekBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null)) {
                    return;
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        TextView songMenu = constraintLayout.findViewById(R.id.textViewOptions);

        songMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PlayQueue.getSelf().getCurrSong() == null) { return; }

                final SongInfo songInfo = PlayQueue.getSelf().getCurrSong();

                BottomSongMenuDialogFragment bottomSongMenuDialogFragment = new BottomSongMenuDialogFragment();

                Bundle bundle = new Bundle();

                bundle.putString(BottomSongMenuDialogFragment.SONG_NAME, songInfo.getSongName());

                bottomSongMenuDialogFragment.setArguments(bundle);

                bottomSongMenuDialogFragment.setListener(new BottomSongMenuDialogFragment.BottomMenuListener() {
                    @Override
                    public void onOptionSelected(String type) {
                        switch (type) {
                            case (BottomSongMenuDialogFragment.OPTION_PLAY_NEXT):
                                PlayQueue.getSelf().insertSongNextInQueue(songInfo);
                                Toast toast = Toast.makeText(constraintLayout.getContext(), "Song added to queue", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) constraintLayout.getContext().getResources().getDimension(R.dimen.toast_offset));
                                toast.show();
                                break;
                            case (BottomSongMenuDialogFragment.OPTION_PLAY):
                                MediaControllerHolder.getMediaController().getTransportControls().playFromMediaId(songInfo.getMediaID().toString(), null);
                                break;
                            case (BottomSongMenuDialogFragment.OPTION_UPDATE_TAGGS):
                                BottomTaggUpdateFragment bottomTaggUpdateFragment = new BottomTaggUpdateFragment();

                                bottomTaggUpdateFragment.setListener(new BottomTaggUpdateFragment.BottomTaggUpdateListener() {
                                    @Override
                                    public SongInfo getSong() {
                                        return songInfo;
                                    }
                                });

                                bottomTaggUpdateFragment.show(((AppCompatActivity)constraintLayout.getContext()).getSupportFragmentManager(), "bottomTaggUpdateSheet");
                                break;
                        }
                    }
                });

                bottomSongMenuDialogFragment.show(((AppCompatActivity)constraintLayout.getContext()).getSupportFragmentManager(), "bottomSheet");
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
        ((TextView) constraintLayout.findViewById(R.id.currentTimeTextView)).setText(parseTime(time));
    }

    @Override
    public void update(Observable observable, Object o) {
        if (MediaControllerHolder.getMediaController() != null && MediaControllerHolder.getMediaController().getPlaybackState() != null && MediaControllerHolder.getMediaController().getMetadata() != null) {
            constraintLayout.findViewById(R.id.pausePlayBtnPeek).setVisibility(View.VISIBLE);
            constraintLayout.findViewById(R.id.textViewOptions).setVisibility(View.VISIBLE);
        }

        if (o instanceof PlaybackStateCompat) {
            PlaybackStateCompat state = (PlaybackStateCompat)o;
            ImageButton pausePlayBtn = constraintLayout.findViewById(R.id.pausePlayBtn);
            ImageButton pausePlayBtnPeek = constraintLayout.findViewById(R.id.pausePlayBtnPeek);

            if (state.getState() == PlaybackStateCompat.STATE_PAUSED || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
                pausePlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                pausePlayBtnPeek.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                pausePlayBtnPeek.setImageResource(R.drawable.ic_pause_white_24dp);
            }

            ImageButton repeatBtn = constraintLayout.findViewById(R.id.repeatBtn);

            if (PlayQueue.getSelf().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
            } else if (PlayQueue.getSelf().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_NONE) {
                repeatBtn.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            } else if (PlayQueue.getSelf().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                repeatBtn.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                repeatBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
            }

            ImageButton shuffleBtn = constraintLayout.findViewById(R.id.shuffleBtn);

            if (PlayQueue.getSelf().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                shuffleBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorActivated), PorterDuff.Mode.SRC_ATOP);
            } else {
                shuffleBtn.setColorFilter(ContextCompat.getColor(constraintLayout.getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            }

        } else if (o instanceof MediaMetadataCompat) {
            MediaMetadataCompat metadata = (MediaMetadataCompat)o;

            ImageView albumArtImageView = constraintLayout.findViewById(R.id.albumArtImageView);
            String albumPath = null;
            if (PlayQueue.getSelf().getCurrSong() != null) {
                albumPath = AlbumArtRetriever.getAlbumArt(Integer.valueOf(PlayQueue.getSelf().getCurrSong().getAlbumID()));
            }
            if (albumPath != null) {
                Drawable image = Drawable.createFromPath(albumPath);
                albumArtImageView.getLayoutParams().height = ALBUM_ART_SIZE;
                albumArtImageView.getLayoutParams().width = ALBUM_ART_SIZE;
                albumArtImageView.setImageDrawable(image);
            } else {
                albumArtImageView.getLayoutParams().height = ALBUM_ART_SIZE;
                albumArtImageView.getLayoutParams().width = ALBUM_ART_SIZE;
                albumArtImageView.setImageDrawable(ContextCompat.getDrawable(constraintLayout.getContext(), R.drawable.ic_album_white_24dp));
            }

            TextView songName = constraintLayout.findViewById(R.id.songNameTextView);
            TextView artistName = constraintLayout.findViewById(R.id.artistNameTextView);

            songName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            TextView songNamePeek = constraintLayout.findViewById(R.id.songNameTextViewPeek);
            TextView artistNamePeek = constraintLayout.findViewById(R.id.artistNameTextViewPeek);

            songNamePeek.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistNamePeek.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            ((TextView) constraintLayout.findViewById(R.id.totalTimeTextView)).setText(parseTime(PlayQueue.getSelf().getCurrSong().getDuration().intValue()));
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
