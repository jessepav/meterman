package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.games.GamesList;
import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.UIConstants;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

class MainFrame implements ActionListener, ListSelectionListener
{
    static final int NUM_EXIT_BUTTONS = 12;
    static final int NUM_ACTION_BUTTONS = 8;

    private SwingUI ui;

    JFrame frame;
    JMenuItem newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem, quitMenuItem, aboutMenuItem;
    JCheckBoxMenuItem musicCheckBoxMenuItem, soundCheckBoxMenuItem;
    JPanel imagePanel;
    JLabel roomNameLabel, objectNameLabel;
    JButton lookButton, waitButton;
    JTextArea mainTextArea, objectTextArea;
    JList<String> roomList, inventoryList;
    JButton[] exitButtons, actionButtons;
    JComboBox<String> moreActionCombo;
    JLabel leftStatusLabel, centerStatusLabel, rightStatusLabel;
    FrameImageComponent imageComponent;

    JFileChooser fc;
    File lastSaveFile;

    DefaultListModel<String> roomListModel, inventoryListModel;

    private BufferedImage frameImage, entityImage;
    private List<String> actions;

    private boolean suppressValueChanged;

    @SuppressWarnings("unchecked")
    public MainFrame(SwingUI ui) {
        this.ui = ui;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman/ui/swingui/MainFrame.jfd");
            FormCreator cr = new FormCreator(formModel);

            frame = (JFrame) cr.createWindow(null);
            newMenuItem = cr.getMenuItem("newMenuItem");
            saveMenuItem = cr.getMenuItem("saveMenuItem");
            saveAsMenuItem = cr.getMenuItem("saveAsMenuItem");
            loadMenuItem = cr.getMenuItem("loadMenuItem");
            quitMenuItem = cr.getMenuItem("quitMenuItem");
            aboutMenuItem = cr.getMenuItem("aboutMenuItem");
            musicCheckBoxMenuItem = cr.getCheckBoxMenuItem("musicCheckBoxMenuItem");
            soundCheckBoxMenuItem = cr.getCheckBoxMenuItem("soundCheckBoxMenuItem");
            imagePanel = cr.getPanel("imagePanel");
            roomNameLabel = cr.getLabel("roomNameLabel");
            objectNameLabel = cr.getLabel("objectNameLabel");
            lookButton = cr.getButton("lookButton");
            waitButton = cr.getButton("waitButton");
            mainTextArea = cr.getTextArea("mainTextArea");
            objectTextArea = cr.getTextArea("objectTextArea");
            roomList = cr.getList("roomList");
            inventoryList = cr.getList("inventoryList");
            exitButtons = new JButton[NUM_EXIT_BUTTONS];
            for (int i = 0; i < NUM_EXIT_BUTTONS; i++)
                exitButtons[i] = cr.getButton("exitButton" + (i+1));
            actionButtons = new JButton[NUM_ACTION_BUTTONS];
            for (int i = 0; i < NUM_ACTION_BUTTONS; i++)
                actionButtons[i] = cr.getButton("actionButton" + (i+1));
            moreActionCombo = cr.getComboBox("moreActionCombo");
            leftStatusLabel = cr.getLabel("leftStatusLabel");
            centerStatusLabel = cr.getLabel("centerStatusLabel");
            rightStatusLabel = cr.getLabel("rightStatusLabel");

            lookButton.setText(Utils.getActionName("Look"));
            waitButton.setText(Utils.getActionName("Wait"));

            imageComponent = new FrameImageComponent();
            imagePanel.add(imageComponent);

            frame.getRootPane().setDoubleBuffered(true);
            frame.addWindowListener(new FrameWindowListener());

            roomListModel = new DefaultListModel<>();
            inventoryListModel = new DefaultListModel<>();
            roomList.setModel(roomListModel);
            inventoryList.setModel(inventoryListModel);

            for (AbstractButton b : new AbstractButton[] {newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem,
                quitMenuItem, aboutMenuItem, musicCheckBoxMenuItem, soundCheckBoxMenuItem, lookButton, waitButton})
                b.addActionListener(this);
            for (JButton b : exitButtons)
                b.addActionListener(this);
            for (JButton b : actionButtons)
                b.addActionListener(this);
            roomList.addListSelectionListener(this);
            inventoryList.addListSelectionListener(this);
            moreActionCombo.addActionListener(this);
            actions = new ArrayList<>(16);
            
            fc = new JFileChooser();

            GuiUtils.setBoundsFromPrefs(frame, "main-window-size");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "MainFrame()", ex);
        }
    }

    void dispose() {
        GuiUtils.saveBoundsToPref(frame, "main-window-size");
        frameImage = null;
        entityImage = null;
        setVisible(false);
        frame.dispose();
    }

    void setFrameImage(BufferedImage image) {
        frameImage = image;
        imageComponent.repaint();
    }

    void setEntityImage(BufferedImage image) {
        entityImage = image;
        imageComponent.repaint();
    }

    void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void clearExits() {
        for (JButton b : exitButtons)
            b.setVisible(false);
    }

    public void setExitLabel(int buttonPos, String label) {
        if (buttonPos < 0 || buttonPos >= UIConstants.NUM_EXIT_BUTTONS)
            return;
        JButton b = exitButtons[buttonPos];
        if (label == null) {
            b.setVisible(false);
        } else {
            b.setVisible(true);
            b.setText(label);
        }
    }

    public void clearActions() {
        actions.clear();
        for (JButton b : actionButtons)
            b.setVisible(false);
        moreActionCombo.setVisible(false);
        moreActionCombo.removeAllItems();
        moreActionCombo.addItem("More...");
    }

    public void addAction(String actionLabel) {
        if (actions.contains(actionLabel))
            return;
        actions.add(actionLabel);
        int n = actions.size();
        if (n <= NUM_ACTION_BUTTONS) {
            JButton b = actionButtons[n - 1];
            b.setText(actionLabel);
            b.setVisible(true);
        } else {
            moreActionCombo.setVisible(true);
            moreActionCombo.addItem(actionLabel);
        }
    }

    public void removeAction(String actionLabel) {
        if (actions.remove(actionLabel)) {
            clearActions();
            for (String a : actions)
                addAction(a);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int buttonIdx;

        if (source == lookButton) {
            Meterman.gm.lookCommand();
        } else if (source == waitButton) {
            Meterman.gm.waitCommand();
        } else if ((buttonIdx = ArrayUtils.indexOf(exitButtons, source)) != -1) {
            Meterman.gm.exitSelected(buttonIdx);
        } else if ((buttonIdx = ArrayUtils.indexOf(actionButtons, source)) != -1) {
            Meterman.gm.entityActionSelected(actionButtons[buttonIdx].getText());
        } else if (source == moreActionCombo) {
            int idx = moreActionCombo.getSelectedIndex();
            if (idx > 0)   // index 0 is "More..."
                Meterman.gm.entityActionSelected(moreActionCombo.getItemAt(idx));
        } else if (source == newMenuItem) {
            ui.listDialog.list.addListSelectionListener(this);
            String gameName = ui.showListDialog("New Game", GamesList.getGameDescription("select-game"),
                GamesList.getGameNames(), true);
            ui.listDialog.list.removeListSelectionListener(this);
            if (gameName != null)
                Meterman.gm.newGame(GamesList.getGame(gameName));
        } else if (source == loadMenuItem) {
            int r = fc.showOpenDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try (InputStream in = new FileInputStream(f)) {
                    Meterman.gm.loadGameState(in);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "MainFrame loadMenuItem", ex);
                    ui.showTextDialog("Load Error", ex.getMessage(), "OK");
                }
            }
        } else if (source == saveMenuItem) {
            if (lastSaveFile == null) {
                saveAsMenuItem.doClick();
                return;
            }
            try (OutputStream out = new FileOutputStream(lastSaveFile)) {
                Meterman.gm.saveGameState(out);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "MainFrame saveMenuItem", ex);
                ui.showTextDialog("Save Error", ex.getMessage(), "OK");
            }
        } else if (source == saveAsMenuItem) {
            int r = fc.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                lastSaveFile = fc.getSelectedFile();
                saveMenuItem.doClick();
            }
        } else if (source == quitMenuItem) {
            close();
        } else if (source == musicCheckBoxMenuItem) {
            Meterman.sound.setMusicEnabled(musicCheckBoxMenuItem.isSelected());
        } else if (source == soundCheckBoxMenuItem) {
            Meterman.sound.setSoundEnabled(soundCheckBoxMenuItem.isSelected());
        } else if (source == aboutMenuItem) {
            Meterman.gm.aboutMenuClicked();
        }
    }

    private void close() {
        Meterman.shutdown();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (suppressValueChanged)
            return;
        Object source = e.getSource();
        if (source == roomList) {
            suppressValueChanged = true;
            inventoryList.clearSelection();
            suppressValueChanged = false;
            ui.roomEntitySelected(roomList.getSelectedIndex());
        } else if (source == inventoryList) {
            suppressValueChanged = true;
            roomList.clearSelection();
            suppressValueChanged = false;
            ui.inventoryEntitySelected(inventoryList.getSelectedIndex());
        } else if (source == ui.listDialog.list) {  // used only when starting a new game
            String selectedGame = ui.listDialog.list.getSelectedValue();
            if (selectedGame == null)
                selectedGame = "select-game";
            ui.listDialog.textArea.setText(GamesList.getGameDescription(selectedGame));
        }
    }

    public void startup() {
        soundCheckBoxMenuItem.setSelected(Meterman.sound.isSoundEnabled());
        musicCheckBoxMenuItem.setSelected(Meterman.sound.isMusicEnabled());

        ui.clearActions();
        ui.clearExits();
        ui.clearText();
        ui.setObjectName("(nothing selected)");
        ui.setObjectText("");
        ui.setFrameImage(MetermanUI.DEFAULT_FRAME_IMAGE);

        initGame:  // make the user keep selecting choices until a game is
        do {       // successfully started or loaded
            String choice;
            do {  // don't let the user avoid making a choice
                choice = ui.showListDialog("Meterman", "Select an option",
                    Arrays.asList("New Game", "Load Game", "Quit"), false);
            } while (choice == null);
            switch (choice) {
            case "New Game":
                newMenuItem.doClick();
                break;
            case "Load Game":
                loadMenuItem.doClick();
                break;
            case "Quit":
                close();
                break initGame;
            }
        } while (Meterman.gm.getGame() == null);
    }

    private class FrameImageComponent extends JComponent {
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int cw = getWidth();
            int ch = getHeight();
            int x, y, width, height;
            if (frameImage != null) {
                int iw = frameImage.getWidth();
                int ih = frameImage.getHeight();
                int scale = Utils.clamp(cw / iw, 1, ch / ih);
                width = iw * scale;
                height = ih * scale;
                x = Math.max(0, (cw - width) / 2);
                y = Math.max(0, (ch - height) / 2);
                g2d.drawImage(frameImage, x, y, width, height, null);
            }
            if (entityImage != null) {
                int iw = entityImage.getWidth();
                int ih = entityImage.getHeight();
                y = ch / 3;
                int scale = Utils.clamp(cw / iw, 1, (ch-y) / ih);
                width = iw * scale;
                height = ih * scale;
                x = Math.max(0, (cw - width) / 2);
                g2d.drawImage(entityImage, x, y, width, height, null);
            }
        }
    }

    private class FrameWindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e) {
            close();
        }
    }
}
