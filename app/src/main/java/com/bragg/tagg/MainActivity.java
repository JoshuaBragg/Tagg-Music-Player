package com.bragg.tagg;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements Observer {

    private RecyclerView recyclerView;
    private MediaController mediaController;
    private SongAdapter songAdapter;
    private ArrayList<SongInfo> songs;
    private SeekBarController seekBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        songs = new ArrayList<>();
        mediaController = MediaController.getSelf();
        mediaController.setSongs(songs);
        mediaController.attach(this);
        seekBarController = new SeekBarController(this);

        CheckPermission();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(songAdapter);

        setClickListeners();
    }

    private void setClickListeners() {
        Button pausePlayBtn = findViewById(R.id.pausePlayBtn);

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaController == null) {
                    return;
                }
                if (mediaController.isPlaying()) {
                    mediaController.pauseSong();
                } else {
                    mediaController.playSong();
                }
            }
        });

        View botBar = findViewById(R.id.botBar);

        botBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaController.songLoaded()) { return; }
                Intent intent = new Intent(MainActivity.this, CurrentlyPlayingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                return;
            }
        }
        loadSongs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123:
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

    void loadSongs() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    if (Pattern.matches(".*\\.mp3", name)) {
                        name = name.substring(0, name.length() - 4);
                    }

                    SongInfo s = new SongInfo(name, artist, url);
                    songs.add(s);
                }while (cursor.moveToNext());
            }
            cursor.close();
            songAdapter = new SongAdapter(this, mediaController, songs);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("playing", mediaController.isPlaying());
        outState.putString("currSongName", ((TextView)findViewById(R.id.songNameBotTextView)).getText().toString());
        outState.putString("currArtistName", ((TextView)findViewById(R.id.artistNameBotTextView)).getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("playing")) {
            findViewById(R.id.pausePlayBtn).setBackgroundResource(R.drawable.baseline_pause_white_18);
        }
        ((TextView)findViewById(R.id.songNameBotTextView)).setText(savedInstanceState.getString("currSongName"));
        ((TextView)findViewById(R.id.artistNameBotTextView)).setText(savedInstanceState.getString("currArtistName"));
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof SongInfo) {
            seekBarController.startThread();
            TextView songName = findViewById(R.id.songNameBotTextView);
            TextView artistName = findViewById(R.id.artistNameBotTextView);

            songName.setText( ((SongInfo)data).getSongName() );
            artistName.setText( ((SongInfo)data).getArtistName() );
        }
        else if (data instanceof Boolean) {
            seekBarController.startThread();
            boolean playing = (Boolean)data;
            Button pausePlayBtn = findViewById(R.id.pausePlayBtn);
            if (playing) {
                pausePlayBtn.setBackgroundResource(R.drawable.baseline_pause_white_18);
            } else {
                pausePlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_white_18);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        seekBarController.killThread();
    }
}
