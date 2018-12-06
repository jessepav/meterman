package com.illcode.meterman;

import com.illcode.meterman.Game;
import com.illcode.meterman.TextBundle;
import com.illcode.meterman.Utils;
import com.illcode.meterman.games.cloakofdarkness.CloakGame;
import com.illcode.meterman.games.riverboat.RiverboatGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A class containing the names of all the games packaged with Meterman,
 * and {@link #getGame a method} to retrieve a {@link Game} instance based on a given name.
 */
public class GamesList
{
    private static final String RIVERBOAT_NAME = "The Riverboat";
    private static final String CLOAK_NAME = "Cloak of Darkness";

    private static List<String> gameNames;

    private static TextBundle descriptionBundle;

    public static Game getGame(String gameName) {
        switch (gameName) {
        case RIVERBOAT_NAME:
            return new RiverboatGame();
        case CLOAK_NAME:
            return new CloakGame();
        default:
            return null;
        }
    }

    public static List<String> getGameNames() {
        if (gameNames == null) {
            gameNames = new LinkedList<>();
            // If we dynamically load games from .jar files, we could query and add them here.
            gameNames.add(RIVERBOAT_NAME);
            gameNames.add(CLOAK_NAME);
        }
        return gameNames;
    }

    /**
     * Return a description of a game.
     * <p/>
     * Game descriptions are found in the <tt>game-description-bundle.txt</tt> TextBundle
     * in the assets directory.
     * @param gameName game name, or <tt>"select-game"</tt> to retrieve text prompting the
     *                 user to select a game.
     * @return game description
     */
    public static String getGameDescription(String gameName) {
        if (descriptionBundle == null)
            descriptionBundle = TextBundle.loadBundle(Utils.pathForSystemAsset("game-description-bundle.txt"));
        return descriptionBundle.getPassage(gameName);
    }
}
