package com.illcode.meterman;

import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.ui.UIConstants;

import java.util.List;
import java.util.Map;

public interface Room
{
    /** Initialize the room to its default state. This is not called when restoring a game. */
    void init();

    /** Returns true if {@code attribute} is set. */
    boolean checkAttribute(int attribute);

    /** Clear an attribute. */
    void clearAttribute(int attribute);

    /** Set an attribute. */
    void setAttribute(int attribute);

    /** Set an attribute to a given value. */
    void setAttribute(int attribute, boolean val);

    /** Clear all attributes. */
    void clearAllAttributes();

    /** Returns the full name of the room. */
    String getName();

    /** Returns a potentially shorter version of the name, to be used in Exit buttons.
     *  This method may return a different name depending on whether the room has been visited. */
    String getExitName();

    /** Returns the text to be displayed when the player enters the room or clicks "Look". */
    String getDescription();

    /**
     * Return the room associated with a given exit direction. This method is used in the world model
     * to get the actual room to which an exit leads.
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @return the room that is found when exiting this room in the given direction, or null if no
     *         exit is possible in that direction.
     * @see #getExitLabel(int)
     */
    Room getExit(int direction);

    /**
     * Return the text that should be shown on the UI button for a given direction.
     * @param direction one of the button constants in {@link UIConstants}
     * @return the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     * @see #getExit(int)
     */
    String getExitLabel(int direction);

    /**
     * Returns a list of the entities found in this room.
     */
    List<Entity> getRoomEntities();

    /**
     * Called when the player has entered the room.
     * @param fromRoom the room (possibly null) from which the player entered
     */
    void entered(Room fromRoom);

    /**
     * Called as the player is exiting the room (but is still there).
     * @param toRoom the room the player is attempting to move to.
     * @return true to block the player from exiting, false to allow the exit (note that
     *          the exit may fail for other reasons)
     */
    boolean exiting(Room toRoom);

    /**
     * Returns a modifiable Map that can be used to store properties useful for custom processing.
     */
    Map<String,Object> getProperties();
}
