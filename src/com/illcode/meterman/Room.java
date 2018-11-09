package com.illcode.meterman;

import com.illcode.meterman.ui.UIConstants;

import java.util.List;

public interface Room
{
    /** Returns the ID of this room, as used in the class-mapper */
    String getId();

    /** Return true if the room has been visited already */
    void isVisited();

    /** Sets the visited state of the room */
    void setVisited(boolean visited);

    /**
     * Return the exit room associated with a given position
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @return the room that is found when exiting this room in the given direction, or null if no
     *         room is in that direction
     */
    Room getExit(int direction);

    /**
     * Returns a list of the entities found in this room.
     */
    List<Entity> getRoomEntities();

    /**
     * Called when the player has entered the room.
     */
    void roomEntered();

    /**
     * Called as the player is exiting the room (but is still there)
     */
    void roomExiting();
}
