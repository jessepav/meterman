package com.illcode.meterman.ui.swingui;

import com.illcode.meterman.Meterman;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

class MainFrame implements ActionListener
{
    JFrame frame;
    JMenuItem newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem, quitMenuItem, aboutMenuItem;
    JCheckBoxMenuItem musicCheckBoxMenuItem, soundCheckBoxMenuItem;
    JPanel imagePanel;
    JLabel roomNameLabel;
    JButton lookButton, waitButton;
    JTextArea mainTextArea, objectTextArea;
    JList<String> roomList, inventoryList;
    JButton[] exitButtons, actionButtons;
    JComboBox<String> moreActionCombo;
    JLabel leftStatusLabel, centerStatusLabel, rightStatusLabel;

    DefaultListModel<String> roomListModel, inventoryListModel;

    @SuppressWarnings("unchecked")
    public MainFrame() {
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

        } catch (Exception ex) {
            logger.log(Level.WARNING, "MainFrame()", ex);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == lookButton) {
            Meterman.gm.lookAction();
        } else if (source == waitButton) {
            Meterman.gm.waitAction();
        } else if (source == aboutMenuItem) {
            Meterman.gm.aboutMenuClicked();
        }
    }

    void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}
