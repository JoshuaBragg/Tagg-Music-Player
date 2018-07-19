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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

import gg.joshbra.tagg.CurrentPlaybackNotifier;
import gg.joshbra.tagg.Fragments.NowPlayingBarFragment;
import gg.joshbra.tagg.Fragments.SongListFragment;
import gg.joshbra.tagg.MediaControllerHolder;
import gg.joshbra.tagg.MusicService;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongInfo;
import gg.joshbra.tagg.SongManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private ArrayList<SongInfo> loadedSongs;
    private SongManager songManager;
    private SongListFragment songListFragment;
    private NowPlayingBarFragment nowPlayingBarFragment;
    private CurrentPlaybackNotifier currentPlaybackNotifier;

    private MediaBrowserCompat mediaBrowser;

    private final MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    MediaControllerCompat mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                    MediaControllerHolder.setMediaController(mediaController);

                    songListFragment.initRecycler(loadedSongs);

                    nowPlayingBarFragment.initNowPlayingBar();

                    currentPlaybackNotifier.notifyPlaybackStateChanged(mediaController.getPlaybackState());
                    currentPlaybackNotifier.notifyMetadataChanged(mediaController.getMetadata());

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
            //nowPlayingBarFragment.updatePlaybackState(null);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            currentPlaybackNotifier.notifyPlaybackStateChanged(state);
            //nowPlayingBarFragment.updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            currentPlaybackNotifier.notifyMetadataChanged(metadata);
            //nowPlayingBarFragment.updateMetadata(metadata);
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
        nowPlayingBarFragment = (NowPlayingBarFragment) getSupportFragmentManager().findFragmentById(R.id.nowPlayingBar);

        CheckPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback,null);
        mediaBrowser.connect();
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 120);
            } else {
                loadSongs();
            }
        } else {
            loadSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 120:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
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
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    // TODO: make permanent solution to quote and SQL injection
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    // TODO: figure out album art
                    String albumArt =  ""; //cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.)).replaceAll("'", "''");
                    String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));

                    if (Pattern.matches(".*\\.mp3", name)) {
                        name = name.substring(0, name.length() - 4);
                    }

                    SongInfo s = new SongInfo(id, name, artist, url, album, Long.parseLong(duration), albumArt, dateAdded);
                    loadedSongs.add(s);
                }while (cursor.moveToNext());
            }
            cursor.close();

            songManager.initDB(this);
            songManager.setSongList(loadedSongs);

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
        }

        else if (id == R.id.taggMenu) {
            item.setChecked(true);
            //Toast.makeText(this, "Taggs", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, TaggActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

        mDrawerLayout.closeDrawer(Gravity.START);

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NavigationView)findViewById(R.id.navView)).getMenu().getItem(0).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
    }
}
