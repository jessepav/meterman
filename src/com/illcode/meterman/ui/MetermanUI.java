package com.illcode.meterman.ui;

import com.illcode.meterman.Entity;
import com.illcode.meterman.GameManager;
import com.illcode.meterman.Room;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * An interface describing the functionality present in all UI implementations.
 * <p/>
 * Currently there is only a Swing interface (SwingUI).
 */
public interface MetermanUI
{
    /**
     * Initializes the UI, binding it to a given {@link GameManager}.
     * @param manager game manager to bind to the UI
     */
    void init(GameManager manager);

    /**
     * Disposes of any resources used by the UI, and hides the interface.
     */
    void dispose();

    /**
     * Sets the frame title (or equivalent), to show the game name, etc.
     * @param title title string
     */
    void setTitle(String title);

    /**
     * Opens a web browser to the given URL, if applicable. If the UI doesn't
     * support this, or the URL is malformed, nothing will happen.
     * @param url URL to open
     */
    void openURL(String url);

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
     * Sets a given exit button to a Room, or hides it.
     * @param buttonPos one of the constants indicating a button position (ex. {@link UIConstants#N_BUTTON})
     * @param r room to add to the button list; if null, the given button will be hidden. If all buttons
     *          are hidden, the "Exits" label will be hidden as well.
     */
    void setExitButton(int buttonPos, Room r);

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
     * @param labelPosition one of {@link UIConstants#LEFT_LABEL},
     *           {@link UIConstants#CENTER_LABEL}, {@link UIConstants#RIGHT_LABEL}
     * @param label the text to show for the given label
     */
    void setStatusLabel(int labelPosition, String label);

    /**
     * Displays a modal dialog showing a passage of text.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param buttonLabel label of the button to dismiss dialog
     */
    void showTextDialog(String header, String text, String buttonLabel);

    /**
     * Displays a modal dialog showing a passage of text and a field for the user to
     * enter a line of text.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param prompt prompt displayed in front of the field
     * @return the text entered by the user
     */
    String showPromptDialog(String header, String text, String prompt);

    /**
     * Shows a dialog allowing the user to select one of a list of items.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param items items from which the user can select one
     * @return the item selected, or null if no item selected.
     */
    <T> T showListDialog(String header, String text, List<T> items);
}
