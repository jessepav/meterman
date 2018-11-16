package com.illcode.meterman.ui.swingui;

import javax.swing.*;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

class ListDialog implements ActionListener, MouseListener
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JTextArea textArea;
    JList<String> list;
    DefaultListModel<String> listModel;
    JButton okButton, cancelButton;

    @SuppressWarnings("unchecked")
    ListDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman/ui/swingui/ListDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            textArea = cr.getTextArea("textArea");
            list = cr.getList("list");
            okButton = cr.getButton("okButton");
            cancelButton = cr.getButton("cancelButton");

            listModel = new DefaultListModel<>();
            list.setModel(listModel);

            okButton.addActionListener(this);
            cancelButton.addActionListener(this);
            list.addMouseListener(this);
            dialog.getRootPane().setDefaultButton(okButton);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "ListDialog()", ex);
        }
    }

    public <T> T showListDialog(String header, String text, List<T> items, boolean showCancelButton) {
        headerLabel.setText(header);
        textArea.setText(text);
        listModel.clear();
        for (T item : items)
            listModel.addElement(item.toString());
        cancelButton.setVisible(showCancelButton);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        list.requestFocusInWindow();
        dialog.setVisible(true);  // blocks until hidden
        int idx = list.getSelectedIndex();
        if (idx == -1)
            return null;
        else
            return items.get(idx);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == okButton) {
            dialog.setVisible(false);
        } else if (source == cancelButton) {
            list.clearSelection();
            dialog.setVisible(false);
        }
    }

    public void dispose() {
        dialog.dispose();
    }


    //region MouseListener implementation
    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();
        if (source == list) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
                okButton.doClick();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    //endregion

}
