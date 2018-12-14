package com.illcode.meterman;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

/**
 * A class containing the names of all the games packaged with Meterman,
 * and {@link #getGame a method} to retrieve a {@link Game} instance based on a given name.
 */
public class GamesList
{
    private static List<String> gameNames;
    private static Map<String,PieceOfGlue> gamesMap;

    public static Game getGame(String gameName) {
        if (gamesMap == null)
            loadGamesMap();
        return gamesMap.get(gameName).createGame();
    }

    public static List<String> getGameNames() {
        if (gameNames == null) {
            if (gamesMap == null)
                loadGamesMap();
            gameNames = new ArrayList<>(gamesMap.keySet());
        }
        return gameNames;
    }

    /**
     * Return a description of a game.
     * @param gameName game name
     * @return game description
     */
    public static String getGameDescription(String gameName) {
        if (gamesMap == null)
            loadGamesMap();
        return gamesMap.get(gameName).description;
    }

    /**
     * Return the string assets path of a game.
     * @param gameName game name
     * @return string assets path
     */
    public static String getGameAssetsPath(String gameName) {
        if (gamesMap == null)
            loadGamesMap();
        return gamesMap.get(gameName).assetsPath;
    }

    private static void loadGamesMap() {
        gamesMap = new HashMap<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Meterman.gluePath)) {
            for (Path p : dirStream) {
                String filename = p.getFileName().toString().toLowerCase();
                if (filename.endsWith(".glue")) {
                    TextBundle b = TextBundle.loadBundle(p);
                    PieceOfGlue glue = new PieceOfGlue();
                    glue.name = b.getPassage("name");
                    glue.description = b.getPassage("description");
                    glue.assetsPath = b.getPassage("assets-path");
                    glue.gameClassName = b.getPassage("game-class");
                    gamesMap.put(glue.name, glue);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "GamesList.createGame()", e);
        }
    }

    private static class PieceOfGlue implements MetermanGameGlue {
        String name;
        String description;
        String assetsPath;
        String gameClassName;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getAssetsPath() {
            return assetsPath;
        }

        public Game createGame() {
            try {
                Class<?> gameClass = Class.forName(gameClassName);
                return (Game) gameClass.newInstance();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "GamesList.createGame()", ex);
                return null;
            }
        }
    }
}
