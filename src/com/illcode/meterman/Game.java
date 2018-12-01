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

    /**
     * Generate the world state to be used at the start of the game.
     * <p/>
     * The player character's current room must be set appropriately.
     */
    WorldState getInitialWorldState();

    /**
     * Called when the GameManager has set up the world model, either by calling
     * {@link #getInitialWorldState()} or by restoring a saved {@code WorldState},
     * and is ready to being the game.
     * <p/>
     * A game should, in this method, allocate any resources (images, sound) it
     * wants immediately or game-globally available.
     *
     * @param newGame true if this is a new game; false if the game has been loaded
     */
    void start(boolean newGame);

    /**
     * Free any allocated resources, other than images and sounds, which will be freed
     * automatically. Called when the GameManager is unloading the
     * current game instance.
     */
    void dispose();
}
