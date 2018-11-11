package com.illcode.meterman;

import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.ui.UIConstants;

import java.util.List;

public interface Room
{
    /** Returns the ID of this room, as used in the class-mapper */
    String getId();

    /** Returns true if {@code attribute} is set */
    boolean checkAttribute(int attribute);

    /** Clear an attribute */
    void clearAttribute(int attribute);

    /** Set an attribute */
    void setAttribute(int attribute);

    /** Clear all attributes */
    void clearAllAttributes();

    /** Returns the full name of the room */
    String getName();

    /** Returns a potentially shorter version of the name, to be used in Exit buttons */
    String getExitName();

    /** Returns the exit-name of the room if it is unvisited */
    String getUnvisitedExitName();

    /** Returns the text to be displayed when the player enters the room or clicks "Look" */
    String getDescription();

    /**
     * Return the exit room associated with a given position
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @return the room that is found when exiting this room in the given direction, or null if no
     *         room is in that direction
     */
    Room getExit(int direction);

   /**
     * Called when the player has entered the room.
     */
    void roomEntered();

    /**
     * Called when the player attempts an exit in the given direction. This method is useful
     * for blocking a player's exit (for reasons of a locked door, being stuck in a web, etc.)
     * without having to register a {@link PlayerMovementListener}.
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     *                  indicating the direction the player is attempting to move
     * @return true if the player should be allowed to exit; false if not
     */
    boolean attemptExit(int direction);

    /**
     * Returns a list of the entities found in this room.
     * <p/>
     * Note: this list must be mutable, as it may be used directly in modifying the room's contents.
     */
    List<Entity> getRoomEntities();
}
