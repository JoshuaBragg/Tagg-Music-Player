package gg.joshbra.tagg;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;

import gg.joshbra.tagg.Activities.MainActivity;
import gg.joshbra.tagg.Activities.TaggActivity;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> implements INameableAdapter {

    private ArrayList<SongInfo> songs;
    private Context context;
    private MediaControllerCompat mediaController;

    @Override
    public Character getCharacterForElement(int element) {
        return songs.get(element).getSongName().charAt(0);
    }

    public SongAdapter(Context context, MediaControllerCompat mediaController, ArrayList<SongInfo> songs) {
        this.context = context;
        this.mediaController = mediaController;
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.row_song, parent, false);
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
                }
                Log.i("d", "juice " + mediaController);
                mediaController.getTransportControls().playFromMediaId(c.getMediaID(), null);
            }
        });

        holder.dropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context wrapper = new ContextThemeWrapper(context, R.style.PopupMenu);
                final PopupMenu popup = new PopupMenu(wrapper, holder.dropDownMenu);

                popup.inflate(R.menu.song_menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.updateTaggs:
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                final ViewGroup container = (ViewGroup) inflater.inflate(R.layout.tagg_selection_popup_simple, null);

                                populateList(container, c);

                                final PopupWindow popupWindow = new PopupWindow(container, (int) Math.round(Resources.getSystem().getDisplayMetrics().widthPixels * (2.0 / 3.0)), ViewGroup.LayoutParams.WRAP_CONTENT, true);
                                popupWindow.setTouchable(true);
                                popupWindow.setFocusable(true);

                                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                                popupWindow.setElevation(50);

                                popupWindow.showAtLocation(holder.getView(), Gravity.CENTER, 0, 0);

                                Button updateBtn = container.findViewById(R.id.updateTaggBtn);

                                updateBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Toast.makeText(context, "Taggs Updated", Toast.LENGTH_SHORT).show();
                                        RecyclerView recyclerView = container.findViewById(R.id.taggSelectRGroup);
                                        TaggAdapter adapter = (TaggAdapter) recyclerView.getAdapter();
                                        if (adapter == null) { popupWindow.dismiss(); return; }
                                        ArrayList<String> updateTaggs = adapter.getCheckedTaggs();
                                        SongManager.getSelf().updateSongTaggRelations(c, updateTaggs);
                                        popupWindow.dismiss();
                                    }
                                });

                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popup.show();
            }
        });
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

    public class SongHolder extends RecyclerView.ViewHolder {
        TextView songName, artistName, dropDownMenu;
        View view;

        public SongHolder(View itemView) {
            super(itemView);
            view = itemView;
            songName = itemView.findViewById(R.id.songNameTextView);
            artistName = itemView.findViewById(R.id.artistNameTextView);
            dropDownMenu = itemView.findViewById(R.id.textViewOptions);
        }

        public View getView() {
            return view;
        }
    }
}
