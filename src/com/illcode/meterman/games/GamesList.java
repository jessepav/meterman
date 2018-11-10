package com.illcode.meterman.games;

import com.illcode.meterman.Game;
import com.illcode.meterman.games.riverboat.RiverboatGame;

/**
 * A class containing the names of all the games packaged with Meterman,
 * and {@link #getGame a method} to retrieve a {@link Game} instance based on a given name.
 */
public class GamesList
{
    private static final String RIVERBOAT_NAME = "Riverboat";

    public static final String[] games = {RIVERBOAT_NAME};

    public Game getGame(String gameName) {
        switch (gameName) {
        case RIVERBOAT_NAME:
            return new RiverboatGame();
        default:
            return null;
        }
    }
}
