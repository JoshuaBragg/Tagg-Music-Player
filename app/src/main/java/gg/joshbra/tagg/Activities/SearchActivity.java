package gg.joshbra.tagg.Activities;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.Helpers.SongFinder;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;
import gg.joshbra.tagg.SongManager;

/**
 * Activity allowing the user to search for a song
 */
public class SearchActivity extends AppCompatActivity implements CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {

    private SongListFragment songListFragment;
    private CurrentlyPlayingSheet currentlyPlayingSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set the actionbar to our custom one
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up fragment
        songListFragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songList);
        songListFragment.initRecycler(new ArrayList<SongInfo>());

        // Set up bottom sheet
        View v = findViewById(R.id.bottomSheet);

        currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
        bottomSheetBehavior = BottomSheetBehavior.from(v);

        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
        CurrentPlaybackNotifier.getSelf().notifyMetadataChanged(MediaControllerHolder.getMediaController().getMetadata());

        // Set listeners
        final ImageButton searchBtn = findViewById(R.id.searchBtn);

        final EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchBtn.performClick();
                return true;
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchBtn.performClick();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                if (!query.equals("")) {
                    songListFragment.initRecycler(searchSongs(query));
                }
            }
        });
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
     * Will return all songs which contain the query provided in the title
     * @param query The query to search for
     * @return Array of songs which have names that contain the query
     */
    private ArrayList<SongInfo> searchSongs(String query) {
        return SongFinder.searchByContains(query, SongManager.getSelf().getAllSongs());
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
