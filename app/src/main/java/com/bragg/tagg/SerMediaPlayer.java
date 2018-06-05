package com.bragg.tagg;

import android.media.MediaPlayer;

import java.io.Serializable;

public class SerMediaPlayer extends MediaPlayer implements Serializable {
    private static SerMediaPlayer self;

    private SerMediaPlayer() {
        super();
    }

    public static SerMediaPlayer getSelf() {
        if (self == null) {
            self = new SerMediaPlayer();
        }
        return self;
    }

    public static void resetNull() {
        self = new SerMediaPlayer();
    }
}
