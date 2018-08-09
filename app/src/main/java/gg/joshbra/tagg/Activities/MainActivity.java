package gg.joshbra.tagg.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

import gg.joshbra.tagg.CurrentlyPlayingSheet;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.Helpers.AboutDialogGenerator;
import gg.joshbra.tagg.Helpers.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Helpers.FlagManager;
import gg.joshbra.tagg.Helpers.MediaControllerHolder;
import gg.joshbra.tagg.MusicService;
import gg.joshbra.tagg.PlayQueue;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;
import gg.joshbra.tagg.SongManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CurrentlyPlayingSheet.UsesCurrentlyPlayingSheet {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private ArrayList<SongInfo> loadedSongs;
    private SongManager songManager;
    private SongListFragment songListFragment;
    private CurrentPlaybackNotifier currentPlaybackNotifier;
    private CurrentlyPlayingSheet currentlyPlayingSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private MediaBrowserCompat mediaBrowser;

    private final MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    MediaControllerCompat mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                    MediaControllerHolder.setMediaController(mediaController);

                    songListFragment.initRecycler(loadedSongs);

                    View v = findViewById(R.id.bottomSheet);

                    currentlyPlayingSheet = new CurrentlyPlayingSheet((ConstraintLayout) v);
                    bottomSheetBehavior = BottomSheetBehavior.from(v);

                    currentPlaybackNotifier.notifyPlaybackStateChanged(mediaController.getPlaybackState());
                    currentPlaybackNotifier.notifyMetadataChanged(mediaController.getMetadata());

                    setFlags();

                    mediaController.registerCallback(controllerCallback);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        };

    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            currentPlaybackNotifier.notifyPlaybackStateChanged(null);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            currentPlaybackNotifier.notifyPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            currentPlaybackNotifier.notifyMetadataChanged(metadata);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Songs");

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        loadedSongs = new ArrayList<>();
        songManager = SongManager.getSelf();
        currentPlaybackNotifier = CurrentPlaybackNotifier.getSelf();

        songListFragment = (SongListFragment) getSupportFragmentManager().findFragmentById(R.id.songList);
        songListFragment.enableIndicator(true);
        songListFragment.initRecycler(loadedSongs);

        CheckPermission();

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback,null);
        mediaBrowser.connect();
    }

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 120);
        } else {
            loadSongs();
        }
    }

    private void setFlags() {
        int[] flags = FlagManager.getSelf().getFlags();
        PlayQueue.getSelf().setShuffleMode(flags[0]);
        PlayQueue.getSelf().setRepeatMode(flags[1]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 120:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs();
                } else {
                    Toast toast = Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) getResources().getDimension(R.dimen.toast_offset));
                    toast.show();
                    CheckPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSongs() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do{
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String albumID = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));

                    if (Pattern.matches(".*\\.mp3", name)) {
                        name = name.substring(0, name.length() - 4);
                    }

                    SongInfo s = new SongInfo(id, name, artist, url, album, Long.parseLong(duration), albumID, dateAdded);
                    loadedSongs.add(s);
                }while (cursor.moveToNext());
            }
            cursor.close();

            songManager.initDB(this, loadedSongs);

            setTitle(loadedSongs.size() == 0 ? "Songs" : "Songs (" + loadedSongs.size() + ")");

            songListFragment.initRecycler(loadedSongs);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.songMenu) {
            item.setChecked(true);
            //Toast.makeText(this, "Songs", Toast.LENGTH_LONG).show();
        } else if (id == R.id.taggMenu) {
            item.setChecked(true);
            //Toast.makeText(this, "Taggs", Toast.LENGTH_LONG).show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(this, TaggActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.recentMenu) {
            item.setChecked(true);
            //Toast.makeText(this, "Recently Added", Toast.LENGTH_LONG).show();
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
    public void nothing(View v) {}

    @Override
    protected void onResume() {
        super.onResume();
        ((NavigationView)findViewById(R.id.navView)).getMenu().getItem(0).setChecked(true);
        try {
            currentPlaybackNotifier.notifyPlaybackStateChanged(MediaControllerHolder.getMediaController().getPlaybackState());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
        currentlyPlayingSheet.destroy();
        FlagManager.getSelf().setFlags(this);
        FlagManager.getSelf().setSongPreferences(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
