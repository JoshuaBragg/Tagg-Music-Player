package com.bragg.tagg;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

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

            Button btn = mainActivity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_pause_circle_outline_white_18);

            TextView sN = mainActivity.findViewById(R.id.songNameBotTextView);
            TextView aN = mainActivity.findViewById(R.id.artistNameBotTextView);

            sN.setText(songInfo.getSongName());
            aN.setText(songInfo.getArtistName());

            if (!seekBarThread.isAlive())
                seekBarThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void playSong() {
        if (mediaPlayer != null) {
            Button btn = mainActivity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_pause_circle_outline_white_18);
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    protected void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            Button btn = mainActivity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_play_circle_outline_white_18);
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public SongAdapter getSongAdapter() {
        return songAdapter;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean songLoaded() {
        return mediaPlayer != null;
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
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    // Only update the mediaPlayer position if the user moved seekBar
                    if (b) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // User started touching seekBar
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // User stopped touching seekBar
                }
            });

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
