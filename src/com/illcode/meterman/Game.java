package com.illcode.meterman;

/**
 * An instance of a particular game.
 */
public interface Game
{
    /** The name of the game, as displayed in the "New Game" UI */
    String getGameName();

    /** Called when the user selects "About..." in the UI  */
    void about();

    /** The world state at the start of the game */
    WorldState getInitialWorldState();

    /**
     * Called when the GameManager has set up the world model, either by calling
     * {@link #getInitialWorldState()} or by restoring a saved {@code WorldState},
     * and is ready to being the game.
     * @param newGame true if this is a new game; false if the game has been loaded
     */
    void start(boolean newGame);
}
