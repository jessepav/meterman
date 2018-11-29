package com.illcode.meterman.ui;

import java.io.File;

public interface SoundManager
{
    /**
     * Initialize the sound manager, starting any threads it may use.
     */
    void init();

    /**
     * Dispose of resources allocated during initialization, and stop any extra threads.
     */
    void dispose();

    /**
     * Unload all sound and music.
     */
    void clearAudio();

    /**
     * Set a global volume multiplier.
     * @param volume global volume multiplier (nominal is 1.0)
     */
    void setGlobalVolume(double volume);

    /**
     * Enables or disables the playing of music
     * @param enabled true if music should be enabled
     */
    void setMusicEnabled(boolean enabled);

    /** Returns true if the playing of music is enabled.*/
    boolean isMusicEnabled();

    /**
     * Loads music from a file
     * @param name the name by which the music will be referred by this SoundManager
     * @param f the File to load music from
     */
    void loadMusic(String name, File f);

    /**
     * Plays music previously loaded.
     * @param name name of the Music, as specified in {@link #loadMusic}
     * @param volume the relative volume at which to play the music (1.0 is nominal)
     */
    void playMusic(String name, boolean loop, double volume);

    /**
     * Stops playback for music previously loaded.
     * @param name name of the Music, as specified in {@link #loadMusic}
     */
    void stopMusic(String name);

    /** Pauses all music currently playing. */
    void pauseAllMusic();

    /** Resumes playing all music that was previously paused by a call to {@link #pauseAllMusic()}. */
    void resumeAllMusic();

    /**
     * Enables or disables the playing of sounds
     * @param enabled true if sounds should be enabled
     */
    void setSoundEnabled(boolean enabled);

    /** Returns true if the playing of sounds is enabled.*/
    boolean isSoundEnabled();

    /**
     * Unloads Music previously loaded by this SoundManager
     * @param name the name under which the audio was loaded
     */
    void unloadMusic(String name);

    /**
     * Loads a Sound from a file
     * @param name the name by which the sound will be referred by this SoundManager
     * @param f the File to load sound from
     */
    void loadSound(String name, File f);

    /**
     * Plays a sound previously loaded.
     * <p/>
     * Note that if any music loads are pending, the sound won't be played, because
     * it may not play quickly and so many appear desynchronized with the visuals.
     * @param name name of the sound, as specified in {@link #loadSound}
     * @param volume the relative volume at which to play the sound (1.0 is nominal)
     */
    void playSound(String name, double volume);

    /**
     * Unloads a Sound previously loaded by this SoundManager
     * @param name the name under which the audio was loaded
     */
    void unloadSound(String name);

    /**
     * Returns true if all pending load and play commands have been
     * processed by the soundThread.
     */
    boolean finishedProcessing();
}
