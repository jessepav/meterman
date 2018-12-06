package com.illcode.meterman;

/**
 * The interface used in the discovery of <tt>Game</tt>S which will be integrated into
 * the Meterman system and shown in the "New Game" list.
 */
public interface MetermanGameGlue {
    /**
     * Return the name of this game as it should be shown in the "New Game" list.
     * @return game name
     */
    String getName();

    /**
     * Return a description of the game, to be shown in the "New Game" dialog.
     * <p/>
     * The description should be exactly 40 characters wide by 10 lines tall.
     * @return game description
     */
    String getDescription();

    /**
     * Get the path of game assets relative to the Meterman assets directory.
     * This will generally just be the name of a ZIP file.
     * @return path of game assets
     */
    String getAssetsPath();

    /**
     * Instantiate and return an instance of the {@link Game}.
     * @return Game instance.
     */
    Game createGame();
}
