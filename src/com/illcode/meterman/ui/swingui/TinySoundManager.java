package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Utils;
import com.illcode.meterman.ui.SoundManager;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

public final class TinySoundManager implements SoundManager
{
    private boolean initialized;
    private SoundThreadRunner soundThreadRunner;
    private Thread soundThread;
    private BlockingQueue<SoundMessage> queue;   // shared with soundThreadRunner

    private final SoundMessage SHUTDOWN_MESSAGE;
    private AtomicInteger pendingLoads;   // keeps track of pending music loads

    private boolean musicEnabled, soundEnabled;

    public TinySoundManager() {
        queue = new ArrayBlockingQueue<>(10);
        pendingLoads = new AtomicInteger(0);
        SHUTDOWN_MESSAGE = new SoundMessage(SoundMessage.SHUTDOWN);
        initialized = false;
    }

    public void init() {
        if (!initialized) {
            soundThreadRunner = new SoundThreadRunner();
            soundThread = new Thread(soundThreadRunner);
            logger.info("SoundManager spinning up soundThread");
            soundThread.start();
        }
        initialized = true;
    }

    public void dispose() {
        if (initialized) {
            try {
                queue.put(SHUTDOWN_MESSAGE);
                soundThread.join();
                logger.info("SoundManager joined soundThread");
                soundThread = null;
                soundThreadRunner = null;
                queue.clear();
                pendingLoads.set(0);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "SoundManager", e);
            }
        }
        initialized = false;
    }

    public void clearAudio() {
        try {
            queue.put(new SoundMessage(SoundMessage.CLEAR_AUDIO));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void setGlobalVolume(double volume) {
        try {
            queue.put(new SoundMessage(SoundMessage.SET_VOLUME, Double.toString(volume)));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void loadMusic(String name, File f) {
        try {
            pendingLoads.incrementAndGet();
            queue.put(new SoundMessage(SoundMessage.LOAD_MUSIC, name, f));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void playMusic(String name, boolean loop, double volume) {
        if (!musicEnabled)
            return;
        try {
            queue.put(new SoundMessage(SoundMessage.PLAY_MUSIC, name, loop, volume));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void stopMusic(String name) {
        try {
            queue.put(new SoundMessage(SoundMessage.STOP_MUSIC, name));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void pauseAllMusic() {
        try {
            queue.put(new SoundMessage(SoundMessage.PAUSE_ALL_MUSIC));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void resumeAllMusic() {
        try {
            queue.put(new SoundMessage(SoundMessage.RESUME_ALL_MUSIC));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void unloadMusic(String name) {
        try {
            queue.put(new SoundMessage(SoundMessage.UNLOAD_MUSIC, name));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void loadSound(String name, File f) {
        try {
            queue.put(new SoundMessage(SoundMessage.LOAD_SOUND, name, f));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public void playSound(String name, double volume) {
        if (!soundEnabled)
            return;
        if (pendingLoads.get() != 0)   // sound won't play quickly, so don't play it at all
            return;
        try {
            queue.put(new SoundMessage(SoundMessage.PLAY_SOUND, name, volume));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }


    public void unloadSound(String name) {
        try {
            queue.put(new SoundMessage(SoundMessage.UNLOAD_SOUND, name));
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "SoundManager", e);
        }
    }

    public boolean finishedProcessing() {
        return queue.isEmpty() && pendingLoads.get() == 0;
    }

    private class SoundThreadRunner implements Runnable
    {
        private Map<String,Music> musicMap;
        private Map<String,Sound> soundMap;
        private List<Music> pausedMusic;

        private SoundThreadRunner() {
            musicMap = new HashMap<>(10);
            soundMap = new HashMap<>(20);
            pausedMusic = new ArrayList<>();
        }

        public void run() {
            TinySound.init();
            boolean quit = false;
            while (!quit) {
                SoundMessage msg;
                Sound s;
                Music m;
                try {
                    msg = queue.take();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "SoundThreadRunner", e);
                    continue;
                }
                switch (msg.command) {
                case SoundMessage.SHUTDOWN:
                    quit = true;
                    break;
                case SoundMessage.CLEAR_AUDIO:
                    clearAudio();
                    break;
                case SoundMessage.SET_VOLUME:
                    // we overload the name field to be a string representation of a double
                    double vol = Utils.parseDouble(msg.name, -1.0);
                    if (vol != -1.0)
                        TinySound.setGlobalVolume(vol);
                    break;
                case SoundMessage.LOAD_SOUND:
                    if (!soundMap.containsKey(msg.name)) {
                        s = TinySound.loadSound(msg.file);
                        if (s != null)
                            soundMap.put(msg.name, s);
                    }
                    break;
                case SoundMessage.UNLOAD_SOUND:
                    s = soundMap.remove(msg.name);
                    if (s != null) {
                        s.stop();
                        s.unload();
                    }
                    break;
                case SoundMessage.PLAY_SOUND:
                    s = soundMap.get(msg.name);
                    if (s != null)
                        s.play(msg.val);
                    break;
                case SoundMessage.LOAD_MUSIC:
                    if (!musicMap.containsKey(msg.name)) {
                        m = TinySound.loadMusic(msg.file);
                        if (m != null)
                            musicMap.put(msg.name, m);
                    }
                    pendingLoads.decrementAndGet();
                    break;
                case SoundMessage.PAUSE_ALL_MUSIC:
                    pausedMusic.clear();
                    for (Music music : musicMap.values()) {
                        if (music.playing()) {
                            music.pause();
                            pausedMusic.add(music);
                        }
                    }
                    break;
                case SoundMessage.RESUME_ALL_MUSIC:
                    if (!pausedMusic.isEmpty()) {
                        for (Music music : pausedMusic)
                            music.resume();
                        pausedMusic.clear();
                    }
                case SoundMessage.UNLOAD_MUSIC:
                    m = musicMap.remove(msg.name);
                    if (m != null) {
                        m.stop();
                        m.unload();
                        pausedMusic.remove(m);  // it _might_ be in there
                    }
                    break;
                case SoundMessage.PLAY_MUSIC:
                    m = musicMap.get(msg.name);
                    if (m != null)
                        m.play(msg.flag, msg.val);
                    break;
                case SoundMessage.STOP_MUSIC:
                    m = musicMap.get(msg.name);
                    if (m != null)
                        m.stop();
                    break;
                }
            } // end main thread loop
            clearAudio();
            TinySound.shutdown();
        }

        private void clearAudio() {
            pausedMusic.clear();
            for (Music m : musicMap.values()) {
                m.stop();
                m.unload();
            }
            musicMap.clear();
            for (Sound s : soundMap.values()) {
                s.stop();
                s.unload();
            }
            soundMap.clear();
            TinySound.setGlobalVolume(1.0);
        }
    }

    private class SoundMessage
    {
        private static final int LOAD_SOUND = 1;
        private static final int UNLOAD_SOUND = 2;
        private static final int PLAY_SOUND = 3;
        private static final int LOAD_MUSIC = 4;
        private static final int UNLOAD_MUSIC = 5;
        private static final int PLAY_MUSIC = 6;
        private static final int STOP_MUSIC = 7;

        private static final int SET_VOLUME = 50;
        private static final int PAUSE_ALL_MUSIC = 51;
        private static final int RESUME_ALL_MUSIC = 52;

        private static final int CLEAR_AUDIO = 100;
        private static final int SHUTDOWN = 101;

        int command;
        String name;
        File file;
        boolean flag;
        double val;

        private SoundMessage(int command) {
            this.command = command;
        }

        private SoundMessage(int command, String name) {
            this.command = command;
            this.name = name;
        }

        private SoundMessage(int command, String name, File file) {
            this.command = command;
            this.name = name;
            this.file = file;
        }

        private SoundMessage(int command, String name, boolean flag) {
            this.command = command;
            this.name = name;
            this.flag = flag;
        }

        private SoundMessage(int command, String name, boolean flag, double val) {
            this.command = command;
            this.name = name;
            this.flag = flag;
            this.val = val;
        }

        private SoundMessage(int command, String name, double val) {
            this.command = command;
            this.name = name;
            this.val = val;
        }
    }
}
