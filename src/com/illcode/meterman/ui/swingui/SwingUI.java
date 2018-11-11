package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Entity;
import com.illcode.meterman.ui.MetermanUI;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SwingUI implements MetermanUI
{
    private MainFrame mainFrame;
    private TextDialog textDialog;
    private PromptDialog promptDialog;
    private ListDialog listDialog;

    private BufferedImage frameImage, entityImage;
    private List<Entity> roomEntities, inventoryEntities;

    public void init() {
        roomEntities = new ArrayList<>();
        inventoryEntities = new ArrayList<>();
    }

    public void dispose() {
        if (frameImage != null) {
            frameImage.flush();
            frameImage = null;
        }
        if (entityImage != null) {
            entityImage.flush();
            entityImage = null;
        }
    }

    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);
    }

    public boolean run() {
        return false;
    }

    public void setTitle(String title) {

    }

    public void openURL(String url) {

    }

    public void setFrameImage(BufferedImage image) {

    }

    public void setEntityImage(BufferedImage image) {

    }

    public void setRoomName(String name) {

    }

    public void clearText() {

    }

    public void appendText(String text) {

    }

    public void setObjectName(String name) {

    }

    public void setObjectText(String text) {

    }

    public void clearRoomEntities() {

    }

    public void addRoomEntity(Entity e) {

    }

    public void removeRoomEntity(Entity e) {

    }

    public void clearInventoryEntities() {

    }

    public void addInventoryEntity(Entity e) {

    }

    public void removeInventoryEntity(Entity e) {

    }

    public void clearExits() {

    }

    public void setExitLabel(int buttonPos, String label) {

    }

    public void clearActions() {

    }

    public void addAction(String actionLabel) {

    }

    public void removeAction(String actionLabel) {

    }

    public void setStatusLabel(int labelPosition, String label) {

    }

    public void showTextDialog(String header, String text, String buttonLabel) {

    }

    public String showPromptDialog(String header, String text, String prompt) {
        return null;
    }

    public <T> T showListDialog(String header, String text, List<T> items) {
        return null;
    }
}
