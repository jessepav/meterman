package com.illcode.meterman.games;

import com.illcode.meterman.Game;
import com.illcode.meterman.TextBundle;
import com.illcode.meterman.Utils;
import com.illcode.meterman.games.riverboat.RiverboatGame;

/**
 * A class containing the names of all the games packaged with Meterman,
 * and {@link #getGame a method} to retrieve a {@link Game} instance based on a given name.
 */
public class GamesList
{
    private static final String RIVERBOAT_NAME = "The Riverboat";

    public static final String[] games = {RIVERBOAT_NAME};

    private static TextBundle descriptionBundle;

    public static Game getGame(String gameName) {
        switch (gameName) {
        case RIVERBOAT_NAME:
            return new RiverboatGame();
        default:
            return null;
        }
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
            descriptionBundle = TextBundle.loadBundle(Utils.pathForAsset("game-description-bundle.txt"));
        return descriptionBundle.getPassage(gameName);
    }
}
