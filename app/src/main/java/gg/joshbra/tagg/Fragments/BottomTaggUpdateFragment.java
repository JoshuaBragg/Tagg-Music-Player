package gg.joshbra.tagg.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import gg.joshbra.tagg.Adapters.TaggAdapter;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;
import gg.joshbra.tagg.SongManager;
import gg.joshbra.tagg.TaggSelector;

/**
 * Fragment for the menu that appears from bottom of screen when updating Taggs
 */
public class BottomTaggUpdateFragment extends BottomSheetDialogFragment {
    private BottomTaggUpdateListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View hostView = inflater.inflate(R.layout.bottom_tagg_update, container, false);

        populateList(hostView, listener.getSong());

        Button updateBtn = hostView.findViewById(R.id.updateTaggBtn);

        // The update button
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getContext(), "Taggs Updated", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) getResources().getDimension(R.dimen.toast_offset));
                toast.show();
                RecyclerView recyclerView = hostView.findViewById(R.id.taggSelectRGroup);
                TaggAdapter adapter = (TaggAdapter) recyclerView.getAdapter();
                if (adapter == null) { dismiss(); return; }
                ArrayList<String> updateTaggs = adapter.getCheckedTaggs();
                SongManager.getSelf().updateSongTaggRelations(listener.getSong(), updateTaggs);
                dismiss();
            }
        });

        return hostView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    /**
     * Populates the recyclerView with the checkboxes
     * @param view The view containing the RecyclerView
     * @param songInfo The song who's Taggs are being queried
     */
    private void populateList(View view, SongInfo songInfo) {
        RecyclerView recyclerView = view.findViewById(R.id.taggSelectRGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        ArrayList<String> taggs = SongManager.getSelf().getTaggs();

        if (taggs.size() == 0) {
            // In this case there are no Taggs yet
            TextView noTaggs = new TextView(getContext());
            noTaggs.setText("No Taggs exist");
            noTaggs.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextSecondary));
            noTaggs.setPadding(0, 50, 0, 20);
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).addView(noTaggs);
            return;
        } else {
            // This is needed to get rid of anything that is in the view right now
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

    public void setListener(BottomTaggUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Interface to be created when using this fragment
     */
    public interface BottomTaggUpdateListener {
        SongInfo getSong();
    }
}
