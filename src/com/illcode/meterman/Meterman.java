package com.illcode.meterman;

import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.SoundManager;
import com.illcode.meterman.ui.swingui.SwingUI;
import com.illcode.meterman.ui.swingui.TinySoundManager;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

public final class Meterman
{
    static Path prefsPath, savesPath;
    static Properties prefs;

    /** The GameManager running the current game */
    public static GameManager gm;

    /** The MetermanUI displaying the current game */
    public static MetermanUI ui;

    /** The SoundManager operating in the current game */
    public static SoundManager sound;

    /** Our persistence implementation */
    public static Persistence persistence;

    public static void main(String[] args) throws IOException {
        prefsPath = Paths.get("config/meterman.properties");
        if (!loadPrefs(prefsPath)) {
            System.err.println("Error loading prefs from " + prefsPath.toString());
            System.exit(1);
        }

        Utils.initLogging();

        savesPath = Paths.get(Utils.pref("saves-path", "saves"));
        if (Files.notExists(savesPath))
            Files.createDirectories(savesPath);

        gm = new GameManager();
        switch (Utils.pref("ui", "swing")) {
        case "swing":
            ui = new SwingUI();
            sound = new TinySoundManager();
            break;
        default:
            logger.severe("Invalid ui set in config!");
            return;
        }
        switch (Utils.pref("persistence", "kryo")) {
        case "kryo":
            persistence = new KryoPersistence();
            break;
        default:
            logger.severe("Invalid persistence set in config!");
            return;
        }
        gm.init();
        ui.init();
        sound.init();
        persistence.init();

        ui.setVisible(true);
        if (ui.run())
            shutdown();
    }

    private static boolean loadPrefs(Path path) {
        prefs = new Properties();
        if (Files.exists(path)) {
            try (FileReader r = new FileReader(path.toFile())) {
                prefs.load(r);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static void savePrefs(Path path) {
        try (FileWriter w = new FileWriter(path.toFile())) {
            prefs.store(w, "Meterman preferences");
        } catch (IOException ex) {
            logger.log(Level.WARNING, "savePrefs " + path.toString(), ex);
        }
    }

    /** Called when the program is shutting down. */
    public static void shutdown() {
        logger.info("Meterman shutting down...");
        persistence.dispose();
        sound.dispose();
        ui.dispose();
        gm.dispose();
        savePrefs(prefsPath);
    }
}
