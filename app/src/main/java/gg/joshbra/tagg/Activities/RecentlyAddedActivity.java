package gg.joshbra.tagg.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import gg.joshbra.tagg.Comparators.SongComparator;
import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.Helpers.AboutDialogGenerator;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SleepTimerController;
import gg.joshbra.tagg.SongManager;

/**
 * Activity which has a list of chronologically sorted songs
 */
public class RecentlyAddedActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private SongListFragment songListFragment;
    private CurrentlyPlayingSheet currentlyPlayingSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_added);

        // Set up side drawer menu
        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Recently Added");

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        songListFragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songList);
        songListFragment.initRecycler(SongManager.getSelf().getDateSortedSongs(SongComparator.SORT_DATE_DESC));

        View v = findViewById(R.id.bottomSheet);

        currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
        bottomSheetBehavior = BottomSheetBehavior.from(v);

        // Manually update the CurrentPlaybackNotifier to trigger the update methods of this classes members
        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
        CurrentPlaybackNotifier.getSelf().notifyMetadataChanged(MediaControllerHolder.getMediaController().getMetadata());
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
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(this, TaggActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.recentMenu) {
            item.setChecked(true);
        } else if (id == R.id.aboutMenu) {
            AboutDialogGenerator.createDialog(this);
        }

        mDrawerLayout.closeDrawer(Gravity.START);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NavigationView)findViewById(R.id.navView)).getMenu().getItem(2).setChecked(true);
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