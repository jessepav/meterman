package com.illcode.meterman;

import java.util.List;
import java.util.Map;

/**
 * A class representing a world entity (i.e. all objects except for Rooms and the player)
 */
public interface Entity
{
    /** Initialize the entity to its default state. This is not called when restoring a game. */
    void init();

    /** Returns true if {@code attribute} is set */
    boolean checkAttribute(int attribute);

    /** Clear an attribute */
    void clearAttribute(int attribute);

    /** Set an attribute */
    void setAttribute(int attribute);

    /** Clear all attributes */
    void clearAllAttributes();

    /** Return the name of this entity */
    String getName();

    /** Return the indefinite article to be used when referring to this entity.
     *  A null return value causes the system to make its best guess. */
    String getIndefiniteArticle();

    /**
     * Returns the name of the entity that should be displayed in the room or inventory list. This
     * may include any modifiers, like "(worn)", or "(equipped)".
     */
    String getListName();

    /**
     * Returns the description that should be shown in the entity text area when
     * this Entity is selected.
     */
    String getDescription();

    /**
     * Called on each entity in a room when a look command is issued. While the implementation is free to do
     * anything, generally it will call {@link GameManager#queueLookText(String, boolean)} to add text to
     * the room description printed.
     */
    void lookInRoom();

    /**
     * Called when the entity enters scope. This can occur when:
     * <ol>
     *     <li>The player moves into the room where this entity resides</li>
     *     <li>The entity is added to the current room</li>
     *     <li>The entity is added to the player inventory from somewhere outside the current room</li>
     * </ol>
     */
    void enterScope();

    /**
     * Called when the entity is exiting scope. This can occur when:
     * <ol>
     *     <li>The player is exiting the room where the entity resides</li>
     *     <li>The entity is being moved from the current room to somewhere other than
     *         the player's inventory</li>
     *     <li>The entity is being moved from the player's inventory to somewhere other than
     *         the current room</li>
     * </ol>
     * Note that this method is called before any of the above actions take place, so it
     * still has a valid place in the world graph.
     */
    void exitingScope();

    /** Called when the entity is moved to the player inventory */
    void taken();

    /** Called when the entity is removed from the player inventory */
    void dropped();

    /**
     * Called when the entity is selected in the UI.
     * <p/>
     * Note that it is called <em>after</em> the UI is updated as normal for the entity, so this method can
     * modify it if desired.
     * @return true if selection listeners should be skipped, false to continue processing.
     */
    boolean selected();

    /**
     * Returns the room  where this entity is found. If the entity is held in player inventory,
     * this returns the current player room.
     * @return the room, or null if not in a room or inventory
     */
    Room getRoom();

    /**
     * Sets the room where this entity resides, or null if it doesn't reside anywhere.
     * @param room
     */
    void setRoom(Room room);

    /**
     * Returns a list of extra actions to be shown in the UI. Never returns null.
     */
    List<String> getActions();

    /**
     * The player invoked an action on this entity from the UI
     * @param action action name
     * @return true if the entity processed the action itself, false to continue
     *              through the processing chain
     */
    boolean processAction(String action);

    /**
     * Normally the GameManager prints a parser-like message when an action is performed, like
     * {@code "> TAKE CLOAK"}. An entity can request that this parser message replaced with its
     * own message, or suppressed entirely.
     * @param action the action that is being performed
     * @return null to allow the normal parser message flow to continue, <tt>""</tt> to suppress the
     *         parser message entirely, or a non-empty string to replace the default parser message.
     */
    String replaceParserMessage(String action);

    /**
     * Returns a modifiable Map that can be used to store arbitrary data useful for custom processing.
     */
    Map<String,Object> getProperties();
}
