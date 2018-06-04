package com.bragg.tagg;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MediaController {
    private SongAdapter songAdapter;
    private ArrayList<SongInfo> songs;
    private boolean isPlaying;
    private SerMediaPlayer mediaPlayer;
    private AppCompatActivity activity;
    private SongInfo currSong;
    private SeekBarController seekBarController;

    public MediaController(MainActivity m) {
        activity = m;
        songs = new ArrayList<>();
        songAdapter = new SongAdapter(m, this, songs);
        isPlaying = false;
        seekBarController = new SeekBarController(this, m);
    }

    public MediaController() {}

    public void dupMediaController(AppCompatActivity m, ArrayList<SongInfo> songs, SerMediaPlayer mediaPlayer, SongInfo currSong) {
        activity = m;
        this.songs = songs;
        songAdapter = new SongAdapter(m, this, songs);
        seekBarController = new SeekBarController(this, m);
        if (mediaPlayer != null) {
            isPlaying = true;
            this.mediaPlayer = mediaPlayer;
            playSong(currSong);
        } else {
            isPlaying = false;
        }
    }

    protected void playSong(SongInfo songInfo) {
        try {
            if (isPlaying) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new SerMediaPlayer();
            setOnCompletion();

            mediaPlayer.setDataSource(songInfo.getSongUrl());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    seekBarController.setProgress(0);
                    seekBarController.setMax(mediaPlayer.getDuration());
                }
            });
            isPlaying = true;

            Button btn = activity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_pause_white_18);

            TextView sN = activity.findViewById(R.id.songNameBotTextView);
            TextView aN = activity.findViewById(R.id.artistNameBotTextView);

            sN.setText(songInfo.getSongName());
            aN.setText(songInfo.getArtistName());

            currSong = songInfo;
            seekBarController.startThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int ms) {
        mediaPlayer.seekTo(ms);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    private void setOnCompletion() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (songs.indexOf(currSong) != songs.size() - 1) {
                    playSong(songs.get(songs.indexOf(currSong) + 1));
                } else {
                    playSong(songs.get(0));
                }
            }
        });
    }

    protected void playSong() {
        if (mediaPlayer != null) {
            Button btn = activity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_pause_white_18);
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    protected void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            Button btn = activity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_play_arrow_white_18);
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public SongAdapter getSongAdapter() {
        return songAdapter;
    }

    public ArrayList<SongInfo> getSongs() {
        return songs;
    }

    public SerMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public SongInfo getCurrSong() {
        return currSong;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean songLoaded() {
        return mediaPlayer != null;
    }

    void loadSongs() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        ContentResolver contentResolver = activity.getContentResolver();
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
            songAdapter = new SongAdapter(activity, this, songs);
        }
    }
}
