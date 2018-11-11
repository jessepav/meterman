package com.illcode.meterman;

import java.util.List;

/**
 * A class representing a world entity (i.e. all objects except for Rooms and the player)
 */
public interface Entity
{
    /** Initialize the entity to its default state. This is not called when restoring a game. */
    void init();

    /** Returns the ID of this entity, as used in the class-mapper */
    String getId();

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

    /**
     * Returns the name of the entity that should be displayed in the room or inventory list. This
     * includes any modifiers, like "(worn)", or "(equipped)".
     */
    String getListName();

    /**
     * Returns the description that should be shown in the entity text area when
     * this Entity is selected.
     */
    String getDescription();

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

    /**
     * Returns the room ID where this entity is found. If the entity is held in player inventory,
     * this returns the current player room ID.
     * @return the room ID, or null if not in a room or inventory
     */
    String getRoomId();

    /**
     * Sets the room where this entity resides, or null if it doesn't reside anywhere.
     * @param id
     */
    void setRoomId(String id);

    /**
     * Returns a list of extra actions to be shown in the UI, in addition to those shown
     * by the engine based on the entity's attributes.
     */
    List<String> getExtraActions();

    /**
     * The player invoked an action on this entity from the UI
     * @param action action name
     */
    void processAction(String action);
}
