package com.bragg.tagg;

import java.util.Observable;

public interface Obs {
    public void update(Observable observable, Object data);
}
