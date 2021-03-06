package com.illcode.meterman;

import org.apache.commons.lang3.StringUtils;
import com.eclipsesource.json.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class Utils
{
    public static Logger logger;
    private static Random random;
    private static Path assetsPath, systemAssetsPath, gameAssetsPath;
    private static FileSystem systemZipFs, gameZipFs;

    private static Map<String,String> actionNameMap;

    public static void init() {
        actionNameMap = new HashMap<>();
    }

    public static void dispose() {
        closeSystemZipFs();
        closeGameZipFs();
        actionNameMap = null;
    }

    public static void closeSystemZipFs() {
        if (systemZipFs != null) {
            try {
                systemZipFs.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Utils.closeSystemZipFs()", e);
            }
            systemZipFs = null;
        }
    }

    public static void closeGameZipFs() {
        if (gameZipFs != null) {
            try {
                gameZipFs.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Utils.closeGameZipFs()", e);
            }
            gameZipFs = null;
        }
    }

    /**
     * Initializes our static logger, pulling the logs directory from the "log-path" pref.
     * Henceforth, {@link Utils#logger} will be ready for action.
     * @throws IOException
     */
    public static void initializeLogging() throws IOException {
        System.setProperty("java.util.logging.config.file", "config/logging.properties");
        LogManager.getLogManager().readConfiguration();

        Path logPath = Paths.get(pref("log-path", "logs"));
        if (Files.notExists(logPath))
            Files.createDirectories(logPath);
        logger = Logger.getLogger("com.illcode.meterman");
        logger.addHandler(new FileHandler(logPath.toString() + "/meterman-%g.log", 50000, 10, true));

        logger.info("Meterman Logger Initialized. Default Charset: " + Charset.defaultCharset());
    }

    /**
     * Return a potentially translated representation of {@code name} to be used as action names.
     * @param name canonical action name
     * @return version of {@code name} to be shown to the user.
     */
    public static String getActionName(String name) {
        String val = actionNameMap.get(name);
        return val != null ? val : name;
    }

    /**
     * Installs action name translations from a JSON file. The JSON data should be an object,
     * whose names are the canonical action names ("Look", etc.) and the values the translated
     * names. An example:
     * <pre>{@code
     * {
     *    "Look" : "Espy",
     *    "Wait" : "Tarry"
     * }
     * }</pre>
     * @param p path to JSON file
     */
    public static void installActionNameTranslations(Path p) {
        installActionNameTranslations(loadActionNameTranslations(p));
    }

    /** Installs action name translations from a Map.
     *  @see #installActionNameTranslations(Path) */
    public static void installActionNameTranslations(Map<String,String> m) {
        actionNameMap.putAll(m);
    }

    /**
     * Loads and returns an action name translation map from a JSON file.
     * @param p path to JSON file
     * @return translation map
     * @see #installActionNameTranslations(Path)
     */
    public static Map<String,String> loadActionNameTranslations(Path p) {
        Map<String,String> map = new HashMap<>();
        int n = 0;
        try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            JsonValue v = Json.parse(r);
            if (v.isObject()) {
                JsonObject o = v.asObject();
                for (JsonObject.Member m : o) {
                    JsonValue value = m.getValue();
                    if (value.isString()) {
                        map.put(m.getName(), value.asString());
                        n++;
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Utils.loadActionNameTranslations()", e);
        }
        logger.fine(fmt("Loaded %d action name translations from %s", n, p.getFileName().toString()));
        return map;
    }

    /**
     * Remove any game-specific action name translations, and retain only the system translations.
     */
    public static void resetActionNameTranslations() {
        actionNameMap.clear();
        installActionNameTranslations(Meterman.systemActionNameTranslations);
    }

    /**
     * Sets the base assets path against which the system and game assets paths will be resolved.
     * @param assetsPath
     */
    public static void setAssetsPath(Path assetsPath) {
        Utils.assetsPath = assetsPath;
    }

    /** Sets the system assets path. This can be a directory or a ZIP file.
     * @param path path, relative to {@link #setAssetsPath(Path) assetsPath} */
    public static void setSystemAssetsPath(String path) {
        closeSystemZipFs();
        if (path != null) {
            systemAssetsPath = assetsPath.resolve(path);
            if (StringUtils.endsWithIgnoreCase(path, ".zip")) {
                try {
                    systemZipFs = FileSystems.newFileSystem(systemAssetsPath, null);
                    systemAssetsPath = systemZipFs.getPath("/");
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Utils.setSystemAssetsPath()", e);
                    systemAssetsPath = null;
                }
            }
        } else {
            systemAssetsPath = null;
        }
    }

    /** Sets the game assets path. This can be a directory or a ZIP file.
     * @param path path, relative to {@link #setAssetsPath(Path) assetsPath} */
    public static void setGameAssetsPath(String path) {
        closeGameZipFs();
        if (path != null) {
            gameAssetsPath = assetsPath.resolve(path);
            if (StringUtils.endsWithIgnoreCase(path, ".zip")) {
                try {
                    gameZipFs = FileSystems.newFileSystem(gameAssetsPath, null);
                    gameAssetsPath = gameZipFs.getPath("/");
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Utils.setGameAssetsPath()", e);
                    gameAssetsPath = null;
                }
            }
        } else {
            gameAssetsPath = null;
        }
    }

    /**
     * Returns a Path representing the given asset, resolved against the game assets path.
     * @param asset path (relative to the game assets path) of the asset we want
     * @return the resolved path
     */
    public static Path pathForGameAsset(String asset) {
        return gameAssetsPath.resolve(asset);
    }

    /**
     * Returns a Path representing the given asset, resolved against the system assets path.
     * <p/>
     * This method is for the Meterman system classes only: games should call
     * {@link #pathForGameAsset(String)} instead.
     * @param asset (relative to the system assets path) of the asset we want
     * @return the resolved path
     */
    public static Path pathForSystemAsset(String asset) {
        return systemAssetsPath.resolve(asset);
    }

    /**
     * Examines a String to determine if it's a way to say "true"
     * @param s if this is "true", "on", "1", or "yes" then we consider it true
     * @return truth value of {@code s}
     */
    public static boolean parseBoolean(String s) {
        if (s != null && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on") || s.equals("1") || s.equals("yes")))
            return true;
        else
            return false;
    }

    /**
     * Parses an input string as a decimal integer
     * @param s String representation of an integer
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return int value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static int parseInt(String s, int errorVal) {
        int i = errorVal;
        if (s != null && s.length() > 0) {
            try {
                i = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                i = errorVal;
            }
        }
        return i;
    }

    /**
     * Parses an input string as a float
     * @param s String representation of a float
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return float value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static float parseFloat(String s, float errorVal) {
        float f = errorVal;
        if (s != null && s.length() > 0) {
            try {
                f = Float.parseFloat(s);
            } catch (NumberFormatException ex) {
                f = errorVal;
            }
        }
        return f;
    }

    /**
     * Parses an input string as a double
     * @param s String representation of a double
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return double value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static double parseDouble(String s, double errorVal) {
        double f = errorVal;
        if (s != null && s.length() > 0) {
            try {
                f = Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                f = errorVal;
            }
        }
        return f;
    }

    /**
     * Equivalent to {@link #parseInt(String, int) parseInt(s, 0)}
     */
    public static int parseInt(String s) {
        return parseInt(s, 0);
    }

    /**
     * Ensures an input int {@code i} is between {@code [min, max]} (inclusive on both ends).
     */
    public static int clamp(int i, int min, int max) {
        if (i < min) return min;
        else if (i > max) return max;
        else return i;
    }

    /** Equivalent to {@code String.format(s, args)} */
    public static String fmt(String s, Object... args) {
        return String.format(s, args);
    }

    /**
     * Returns the boolean value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static boolean booleanPref(String key, boolean defaultVal) {
        if (Meterman.prefs.containsKey(key)) {
            return parseBoolean(Meterman.prefs.getProperty(key));
        } else {
            Meterman.prefs.setProperty(key, Boolean.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the int value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}. If the value of
     * the preference cannot be parsed as an int, we return {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static int intPref(String key, int defaultVal) {
        if (Meterman.prefs.containsKey(key)) {
            return parseInt(Meterman.prefs.getProperty(key), defaultVal);
        } else {
            Meterman.prefs.setProperty(key, Integer.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the float value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}. If the value of
     * the preference cannot be parsed as a float, we return {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static float floatPref(String key, float defaultVal) {
        if (Meterman.prefs.containsKey(key)) {
            return parseFloat(Meterman.prefs.getProperty(key), defaultVal);
        } else {
            Meterman.prefs.setProperty(key, Float.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the String value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static String pref(String key, String defaultVal) {
        if (Meterman.prefs.containsKey(key)) {
            return Meterman.prefs.getProperty(key);
        } else {
            Meterman.prefs.setProperty(key, defaultVal);
            return defaultVal;
        }
    }

    /** Facade for {@link Properties#getProperty(java.lang.String)}*/
    public static String getPref(String key) {
        return Meterman.prefs.getProperty(key);
    }

    /** Facade for {@link Properties#setProperty(java.lang.String, java.lang.String)}*/
    public static void setPref(String key, String value) {
        Meterman.prefs.setProperty(key, value);
    }

    /** Causes the current thread to sleep for {@code millis} milliseconds, catching InterruptedException
     *  and re-setting the thread interrupted flag. */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns a random int between {@code min} and {@code max}, inclusive.
     */
    public static int randInt(int min, int max) {
        if (random == null)
            random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Read all characters available from a Reader and return them as a string.
     * We use a BufferedReader internally to make the process more efficient.
     * @param r Reader from which to read characters
     * @param bufferSize buffer size for our BufferedReader
     * @return String read from the reader, or null if an exception occurred
     */
    public static String slurpReaderText(Reader r, int bufferSize) {
        String s = null;
        try (BufferedReader reader = new BufferedReader(r)) {
            char [] buffer = new char[bufferSize];
            StringBuilder sb = new StringBuilder(5*bufferSize);
            int n;
            while ((n = reader.read(buffer, 0, buffer.length)) != -1)
                sb.append(buffer, 0, n);
            s = sb.toString();
        } catch (IOException ex) {
            s = null;
        }
        return s;
    }

    /**
     * Loads a ClassLoader text resource and returns its contents as a String, assuming a UTF-8 encoding.
     * @param resourcePath class-loader resource path, relative to {@code Utils.class}
     * @return String value of resource, or an empty string on error
     */
    public static String getStringResource(String resourcePath) {
        String val = null;
        InputStream in = Utils.class.getResourceAsStream(resourcePath);
        if (in != null)
            val = Utils.slurpReaderText(new InputStreamReader(in, StandardCharsets.UTF_8), 512);
        return val != null ? val : "";
    }
}
