package gg.joshbra.tagg.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import gg.joshbra.tagg.AlbumInfo;
import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Fragments.BottomTaggSelectionFragment;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.Helpers.AboutDialogGenerator;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SleepTimerController;
import gg.joshbra.tagg.SongManager;

/**
 * Activity allowing the user to select from list of songs from the previously selected album
 */
public class AlbumSongListActivity extends AppCompatActivity implements CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {
        private SongListFragment songListFragment;

        private static AlbumInfo currAlbum = null;

        private CurrentlyPlayingSheet currentlyPlayingSheet;
        private BottomSheetBehavior bottomSheetBehavior;

        public static final String ALBUM_NAME = "album_name";
        public static final String ALBUM_ID = "album_id";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_album_song_list);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            setTitle(getIntent().getStringExtra(ALBUM_NAME));

            // Retrieve the AlbumInfo for the album that was selected when launching this activity
            ArrayList<AlbumInfo> albums = AlbumActivity.getLoadedAlbums();
            for (AlbumInfo album : albums) {
                if (album.getAlbumID() == getIntent().getLongExtra(ALBUM_ID, -1)) {
                    currAlbum = album;
                    break;
                }
            }

            songListFragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songList);
            songListFragment.initRecycler(SongManager.getSelf().getAlbumSongs(currAlbum));

            View v = findViewById(R.id.bottomSheet);

            currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
            bottomSheetBehavior = BottomSheetBehavior.from(v);

            CurrentPlaybackNotifier.getSelf().notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
            CurrentPlaybackNotifier.getSelf().notifyMetadataChanged(MediaControllerHolder.getMediaController().getMetadata());
        }

        public static AlbumInfo getCurrAlbum() {
            return currAlbum;
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                finish();
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
        protected void onResume() {
            super.onResume();
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

