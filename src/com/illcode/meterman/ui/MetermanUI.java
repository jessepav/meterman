package com.illcode.meterman.ui;

import com.illcode.meterman.Entity;
import com.illcode.meterman.GameManager;
import com.illcode.meterman.Room;

import java.awt.image.BufferedImage;

/**
 * An interface describing the functionality present in all UI implementations.
 * <p/>
 * Currently there is only a Swing interface (SwingUI).
 */
public interface MetermanUI
{
    /** Constant indicating the left status bar label */
    public static final int LEFT_LABEL = 0;

    /** Constant indicating the center status bar label */
    public static final int CENTER_LABEL = 1;

    /** Constant indicating the right status bar label */
    public static final int RIGHT_LABEL = 2;

    /**
     * Initializes the UI, binding it to a given {@link GameManager}.
     * @param manager game manager to bind to the UI
     */
    void init(GameManager manager);

    /**
     * Sets the image displayed in the main UI frame.
     * @param image image to display
     */
    void setFrameImage(BufferedImage image);

    /**
     * Sets the room name displayed in the UI
     * @param name room name
     */
    void setRoomName(String name);

    /**
     * Clears the main text area.
     */
    void clearText();

    /**
     * Appends text to the main text area.
     * @param text text to append
     */
    void appendText(String text);

    /**
     * Sets the object name displayed above the description text area.
     * @param name object name
     */
    void setObjectName(String name);

    /**
     * Sets the object text displayed in the selected object area.
     * @param text text to display
     */
    void setObjectText(String text);

    /**
     * Clears the list displaying Entities in the current room.
     */
    void clearRoomEntities();

    /**
     * Adds an entity to the list of entities in the current room.
     * @param e entity to add
     */
    void addRoomEntity(Entity e);

    /**
     * Removes an entity from the list of entities in the current room.
     * @param e entity to remove
     */
    void removeRoomEntity(Entity e);
    
    /**
     * Clears the list displaying Entities in the player's inventory.
     */
    void clearInventoryEntities();

    /**
     * Adds an entity to the list of entities in the player's inventory.
     * @param e entity to add
     */
    void addInventoryEntity(Entity e);

    /**
     * Removes an entity from the list of entities in the player's inventory.
     * @param e entity to remove
     */
    void removeInventoryEntity(Entity e);

    /**
     * Clears the exit button list, and hides the "Exits" label.
     */
    void clearExits();

    /**
     * Adds a Room to the exit button list. If the "Exits" label is hidden,
     * this will show it. There are a maximum of 12 exit buttons.
     * @param r room to add to the button list
     */
    void addExit(Room r);

    /**
     * Removes a Room from the exit button list. If no exits remain, the "Exits"
     * label will be hidden.
     * @param r room to remove from the button list
     */
    void removeExit(Room r);

    /**
     * Clears the action button list and hides the "Actions" label.
     */
    void clearActions();

    /**
     * Add an action to the action button list. If the "Actions" label is hidden,
     * it will be displayed. If an action with the same label is already shown,
     * this method will return without any effect.
     * @param actionLabel action to add
     */
    void addAction(String actionLabel);

    /**
     * Removes an action from the action button list. If no actions remain, the
     * "Actions" label will be hidden
     * @param actionLabel action to remove
     */
    void removeAction(String actionLabel);

    /**
     * Sets one of the three status bar labels.
     * @param labelPosition one of {@link #LEFT_LABEL}, {@link #CENTER_LABEL}, {@link #RIGHT_LABEL}
     * @param label the text to show for the given label
     */
    void setStatusLabel(int labelPosition, String label);

    // TODO: methods handling the TextDialog, PromptDialog, and ListDialog
}
