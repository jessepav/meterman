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
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

public final class Meterman
{
    public static Path prefsPath, savesPath;
    static Properties prefs;

    /** The GameManager running the current game */
    public static GameManager gm;

    /** The MetermanUI displaying the current game */
    public static MetermanUI ui;

    /** The SoundManager operating in the current game */
    public static SoundManager sound;

    /** Our persistence implementation */
    public static Persistence persistence;

    // The default system text bundle, and a game-specific bundle that can shadow passages
    // in the default system bundle to customize messages.
    private static TextBundle systemBundle, gameBundle;

    // Stores the system-default action name translations
    static Map<String,String> systemActionNameTranslations;

    public static void main(String[] args) throws IOException {
        prefsPath = Paths.get("config/meterman.properties");
        if (!loadPrefs(prefsPath)) {
            System.err.println("Error loading prefs from " + prefsPath.toString());
            System.exit(1);
        }

        Utils.init();
        Utils.initializeLogging();

        savesPath = Paths.get(Utils.pref("saves-path", "saves"));
        if (Files.notExists(savesPath))
            Files.createDirectories(savesPath);

        systemBundle = TextBundle.loadBundle(Utils.pathForAsset(Utils.pref("system-bundle", "meterman/system-bundle.txt")));
        if (systemBundle == null) {
            logger.severe("Invalid system bundle path in config!");
            return;
        }

        systemActionNameTranslations = Utils.loadActionNameTranslations(
            Utils.pathForAsset("meterman/system-action-translations.json"));
        Utils.resetActionNameTranslations();

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

        sound.setSoundEnabled(Utils.booleanPref("sound", true));
        sound.setMusicEnabled(Utils.booleanPref("music", true));
        gm.setAlwaysLook(Utils.booleanPref("always-look", true));

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
        Utils.dispose();
    }

    /**
     * Return a bundle representing system text and messages. This bundle may have passages shadowed by
     * a game-specific bundle installed to customize messages.
     * @return bundle with system passages
     */
    public static TextBundle getSystemBundle() {
        if (gameBundle != null)
            return gameBundle;
        else
            return systemBundle;
    }

    /**
     * Installs a bundle as a game-specific bundle in front of the default system bundle, in order to
     * customize game messages.
     * @param b game-specific bundle, or null to remove any such bundle. If this bundle does not have the
     * default system bundle in its parent chain, the default system bundle will be placed in its parent
     * chain.
     */
    public static void setGameBundle(TextBundle b) {
        gameBundle = b;
        if (gameBundle != null)
            GameUtils.ensureBundleHasParent(gameBundle, systemBundle);
    }
}
