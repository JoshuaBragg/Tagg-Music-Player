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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView recyclerView;
    private MediaController mediaController;
    private SongAdapter songAdapter;
    private ArrayList<SongInfo> songs;
    private SongManager songManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        recyclerView = findViewById(R.id.recyclerView);
        songs = new ArrayList<>();
        mediaController = MediaController.getSelf();
        mediaController.setSongs(songs);

        songManager = SongManager.getSelf();

        CheckPermission();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSongs() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    // TODO: make permanent solution to quote and SQL injection
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)).replaceAll("'", "''");
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)).replaceAll("'", "''");
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)).replaceAll("'", "''");
                    String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));

                    if (Pattern.matches(".*\\.mp3", name)) {
                        name = name.substring(0, name.length() - 4);
                    }

                    SongInfo s = new SongInfo(name, artist, url, dateAdded);
                    songs.add(s);
                }while (cursor.moveToNext());
            }
            cursor.close();

            songManager.initDB(this);
            songManager.readSongs();
            songManager.checkSongsForChanges(songs);
            songManager.readTaggs();

            songs = songManager.getCurrSongs();
            Collections.sort(songs);

            songAdapter = new SongAdapter(this, songs);
            recyclerView.setAdapter(songAdapter);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.songMenu) {
            item.setChecked(true);
            Toast.makeText(this, "Songs", Toast.LENGTH_LONG).show();
        }

        else if (id == R.id.taggMenu) {
            item.setChecked(true);
            Toast.makeText(this, "Taggs", Toast.LENGTH_LONG).show();

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
}
