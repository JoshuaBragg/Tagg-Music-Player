package gg.joshbra.tagg.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.MaterialScrollBar;

import java.util.ArrayList;

import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongAdapter;
import gg.joshbra.tagg.SongInfo;

public class SongListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;

    public SongListFragment() {
        // Required empty public constructor
    }

    public static SongListFragment newInstance() {
        return new SongListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = getView().findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);

        MaterialScrollBar scrollBar = getView().findViewById(R.id.touchScrollBar);
        scrollBar.setRecyclerView(recyclerView);
    }

    public void initRecycler(ArrayList<SongInfo> songs) {
        songAdapter = new SongAdapter(getContext(), songs);
        recyclerView.setAdapter(songAdapter);
    }

    public void enableIndicator(boolean e) {
        MaterialScrollBar scrollBar = getView().findViewById(R.id.touchScrollBar);
        scrollBar.setIndicator(new AlphabetIndicator(getContext()), true);
    }
}
