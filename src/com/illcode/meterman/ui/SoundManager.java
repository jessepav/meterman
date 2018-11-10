package com.illcode.meterman.ui;

import java.io.File;

public interface SoundManager
{
    void init();

    void dispose();

    void clearAudio();

    void setGlobalVolume(double volume);

    void loadMusic(String name, File f);

    void playMusic(String name, boolean loop);

    void stopMusic(String name);

    void pauseAllMusic();

    void resumeAllMusic();

    void unloadMusic(String name);

    void loadSound(String name, File f);

    void playSound(String name);

    void unloadSound(String name);

    boolean finishedProcessing();
}
