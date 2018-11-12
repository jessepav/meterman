package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Meterman;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

class MainFrame implements ActionListener, ListSelectionListener
{
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

    DefaultListModel<String> roomListModel, inventoryListModel;

    private BufferedImage frameImage, entityImage;

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
            exitButtons = new JButton[12];
            for (int i = 0; i < exitButtons.length; i++)
                exitButtons[i] = cr.getButton("exitButton" + (i+1));
            actionButtons = new JButton[8];
            for (int i = 0; i < actionButtons.length; i++)
                actionButtons[i] = cr.getButton("actionButton" + (i+1));
            moreActionCombo = cr.getComboBox("moreActionCombo");
            leftStatusLabel = cr.getLabel("leftStatusLabel");
            centerStatusLabel = cr.getLabel("centerStatusLabel");
            rightStatusLabel = cr.getLabel("rightStatusLabel");

            FrameImage fi = new FrameImage();
            imagePanel.add(fi);

            frame.getRootPane().setDoubleBuffered(true);

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
        frameImage = image;
    }

    void setEntityImage(BufferedImage image) {
        entityImage = image;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int buttonIdx;

        if (source == lookButton) {
            Meterman.gm.lookAction();
        } else if (source == waitButton) {
            Meterman.gm.waitAction();
        } else if (source == aboutMenuItem) {
            Meterman.gm.aboutMenuClicked();
        } else if ((buttonIdx = ArrayUtils.indexOf(exitButtons, source)) != -1) {
            Meterman.gm.exitSelected(buttonIdx);
        } else if ((buttonIdx = ArrayUtils.indexOf(actionButtons, source)) != -1) {
            Meterman.gm.entityActionSelected(actionButtons[buttonIdx].getText());
        } else if (source == musicCheckBoxMenuItem) {
            Meterman.sound.setMusicEnabled(musicCheckBoxMenuItem.isSelected());
        } else if (source == soundCheckBoxMenuItem) {
            Meterman.sound.setSoundEnabled(soundCheckBoxMenuItem.isSelected());
        } else if (source == quitMenuItem) {
            Meterman.shutdown();
        } else if (source == moreActionCombo) {
            int idx = moreActionCombo.getSelectedIndex();
            if (idx > 0)
                Meterman.gm.entityActionSelected(moreActionCombo.getItemAt(idx));
        }
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
        }
    }

    void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private class FrameImage extends JComponent {
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            if (frameImage != null) {
                int cw = getWidth();
                int iw = frameImage.getWidth();
                int ih = frameImage.getHeight();
                float ratio = (float) cw / iw;
                g2d.setRenderingHints(GuiUtils.getQualityRenderingHints());
                g2d.drawImage(frameImage, 0, 0, cw, (int) (ih * ratio), null);
            }
            if (entityImage != null) {
                int cw = getWidth();
                int ch = getHeight();
                int iw = entityImage.getWidth();
                int ih = entityImage.getHeight();

                final int margin = cw * 9 / 10;
                cw -= 2*margin;
                float ratio = (float) cw / iw;
                ih = (int) (ih * ratio);
                g2d.setRenderingHints(GuiUtils.getQualityRenderingHints());
                g2d.drawImage(entityImage, margin, ch / 2 - ih / 3, cw, ih, null);
            }
        }
    }
}
