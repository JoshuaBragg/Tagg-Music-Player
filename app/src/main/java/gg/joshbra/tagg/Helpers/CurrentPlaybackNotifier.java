package gg.joshbra.tagg.Helpers;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Responsible for updating all aspects of the App that display information about the current media playback
 */
public class CurrentPlaybackNotifier extends Observable {
    private final static CurrentPlaybackNotifier self = new CurrentPlaybackNotifier();

    private CurrentPlaybackNotifier() {
        observers = new ArrayList<>();
    }

    public static CurrentPlaybackNotifier getSelf() {
        return self;
    }

    //////////////////////////////////////////////////////////////////////

    private ArrayList<Observer> observers;

    public void attach(Observer observer) {
        if (observers.contains(observer)) { return; }
        observers.add(observer);
    }

    public void detach(Observer observer) {
        if (!observers.contains(observer)) { return; }
        observers.remove(observer);
    }

    public void notifyPlaybackStateChanged(PlaybackStateCompat state) {
        for (Observer o : observers) {
            o.update(this, state);
        }
    }

    public void notifyMetadataChanged(MediaMetadataCompat metadata) {
        for (Observer o : observers) {
            o.update(this, metadata);
        }
    }
}
