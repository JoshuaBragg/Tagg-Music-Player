package gg.joshbra.tagg.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import gg.joshbra.tagg.Activities.AlbumSongListActivity;
import gg.joshbra.tagg.Activities.MainActivity;
import gg.joshbra.tagg.Activities.RecentlyAddedActivity;
import gg.joshbra.tagg.Activities.SearchActivity;
import gg.joshbra.tagg.Activities.TaggActivity;
import gg.joshbra.tagg.Fragments.BottomSongMenuDialogFragment;
import gg.joshbra.tagg.Fragments.BottomTaggUpdateFragment;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.MusicController;
import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;
import gg.joshbra.tagg.SongManager;

/**
 * RecyclerView Adapter that allows Tagg to create lists of songs
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> implements INameableAdapter {

    private ArrayList<SongInfo> songs;
    private Context context;
    private MediaControllerCompat mediaController;

    private int[] activeRows = new int[]{};

    // Required to allow the Letter to appear on scrolling through alphabetical orders song list
    @Override
    public Character getCharacterForElement(int element) {
        try {
            return songs.get(element).getSongName().toUpperCase().charAt(0);
        } catch (IndexOutOfBoundsException e) {
            return '~';
        }
    }

    /**
     * Creates a SongAdapter for use in a RecyclerView
     * @param context The context that contains the RecyclerView
     * @param songs The arraylist of songs to display
     */
    public SongAdapter(Context context, ArrayList<SongInfo> songs) {
        this.context = context;
        this.mediaController = MediaControllerHolder.getMediaController();
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView;
        // Has two cases, when the row is active or regular
        if (viewType == 0) {
            myView = LayoutInflater.from(context).inflate(R.layout.row_song, parent, false);
        } else {
            myView = LayoutInflater.from(context).inflate(R.layout.row_song_active, parent, false);
        }
        return new SongHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHolder holder, int i) {
        final SongInfo c = songs.get(i);

        holder.songName.setText(c.getSongName());
        holder.artistName.setText(c.getArtistName());

        // Initializing the onClickListeners for this Holder

        // Plays song on click
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof MainActivity) {
                    SongManager.getSelf().updateCurrQueue(SongManager.TYPE_ALL_SONGS);
                } else if (context instanceof TaggActivity) {
                    SongManager.getSelf().updateCurrQueue(SongManager.TYPE_TAGG);
                } else if (context instanceof RecentlyAddedActivity) {
                    SongManager.getSelf().updateCurrQueue(SongManager.TYPE_RECENT);
                } else if (context instanceof SearchActivity) {
                    SongManager.getSelf().updateCurrQueue(SongManager.TYPE_ALL_SONGS);
                } else if (context instanceof AlbumSongListActivity) {
                    SongManager.getSelf().updateCurrQueue(SongManager.TYPE_ALBUM);
                }
                Bundle extra = new Bundle();
                extra.putInt(MusicController.PLAY_TYPE, MusicController.PLAY_BY_USER);
                extra.putInt(MusicController.SONG_POSITION, holder.getAdapterPosition());
                if (mediaController == null) { return; }
                mediaController.getTransportControls().playFromMediaId(c.getMediaID().toString(), extra);
            }
        });

        // Creates BottomSongMenuDialogFragment when clicking the 3 Dots
        holder.dropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSongMenuDialogFragment bottomSongMenuDialogFragment = new BottomSongMenuDialogFragment();

                Bundle bundle = new Bundle();

                bundle.putString(BottomSongMenuDialogFragment.SONG_NAME, c.getSongName());

                bottomSongMenuDialogFragment.setArguments(bundle);

                bottomSongMenuDialogFragment.setListener(new BottomSongMenuDialogFragment.BottomMenuListener() {
                    @Override
                    public void onOptionSelected(String type) {
                        switch (type) {
                            case (BottomSongMenuDialogFragment.OPTION_PLAY_NEXT):
                                PlayQueue.getSelf().insertSongNextInQueue(c);
                                Toast toast = Toast.makeText(context, "Song added to queue", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) context.getResources().getDimension(R.dimen.toast_offset));
                                toast.show();
                                break;
                            case (BottomSongMenuDialogFragment.OPTION_PLAY):
                                holder.getView().callOnClick();
                                break;
                            case (BottomSongMenuDialogFragment.OPTION_UPDATE_TAGGS):
                                BottomTaggUpdateFragment bottomTaggUpdateFragment = new BottomTaggUpdateFragment();

                                bottomTaggUpdateFragment.setListener(new BottomTaggUpdateFragment.BottomTaggUpdateListener() {
                                    @Override
                                    public SongInfo getSong() {
                                        return c;
                                    }
                                });

                                bottomTaggUpdateFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "bottomTaggUpdateSheet");
                                break;
                        }
                    }
                });

                bottomSongMenuDialogFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "bottomSheet");
            }
        });

        // If this row is active, attach it to the notifier
        for (int n : activeRows) {
            if (n == i) {
                CurrentPlaybackNotifier.getSelf().attach(holder);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        for (int i : activeRows) {
            if (position == i) {
                return 1;
            }
        }
        return 0;
    }

    public SongInfo getSong(int i) {
        return songs.get(i);
    }

    /**
     * Gets the index of all rows that display songInfo
     * @param songInfo The song to search for
     * @return A int[] of the indices for rows that display songInfo
     */
    public int[] getRowsForSong(SongInfo songInfo) {
        ArrayList<Integer> temp = new ArrayList<>();

        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).equals(songInfo)) {
                temp.add(i);
            }
        }

        int[] out = new int[temp.size()];

        for (int i = 0; i < temp.size(); i++) {
            out[i] = temp.get(i);
        }

        return out;
    }

    public void setActiveRows(int[] activeRows) {
        this.activeRows = activeRows;
    }

    public int[] getActiveRows() {
        return activeRows;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * A ViewHolder for RecyclerView to hold the information obtained from a SongInfo
     */
    public class SongHolder extends RecyclerView.ViewHolder implements Observer{
        TextView songName, artistName, dropDownMenu;
        EqualizerView equalizer;
        View view;

        public SongHolder(View itemView) {
            super(itemView);
            view = itemView;
            songName = itemView.findViewById(R.id.songNameTextView);
            artistName = itemView.findViewById(R.id.artistNameTextView);
            dropDownMenu = itemView.findViewById(R.id.textViewOptions);
            equalizer = itemView.findViewById(R.id.equalizer);

            try {
                if (MediaControllerHolder.getMediaController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    equalizer.animateBars();
                } else {
                    equalizer.stopBars();
                }
            } catch (NullPointerException e) {}
        }

        public View getView() {
            return view;
        }

        public EqualizerView getEqualizer() {
            return equalizer;
        }

        @Override
        public void update(Observable o, Object arg) {
            if (arg instanceof PlaybackStateCompat) {
                if (((PlaybackStateCompat) arg).getState() == PlaybackStateCompat.STATE_PLAYING) {
                    equalizer.animateBars();
                } else {
                    equalizer.stopBars();
                }
            }
        }
    }
}
