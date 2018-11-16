package com.illcode.meterman.ui;

import java.io.File;

public interface SoundManager
{
    void init();

    void dispose();

    void clearAudio();

    void setGlobalVolume(double volume);

    void setMusicEnabled(boolean enabled);

    boolean isMusicEnabled();

    void loadMusic(String name, File f);

    void playMusic(String name, boolean loop, double volume);

    void stopMusic(String name);

    void pauseAllMusic();

    void resumeAllMusic();

    void setSoundEnabled(boolean enabled);

    boolean isSoundEnabled();

    void unloadMusic(String name);

    void loadSound(String name, File f);

    void playSound(String name, double volume);

    void unloadSound(String name);

    boolean finishedProcessing();
}
