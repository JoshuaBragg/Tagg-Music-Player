package gg.joshbra.tagg.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.Observable;

import gg.joshbra.tagg.Adapters.SongAdapter;
import gg.joshbra.tagg.Comparators.SongPlayOrderTripletComparator;
import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.SongInfo;

/**
 * A SongList fragment used to display the current play queue
 *
 * Will update the song list upon update of queue
 */
public class CurrentQueueSongListFragment extends SongListFragment {
    private int prevShuffleMode = PlayQueue.getSelf().getShuffleMode();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecycler(new ArrayList<SongInfo>());
    }

    @Override
    public void update(Observable o, Object arg) {
        if (PlayQueue.getSelf().getShuffleMode() != prevShuffleMode) {
            initRecycler(new ArrayList<SongInfo>());
        }

        int[] temp = new int[] { PlayQueue.getSelf().getCurrSongOrder() };
        ((SongAdapter) recyclerView.getAdapter()).setActiveRows(temp);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void initRecycler(ArrayList<SongInfo> songs) {
        prevShuffleMode = PlayQueue.getSelf().getShuffleMode();
        songAdapter = new SongAdapter(getContext(),
                PlayQueue.tripletToSongInfoConvert(PlayQueue.getSelf().getCurrQueue(),
                        new SongPlayOrderTripletComparator(PlayQueue.getSelf().getShuffleMode())));
        recyclerView.setAdapter(songAdapter);
        recyclerView.scrollToPosition(PlayQueue.getSelf().getCurrSongOrder());
    }
}
