package gg.joshbra.tagg.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import gg.joshbra.tagg.Activities.MainActivity;
import gg.joshbra.tagg.Activities.RecentlyAddedActivity;
import gg.joshbra.tagg.Activities.TaggActivity;
import gg.joshbra.tagg.Fragments.BottomSongMenuDialogFragment;
import gg.joshbra.tagg.Fragments.BottomTaggSelectionFragment;
import gg.joshbra.tagg.Fragments.BottomTaggUpdateFragment;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.Comparators.SongComparator;
import gg.joshbra.tagg.SongInfo;
import gg.joshbra.tagg.SongManager;
import gg.joshbra.tagg.TaggSelector;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> implements INameableAdapter {

    private ArrayList<SongInfo> songs;
    private Context context;
    private MediaControllerCompat mediaController;

    private int activeRow = -1;

    @Override
    public Character getCharacterForElement(int element) {
        try {
            return songs.get(element).getSongName().charAt(0);
        } catch (IndexOutOfBoundsException e) {
            return '~';
        }
    }

    public SongAdapter(Context context, ArrayList<SongInfo> songs) {
        this.context = context;
        this.mediaController = MediaControllerHolder.getMediaController();
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView;
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
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof MainActivity) {
                    SongManager.getSelf().resetCurrSongs();
                } else if (context instanceof TaggActivity) {
                    SongManager.getSelf().updateCurrSongsFromTaggs();
                } else if (context instanceof RecentlyAddedActivity) {
                    SongManager.getSelf().updateCurrSongsFromSorted(SongComparator.SORT_DATE_DESC);
                }
                mediaController.getTransportControls().playFromMediaId(c.getMediaID().toString(), null);
            }
        });

        holder.dropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSongMenuDialogFragment bottomSongMenuDialogFragment = new BottomSongMenuDialogFragment();

                Bundle bundle = new Bundle();

                bundle.putString("songName", c.getSongName());

                bottomSongMenuDialogFragment.setArguments(bundle);

                bottomSongMenuDialogFragment.setListener(new BottomSongMenuDialogFragment.BottomMenuListener() {
                    @Override
                    public void onOptionSelected(String type) {
                        switch (type) {
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

        if (i == activeRow) {
            CurrentPlaybackNotifier.getSelf().attach(holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == activeRow) {
            return 1;
        }
        return 0;
    }

    public SongInfo getSong(int i) {
        return songs.get(i);
    }

    public void setActiveRow(int activeRow) {
        this.activeRow = activeRow;
    }

    public int getActiveRow() {
        return activeRow;
    }

    private void populateList(View view, SongInfo songInfo) {
        RecyclerView recyclerView = view.findViewById(R.id.taggSelectRGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        ArrayList<String> taggs = SongManager.getSelf().getTaggs();

        if (taggs.size() == 0) {
            TextView noTaggs = new TextView(context);
            noTaggs.setText("No Taggs exist");
            noTaggs.setTextColor(context.getResources().getColor(R.color.colorTextSecondary));
            noTaggs.setPadding(0, 50, 0, 20);
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).addView(noTaggs);
            return;
        } else {
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).removeAllViews();
        }

        ArrayList<TaggSelector> selectors = new ArrayList<>();

        ArrayList<String> songTaggs = SongManager.getSelf().getSongsRelatedTaggs(songInfo);

        for (String t : taggs) {
            selectors.add(new TaggSelector(t, songTaggs.contains(t)));
        }

        TaggAdapter taggAdapter = new TaggAdapter(view.getContext(), selectors, TaggAdapter.UPDATE_TYPE);

        recyclerView.setAdapter(taggAdapter);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

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
