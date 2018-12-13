package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.UIConstants;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SwingUI implements MetermanUI
{
    MainFrame mainFrame;
    TextDialog textDialog;
    PromptDialog promptDialog;
    ListDialog listDialog;
    ImageDialog imageDialog;

    private List<Entity> roomEntities, inventoryEntities;

    private boolean realized;  // true once the UI has been made visible on the EDT

    private Map<String,BufferedImage> imageMap;
    private BufferedImage defaultFrameImage;
    private String currentFrameImage, currentEntityImage;

    int maxBufferSize;

    public void init() {
        GuiUtils.initGraphics();

        roomEntities = new ArrayList<>();
        inventoryEntities = new ArrayList<>();
        imageMap = new HashMap<>();
        mainFrame = new MainFrame(this);
        textDialog = new TextDialog(mainFrame.frame);
        promptDialog = new PromptDialog(mainFrame.frame);
        listDialog = new ListDialog(mainFrame.frame);
        imageDialog = new ImageDialog(mainFrame.frame);

        setStatusLabel(UIConstants.LEFT_LABEL, "");
        setStatusLabel(UIConstants.CENTER_LABEL, "");
        setStatusLabel(UIConstants.RIGHT_LABEL, "");

        defaultFrameImage = GuiUtils.loadBitmaskImage(Utils.pathForSystemAsset("default-frame-image.png"));
        currentFrameImage = NO_IMAGE;
        currentEntityImage = NO_IMAGE;
    }

    public void dispose() {
        unloadAllImages();
        defaultFrameImage.flush();
        defaultFrameImage = null;
        imageDialog.dispose();
        listDialog.dispose();
        promptDialog.dispose();
        textDialog.dispose();
        mainFrame.dispose();
    }

    public boolean run() {
        maxBufferSize = Utils.intPref("max-text-buffer-size", 50000);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ex) {
                    Utils.logger.log(Level.WARNING, "UIManager.setLookAndFeel()", ex);
                }
                setGameName(null);
                mainFrame.setVisible(true);
                realized = true;
                mainFrame.startup();
            }
        });
        return false;
    }

    public void setGameName(String name) {
        if (name == null) {
            mainFrame.frame.setTitle("Meterman (no game loaded)");
            mainFrame.aboutMenuItem.setText("About...");
            mainFrame.aboutMenuItem.setEnabled(false);
        } else {
            mainFrame.frame.setTitle("Meterman - " + name);
            mainFrame.aboutMenuItem.setText("About " + name);
            mainFrame.aboutMenuItem.setEnabled(true);
        }
    }

    public void openURL(String url) {
        DesktopUtils.browseURI(url);
    }

    public void loadImage(String name, Path p) {
        if (!imageMap.containsKey(name)) {
            BufferedImage img = GuiUtils.loadBitmaskImage(p);
            if (img != null)
                imageMap.put(name, img);
        }
    }

    public void unloadImage(String name) {
        BufferedImage img = imageMap.get(name);
        if (img != null)
            img.flush();
        imageMap.remove(name);
    }

    public void unloadAllImages() {
        mainFrame.setFrameImage(null);
        currentFrameImage = NO_IMAGE;
        mainFrame.setEntityImage(null);
        for (BufferedImage img : imageMap.values())
            img.flush();
        imageMap.clear();
    }

    public void setFrameImage(String imageName) {
        if (currentFrameImage.equals(imageName))
            return;
        currentFrameImage = imageName;
        BufferedImage img;
        if (imageName == DEFAULT_FRAME_IMAGE)
            img = defaultFrameImage;
        else if (imageName == NO_IMAGE)
            img = null;
        else
            img = imageMap.get(imageName);
        mainFrame.setFrameImage(img);
    }

    public String getFrameImage() {
        return currentFrameImage;
    }

    public void setEntityImage(String imageName) {
        if (currentEntityImage.equals(imageName))
            return;
        currentEntityImage = imageName;
        BufferedImage img;
        if (imageName == NO_IMAGE)
            img = null;
        else
            img = imageMap.get(imageName);
        mainFrame.setEntityImage(img);
    }

    public String getEntityImage() {
        return currentEntityImage;
    }

    public void setRoomName(String name) {
        mainFrame.roomNameLabel.setText(name);
    }

    public void clearText() {
        mainFrame.mainTextArea.setText(null);
    }

    public void appendText(String text) {
        JTextArea ta = mainFrame.mainTextArea;
        ta.append(text);
        Document doc = ta.getDocument();
        int len = doc.getLength();
        if (len > maxBufferSize) {
            try {
                doc.remove(0, len - maxBufferSize);
                len = maxBufferSize;
            } catch (BadLocationException e) {
                Utils.logger.log(Level.WARNING, "SwingUI.appendText()", e);
            }
        }
        ta.setCaretPosition(len); // scroll to the bottom of the text area
    }

    public void appendNewline() {
        mainFrame.mainTextArea.append("\n");
    }

    public void appendTextLn(String text) {
        appendText(text);
        appendNewline();
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

    public void addInventoryEntity(Entity e, String modifiers) {
        inventoryEntities.add(e);
        String s = e.getListName();
        if (modifiers != null)
            s += " " + modifiers;
        mainFrame.inventoryListModel.addElement(s);
    }

    public void removeInventoryEntity(Entity e) {
        int idx = inventoryEntities.indexOf(e);
        if (idx != -1) {
            inventoryEntities.remove(idx);
            mainFrame.inventoryListModel.remove(idx);
        }
    }

    public void selectEntity(Entity e) {
        int idx = roomEntities.indexOf(e);
        if (idx != -1) {
            mainFrame.roomList.setSelectedIndex(idx);
        } else {
            idx = inventoryEntities.indexOf(e);
            if (idx != -1)
                mainFrame.inventoryList.setSelectedIndex(idx);
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

    public void showImageDialog(String header, String imageName, int scale, String text, String buttonLabel) {
        BufferedImage image = imageMap.get(imageName);
        if (image != null && scale > 1)
            image = GuiUtils.getScaledImage(image, scale);
        imageDialog.show(header, image, text, buttonLabel);
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
