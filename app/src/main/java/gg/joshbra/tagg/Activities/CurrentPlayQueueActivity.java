package gg.joshbra.tagg.Activities;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.R;

/**
 * Activity allowing the user to view the current play queue
 */
public class CurrentPlayQueueActivity extends AppCompatActivity implements CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {

    private SongListFragment songListFragment;
    private CurrentlyPlayingSheet currentlyPlayingSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_play_queue);

        setTitle("Current Play Queue");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up fragment
        songListFragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songList);
        songListFragment.initRecycler(PlayQueue.tripletToSongInfoConvert(PlayQueue.getSelf().getCurrQueue()));

        // Set up bottom sheet
        View v = findViewById(R.id.bottomSheet);

        currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
        bottomSheetBehavior = BottomSheetBehavior.from(v);

        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
        CurrentPlaybackNotifier.getSelf().notifyMetadataChanged(MediaControllerHolder.getMediaController().getMetadata());
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlagManager.getSelf().setFlags(this);
        FlagManager.getSelf().setSongPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentlyPlayingSheet.destroy();
    }

    /**
     * Method which has no function but is required to make the Currently Playing Sheet 'solid'.
     * i.e. Prevents user from clicking elements below the Currently Playing Sheet
     * @param v
     */
    @Override
    public void nothing(View v) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
