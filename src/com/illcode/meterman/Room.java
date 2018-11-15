package com.illcode.meterman;

import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.ui.UIConstants;

import java.util.List;
import java.util.Map;

public interface Room
{
    /** Initialize the room to its default state. This is not called when restoring a game. */
    void init();

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

    /** Returns a potentially shorter version of the name, to be used in Exit buttons.
     *  This method may return a different name depending on whether the room has been visited. */
    String getExitName();

    /** Returns the text to be displayed when the player enters the room or clicks "Look" */
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
     * Sets the exit leading from this room in the given direction.
     * <p/>
     * This method exists in order to enable world-modification helper classes and listeners -- if
     * a particular Room wants to ignore this method, it is free to do so.
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @param room the room found in the given direction, or null if no room
     * @see #getExit(int)
     */
    void setExit(int direction, Room room);

    /**
     * Return the text that should be shown on the UI button for a given direction.
     * @param direction one of the button constants in {@link UIConstants}
     * @return the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     * @see #getExit(int)
     */
    String getExitLabel(int direction);

    /**
     * Sets the exit label displayed in the UI for a given direction.
     * <p/>
     * This method exists in order to enable world-modification helper classes and listeners -- if
     * a particular Room wants to ignore this method, it is free to do so.
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @param label the label that should be displayed in the UI for the given direction, or null
     *              if the button should be hidden
     * @see #getExitLabel(int)
     */
    void setExitLabel(int direction, String label);

    /**
     * Returns a list of the entities found in this room.
     */
    List<Entity> getRoomEntities();

    /**
     * Called when the player has entered the room.
     */
    void entered();

    /**
     * Called as the player is exiting the room (but is still there)
     */
    void exiting();

    /**
     * Returns a modifiable Map that can be used to store properties useful for custom processing.
     */
    Map<String,Object> getProperties();
}
