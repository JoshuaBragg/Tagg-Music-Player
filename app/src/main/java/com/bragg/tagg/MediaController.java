package com.bragg.tagg;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.ArrayList;

public class MediaController {
    private SongAdapter songAdapter;
    private SeekBar seekBar;
    private ArrayList<SongInfo> songs;
    private boolean isPlaying;
    private MediaPlayer mediaPlayer;
    private MainActivity mainActivity;
    private final Thread seekBarThread = new SeekBarThread();

    public MediaController(MainActivity m) {
        mainActivity = m;
        seekBar = m.findViewById(R.id.seekBar);
        songs = new ArrayList<>();
        songAdapter = new SongAdapter(m, this, songs);
        isPlaying = false;
    }

    protected void playSong(SongInfo songInfo) {
        try {
            if (isPlaying) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songInfo.getSongUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    seekBar.setProgress(0);
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            });
            isPlaying = true;
            if (!seekBarThread.isAlive())
                seekBarThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SongAdapter getSongAdapter() {
        return songAdapter;
    }

    void loadSongs() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        ContentResolver contentResolver = mainActivity.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    SongInfo s = new SongInfo(name,artist,url);
                    songs.add(s);

                }while (cursor.moveToNext());
            }

            cursor.close();
            songAdapter = new SongAdapter(mainActivity, this, songs);
        }
    }

    public class SeekBarThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (mediaPlayer != null) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition(), true);
                        } else {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
