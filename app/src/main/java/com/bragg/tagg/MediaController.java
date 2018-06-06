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
        mediaPlayer = SerMediaPlayer.getSelf();
    }

    public MediaController() {}

    public void dupMediaController(AppCompatActivity m, ArrayList<SongInfo> songs, SongInfo currSong, boolean isPlaying) {
        activity = m;
        this.songs = songs;
        songAdapter = new SongAdapter(m, this, songs);
        seekBarController = new SeekBarController(this, m);
        this.isPlaying = isPlaying;
        this.currSong = currSong;
        this.mediaPlayer = SerMediaPlayer.getSelf();
    }

    protected void updateGui() {
        if (currSong == null) {
            return;
        }

        TextView sN = activity.findViewById(R.id.songNameBotTextView);
        TextView aN = activity.findViewById(R.id.artistNameBotTextView);

        sN.setText(currSong.getSongName());
        aN.setText(currSong.getArtistName());

        updateButton();
    }

    protected void updateButton() {
        if (isPlaying) {
            Button btn = activity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_pause_white_18);
        } else {
            Button btn = activity.findViewById(R.id.pausePlayBtn);
            btn.setBackgroundResource(R.drawable.baseline_play_arrow_white_18);
        }
    }

    protected void playSong(SongInfo songInfo) {
        try {
            if (isPlaying) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }

            SerMediaPlayer.resetNull();
            mediaPlayer = SerMediaPlayer.getSelf();
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

            currSong = songInfo;
            seekBarController.startThread();
            updateGui();
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
            mediaPlayer.start();
            isPlaying = true;
            updateGui();
        }
    }

    protected void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            updateGui();
        }
    }

    public SongAdapter getSongAdapter() {
        return songAdapter;
    }

    public ArrayList<SongInfo> getSongs() {
        return songs;
    }

    public SongInfo getCurrSong() {
        return currSong;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean songLoaded() {
        return currSong != null;
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
