package gg.joshbra.tagg.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Fragments.BottomTaggSelectionFragment;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.Helpers.AboutDialogGenerator;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongManager;

/**
 * Activity that allows user to enable/disable Taggs and generate playlists
 */
public class TaggActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private SongManager songManager;

    private SongListFragment songListFragment;
    private CurrentlyPlayingSheet currentlyPlayingSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagg);
        
        songManager = SongManager.getSelf();

        // Set up side drawer menu
        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Taggs");

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        songListFragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songList);

        View v = findViewById(R.id.bottomSheet);

        currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
        bottomSheetBehavior = BottomSheetBehavior.from(v);

        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
        CurrentPlaybackNotifier.getSelf().notifyMetadataChanged(MediaControllerHolder.getMediaController().getMetadata());

        updateSongRepeater();

        // Set up FAB functionality

        FloatingActionButton fab = findViewById(R.id.taggFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomTaggSelectionFragment bottomTaggSelectionFragment = new BottomTaggSelectionFragment();

                bottomTaggSelectionFragment.setListener(new BottomTaggSelectionFragment.BottomTaggSelectionListener() {
                    @Override
                    public void dismissed(ArrayList<String> aTagg) {
                        if (!SongManager.getSelf().getActiveTaggs().equals(aTagg))
                            updateSongRepeater();
                    }
                });

                bottomTaggSelectionFragment.show(getSupportFragmentManager(), "bottomTaggSheet");
            }
        });
    }

    /**
     * Sets title to include the current amount of active Taggs and initilizes SongListFragment
     */
    private void updateSongRepeater() {
        setTitle(songManager.getActiveTaggs().size() == 0 ? "Taggs" : "Taggs (" + songManager.getActiveTaggs().size() + ")");
        songListFragment.initRecycler(songManager.getCurrSongsFromTaggs());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handles launching different activities from Drawer Menu

        if (id == R.id.songMenu) {
            item.setChecked(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(this, gg.joshbra.tagg.Activities.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.taggMenu) {
            item.setChecked(true);
        } else if (id == R.id.recentMenu) {
            item.setChecked(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(this, RecentlyAddedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.aboutMenu) {
            AboutDialogGenerator.createDialog(this);
        }

        mDrawerLayout.closeDrawer(Gravity.START);

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NavigationView)findViewById(R.id.navView)).getMenu().getItem(1).setChecked(true);
        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
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
}
