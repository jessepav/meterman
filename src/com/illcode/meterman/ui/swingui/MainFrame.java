package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Meterman;
import com.illcode.meterman.games.GamesList;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
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

    DefaultListModel<String> roomListModel, inventoryListModel;

    private BufferedImage frameImage, entityImage;
    private BufferedImage defaultFrameImage;
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
            
            defaultFrameImage = GuiUtils.loadBitmaskImage(Paths.get("assets/meterman/default-frame-image.png"), -1);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "MainFrame()", ex);
        }
    }

    void dispose() {
        if (frameImage != null) {
            frameImage.flush();
            frameImage = null;
        }
        if (entityImage != null) {
            entityImage.flush();
            entityImage = null;
        }
        setVisible(false);
        frame.dispose();
    }

    void setFrameImage(BufferedImage image) {
        if (image != frameImage) {
            frameImage = image;
            imageComponent.repaint();
        }
    }

    void setEntityImage(BufferedImage image) {
        if (image != entityImage) {
            entityImage = image;
            imageComponent.repaint();
        }
    }

    void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void clearExits() {
        for (JButton b : exitButtons)
            b.setVisible(false);
    }

    public void setExitLabel(int buttonPos, String label) {
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
            Meterman.gm.lookAction();
        } else if (source == waitButton) {
            Meterman.gm.waitAction();
        } else if ((buttonIdx = ArrayUtils.indexOf(exitButtons, source)) != -1) {
            Meterman.gm.exitSelected(buttonIdx);
        } else if ((buttonIdx = ArrayUtils.indexOf(actionButtons, source)) != -1) {
            Meterman.gm.entityActionSelected(actionButtons[buttonIdx].getText());
        } else if (source == moreActionCombo) {
            int idx = moreActionCombo.getSelectedIndex();
            if (idx > 0)   // index 0 is "More..."
                Meterman.gm.entityActionSelected(moreActionCombo.getItemAt(idx));
        } else if (source == musicCheckBoxMenuItem) {
            Meterman.sound.setMusicEnabled(musicCheckBoxMenuItem.isSelected());
        } else if (source == soundCheckBoxMenuItem) {
            Meterman.sound.setSoundEnabled(soundCheckBoxMenuItem.isSelected());
        } else if (source == quitMenuItem) {
            close();
        } else if (source == aboutMenuItem) {
            Meterman.gm.aboutMenuClicked();
        } else if (source == newMenuItem) {
            ui.listDialog.list.addListSelectionListener(this);
            String gameName = ui.showListDialog("New Game", GamesList.getGameDescription("select-game"),
                Arrays.asList(GamesList.games), true);
            ui.listDialog.list.removeListSelectionListener(this);
            if (gameName != null)
                Meterman.gm.newGame(GamesList.getGame(gameName));
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
        } else if (source == ui.listDialog.list) {
            String selectedGame = ui.listDialog.list.getSelectedValue();
            if (selectedGame == null)
                selectedGame = "select-game";
            ui.listDialog.textArea.setText(GamesList.getGameDescription(selectedGame));
        }
    }

    private class FrameImageComponent extends JComponent {
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            BufferedImage img = frameImage != null ? frameImage : defaultFrameImage;
            if (img != null) {
                int cw = getWidth();
                int iw = img.getWidth();
                int ih = img.getHeight();
                float ratio = (float) cw / iw;
                RenderingHints oldHints = g2d.getRenderingHints();
                g2d.setRenderingHints(GuiUtils.getQualityRenderingHints());
                g2d.drawImage(img, 0, 0, cw, (int) (ih * ratio), null);
                g2d.setRenderingHints(oldHints);
            }
            if (entityImage != null) {
                int cw = getWidth();
                int ch = getHeight();
                int iw = entityImage.getWidth();
                int ih = entityImage.getHeight();
                final int margin = cw / 10;
                cw -= 2*margin;
                float ratio = (float) cw / iw;
                ih = (int) (ih * ratio);
                RenderingHints oldHints = g2d.getRenderingHints();
                g2d.setRenderingHints(GuiUtils.getQualityRenderingHints());
                g2d.drawImage(entityImage, margin, ch / 2 - ih / 3, cw, ih, null);
                g2d.setRenderingHints(oldHints);
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
