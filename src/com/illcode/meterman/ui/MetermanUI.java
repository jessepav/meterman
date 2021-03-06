package com.illcode.meterman.ui;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;

import java.nio.file.Path;
import java.util.List;

/**
 * An interface describing the functionality present in all UI implementations.
 * <p/>
 * Currently there is only a Swing interface (SwingUI).
 */
public interface MetermanUI
{
    /**
     * Initializes the UI.
     */
    void init();

    /**
     * Hides the interface and disposes of any resources used by the UI.
     */
    void dispose();

    /**
     * Run the UI event loop, if applicable.
     * @return true if the main thread should call {@link Meterman#shutdown()} after run()
     *      returns; false if the UI will call shutdown() itself when the UI is closed (as in
     *      the case for Swing/AWT interfaces, where event dispatch is performed on a separate
     *      non-daemon thread).
     */
    boolean run();

    /**
     * Sets the frame title (or equivalent) and About menu text to show the game name.
     * @param name game name, or null if no game loaded
     */
    void setGameName(String name);

    /**
     * Opens a web browser to the given URL, if applicable. If the UI doesn't
     * support this, or the URL is malformed, nothing will happen.
     * @param url URL to open
     */
    void openURL(String url);

    /**
     * Load an image into the UI. JPEG and PNG (with bitmask transparency) are supported.
     * @param name name by which the image will be referred to in the {@code setXXXImage()} methods.
     * @param p path of the image file.
     */
    void loadImage(String name, Path p);

    /**
     * Unload an image from the UI.
     * @param name name of image
     */
    void unloadImage(String name);

    /** Unload all images from the UI. */
    void unloadAllImages();

    /**
     * Sets the image displayed in the main UI frame. The recommended size for
     * frame images is 150x400 pixels, or an integer fraction of that, in which
     * case the image will be scaled up.
     * @param imageName name of the image, as chosen in {@link #loadImage(String, Path)}
     */
    void setFrameImage(String imageName);

    /**
     * Get the name of the current frame image. This can be useful if a game object
     * wants to save the current image name so that it can restore it later.
     * @return the name of the current frame image.
     */
    String getFrameImage();

    /**
     * Sets the entity image that will be drawn inset in the frame image. The recommended size for entity
     * images is 140x140 pixels, or an integer fraction of that, in which case the image will be scaled up;
     * the image itself should have a border to visually separate it from the frame image.
     * @param imageName name of the image, as chosen in {@link #loadImage(String, Path)}
     */
    void setEntityImage(String imageName);

    /**
     * Get the name of the current entity image. This can be useful if a game object
     * wants to save the current image name so that it can restore it later.
     * @return the name of the current entity image.
     */
    String getEntityImage();

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
     * Appens a newline to the main text area.
     */
    void appendNewline();

    /**
     * Appends text to the main text area, followed by a newline.
     * @param text text to append
     */
    void appendTextLn(String text);

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
     * Refresh the list item corresponding to an entity in the current room.
     * @param e entity to refresh
     */
    void refreshRoomEntity(Entity e);

    /**
     * Clears the list displaying Entities in the player's inventory.
     */
    void clearInventoryEntities();

    /**
     * Adds an entity to the list of entities in the player's inventory.
     * @param e entity to add
     * @param modifiers string to append to the list display for the entity, to indicate status like worn or
     *                  equipped. Will be <tt>null</tt> if there are no modifiers.
     */
    void addInventoryEntity(Entity e, String modifiers);

    /**
     * Removes an entity from the list of entities in the player's inventory.
     * @param e entity to remove
     */
    void removeInventoryEntity(Entity e);

    /**
     * Refresh the list item corresponding to an entity in inventory.
     * @param e entity to refresh
     * @param modifiers string to append to the list display for the entity, to indicate status like worn or
     *                  equipped. Will be <tt>null</tt> if there are no modifiers.
     */
    void refreshInventoryEntity(Entity e, String modifiers);

    /**
     * Cause a given entity to be selected in the UI, if it is present in the room
     * or inventory lists.
     * @param e entity to select
     */
    void selectEntity(Entity e);

    /**
     * Clears any selection in the room and inventory entity lists.
     */
    void clearEntitySelection();

    /**
     * Clears the exit button list.
     */
    void clearExits();

    /**
     * Sets a given exit button to a Room, or hides it.
     * @param buttonPos one of the constants indicating a button position (ex. {@link UIConstants#N_BUTTON})
     * @param label label to use for the specified exit button; if null, the given button will be hidden.
     */
    void setExitLabel(int buttonPos, String label);

    /**
     * Clears the action button group.
     */
    void clearActions();

    /**
     * Add an action to the action button list. If an action with the same label is already shown,
     * this method will return without any effect.
     * @param actionLabel action to add
     */
    void addAction(String actionLabel);

    /**
     * Removes an action from the action button list.
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
     * @param initialText the text initially set in the text field
     * @return the text entered by the user
     */
    String showPromptDialog(String header, String text, String prompt, String initialText);

    /**
     * Shows a dialog allowing the user to select one of a list of items.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param items items from which the user can select one
     * @param showCancelButton if true, a cancel button will be shown; if clicked,
     *          this method will return null
     * @return the item selected, or null if no item selected.
     */
    <T> T showListDialog(String header, String text, List<T> items, boolean showCancelButton);

    /**
     * Shows a dialog displaying an image.
     * @param header header surmounted above the image
     * @param imageName name of the image, as chosen in {@link #loadImage(String, Path)}
     * @param scale the factor (>= 1) by which the image will be scaled before being shown
     * @param text text passage (line-breaks kept intact) shown below the image
     * @param buttonLabel label of the button used to dismiss dialog
     */
    void showImageDialog(String header, String imageName, int scale, String text, String buttonLabel);

    /**
     * Displays an always-on-top dialog with a message, that stays visible until {@link #hideWaitDialog() hidden}. It is
     * intended to inform the user when a potentially long-running operation is taking place.
     * @param message message to show
     */
    void showWaitDialog(String message);

    /**
     * Hides the dialog previously shown by {@link #showWaitDialog(String)}.
     */
    void hideWaitDialog();
}
