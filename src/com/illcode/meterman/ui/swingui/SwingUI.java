package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.UIConstants;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SwingUI implements MetermanUI
{
    MainFrame mainFrame;
    TextDialog textDialog;
    PromptDialog promptDialog;
    ListDialog listDialog;

    private List<Entity> roomEntities, inventoryEntities;

    public void init() {
        GuiUtils.initGraphics();

        roomEntities = new ArrayList<>();
        inventoryEntities = new ArrayList<>();
        mainFrame = new MainFrame(this);
        textDialog = new TextDialog(mainFrame.frame);
        promptDialog = new PromptDialog(mainFrame.frame);
        listDialog = new ListDialog(mainFrame.frame);

        setStatusLabel(UIConstants.LEFT_LABEL, "");
        setStatusLabel(UIConstants.CENTER_LABEL, "");
        setStatusLabel(UIConstants.RIGHT_LABEL, "");
    }

    public void dispose() {
        listDialog.dispose();
        promptDialog.dispose();
        textDialog.dispose();
        mainFrame.dispose();
    }

    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);
    }

    public boolean run() {
        return false;
    }

    public void setTitle(String title) {
        mainFrame.frame.setTitle(title);
    }

    public void openURL(String url) {
        DesktopUtils.browseURI(url);
    }

    public void setFrameImage(BufferedImage image) {
        mainFrame.setFrameImage(image);
    }

    public void setEntityImage(BufferedImage image) {
        mainFrame.setEntityImage(image);
    }

    public void setRoomName(String name) {
        mainFrame.roomNameLabel.setText(name);
    }

    public void clearText() {
        mainFrame.mainTextArea.setText(null);
    }

    public void appendText(String text) {
        mainFrame.mainTextArea.append(text);
    }

    public void setObjectName(String name) {
        mainFrame.objectNameLabel.setText(name);
    }

    public void setObjectText(String text) {
        mainFrame.objectTextArea.setText(text);
    }

    public void clearRoomEntities() {
        roomEntities.clear();
        mainFrame.roomListModel.clear();
    }

    public void addRoomEntity(Entity e) {
        roomEntities.add(e);
        mainFrame.roomListModel.addElement(e.getListName());
    }

    public void removeRoomEntity(Entity e) {
        int idx = roomEntities.indexOf(e);
        if (idx != -1) {
            roomEntities.remove(idx);
            mainFrame.roomListModel.remove(idx);
        }
    }

    public void clearInventoryEntities() {
        inventoryEntities.clear();
        mainFrame.inventoryListModel.clear();
    }

    public void addInventoryEntity(Entity e) {
        inventoryEntities.add(e);
        mainFrame.inventoryListModel.addElement(e.getListName());
    }

    public void removeInventoryEntity(Entity e) {
        int idx = inventoryEntities.indexOf(e);
        if (idx != -1) {
            inventoryEntities.remove(idx);
            mainFrame.inventoryListModel.remove(idx);
        }
    }

    public void clearEntitySelection() {
        mainFrame.roomList.clearSelection();
        mainFrame.inventoryList.clearSelection();
    }

    public void clearExits() {
        mainFrame.clearExits();
    }

    public void setExitLabel(int buttonPos, String label) {
        mainFrame.setExitLabel(buttonPos, label);
    }

    public void clearActions() {
        mainFrame.clearActions();
    }

    public void addAction(String actionLabel) {
        mainFrame.addAction(actionLabel);
    }

    public void removeAction(String actionLabel) {
        mainFrame.removeAction(actionLabel);
    }

    public void setStatusLabel(int labelPosition, String label) {
        switch (labelPosition) {
        case UIConstants.LEFT_LABEL:
            mainFrame.leftStatusLabel.setText(label);
            break;
        case UIConstants.CENTER_LABEL:
            mainFrame.centerStatusLabel.setText(label);
            break;
        case UIConstants.RIGHT_LABEL:
            mainFrame.rightStatusLabel.setText(label);
            break;
        }
    }

    public void showTextDialog(String header, String text, String buttonLabel) {
        textDialog.show(header, text, buttonLabel);
    }

    public String showPromptDialog(String header, String text, String prompt, String initialText) {
        return promptDialog.show(header, text, prompt, initialText);
    }

    public <T> T showListDialog(String header, String text, List<T> items, boolean showCancelButton) {
        return listDialog.showListDialog(header, text, items, showCancelButton);
    }

    /** Called by MainFrame when an entity is selected from the room list. We translate
     *  the index into the actual Entity (or null) and pass that along to the game manager. */
    void roomEntitySelected(int idx) {
        Entity e = null;
        if (idx != -1)
            e = roomEntities.get(idx);
        Meterman.gm.entitySelected(e);
    }

    /** Like {@link #roomEntitySelected(int)}, but for the inventory list. */
    void inventoryEntitySelected(int idx) {
        Entity e = null;
        if (idx != -1)
            e = inventoryEntities.get(idx);
        Meterman.gm.entitySelected(e);
    }
}
