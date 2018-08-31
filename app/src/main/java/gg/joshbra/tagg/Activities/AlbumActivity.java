package gg.joshbra.tagg.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.MaterialScrollBar;

import java.util.ArrayList;
import java.util.Collections;

import gg.joshbra.tagg.Adapters.AlbumAdapter;
import gg.joshbra.tagg.AlbumInfo;
import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Helpers.AboutDialogGenerator;
import gg.joshbra.tagg.Helpers.AlbumGridScaler;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.ItemDecorationAlbumColumns;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SleepTimerController;

/**
 * Activity that allows user to browse albums from their music library
 */
public class AlbumActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private CurrentlyPlayingSheet currentlyPlayingSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView recyclerView;

    private static ArrayList<AlbumInfo> loadedAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Set up side drawer menu
        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Albums");

        // Set up side menu drawer
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(3).setChecked(true);

        // Initialize recycler view (recycler view is local because there will not be another screen
        // needing to list albums therefore no need for separate fragment
        recyclerView = findViewById(R.id.albumRecycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(((Long)Math.round(AlbumGridScaler.getItemSpacingPixels(this))).intValue(), 2));
        recyclerView.setLayoutManager(gridLayoutManager);

        loadedAlbums = new ArrayList<>();
        populateRecycler();

        View v = findViewById(R.id.bottomSheet);

        currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
        bottomSheetBehavior = BottomSheetBehavior.from(v);

        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
        CurrentPlaybackNotifier.getSelf().notifyMetadataChanged(MediaControllerHolder.getMediaController().getMetadata());
    }

    /**
     * Loads albums from device and stores list within loadedAlbums
     * Creates adapter for recyclerView
     */
    private void populateRecycler() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permissions were not granted and we cannot read albums
            Toast toast = Toast.makeText(this, "Permission Denied. Storage permission is required to read albums from device. Please go to your settings and enable permissions to use Tagg.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) getResources().getDimension(R.dimen.toast_offset));
            toast.show();
            return;
        }

        // Load albums from device
        String[] projection = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
        String sortOrder = MediaStore.Audio.Media.ALBUM + " ASC";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String albumID = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
                    String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                    String albumArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
                    String albumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    long numSongs = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));

                    loadedAlbums.add(new AlbumInfo(albumID, albumName, albumArtist, albumArt, numSongs));
                } while (cursor.moveToNext());
            }
            cursor.close();

            Collections.sort(loadedAlbums);

            setTitle("Albums" + (loadedAlbums.size() == 0 ? "" : " (" + loadedAlbums.size() + ")"));

            recyclerView.setAdapter(new AlbumAdapter(this, loadedAlbums));
        }
    }

    public static ArrayList<AlbumInfo> getLoadedAlbums() {
        return loadedAlbums;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.sleepTimerOption:
                SleepTimerController.getSelf().create(this);
                return true;
            case R.id.searchOption:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
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
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(this, RecentlyAddedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.albumMenu) {
            item.setChecked(true);
        } else if (id == R.id.aboutMenu) {
            AboutDialogGenerator.createDialog(this);
        }

        mDrawerLayout.closeDrawer(Gravity.START);

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NavigationView)findViewById(R.id.navView)).getMenu().getItem(3).setChecked(true);
        CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
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
}
