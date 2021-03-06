package com.illcode.meterman;

import com.illcode.meterman.impl.WorldBuilder;

import java.nio.file.Path;

/**
 * An instance of a particular game.
 */
public interface Game
{
    /** The name of the game, as displayed in the "New Game" UI. It must match
     *  the name given in the game's .glue file. */
    String getName();

    /**
     * Called when a game is started or loaded. A game should, in this method, load any resources (images,
     * sound, bundles) it wants immediately or game-globally available.
     * <p/>
     * It should also install any game-specific action translations and set the game bundle.
     *
     * @see Utils#installActionNameTranslations(Path)
     * @see Meterman#setGameBundle(TextBundle)
     */
    void init();

    /** Called when the user selects "About..." in the UI  */
    void about();

    /**
     * Generate the world state to be used at the start of the game. This includes
     * registering any listeners and processors with the GameManager that should
     * be active when play begins.
     * <p/>
     * Remember that the player character's current room must be set appropriately.
     * <hr/>
     * The general order of things in the typical implementation of this method is
     * <ol>
     *     <li>Create all rooms and entities</li>
     *     <li>Connect rooms and add entities</li>
     *     <li>Register managers, listeners, and delegates, passing as parameters
     *         references to the particular objects they need to work with.</li>
     * </ol>
     * @see WorldBuilder
     */
    WorldState getInitialWorldState();

    /**
     * Called when the GameManager has set up the world model, either by calling
     * {@link #getInitialWorldState()} or by restoring a saved {@code WorldState},
     * and is ready to being the game.
     * @param newGame true if this is a new game; false if the game has been loaded
     */
    void start(boolean newGame);

    /**
     * Free any allocated resources, other than images and sounds, which will be freed
     * automatically. Called when the GameManager is unloading the current game instance.
     */
    void dispose();

    /**
     * The UI provides a facility for the implementor to enter debug commands, which will
     * be passed along to the game using this method. These commands can be used to,
     * for instance, jump to a later part of the game, move entities around, etc.
     * @param command debug command
     */
    void debugCommand(String command);
}
