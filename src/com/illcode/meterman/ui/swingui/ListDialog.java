package com.illcode.meterman.ui.swingui;

import javax.swing.*;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

class ListDialog implements ActionListener
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JTextArea textArea;
    JList<String> list;
    DefaultListModel<String> listModel;
    JButton okButton;

    @SuppressWarnings("unchecked")
    ListDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman/ui/swingui/MainFrame.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            textArea = cr.getTextArea("textArea");
            list = cr.getList("list");
            okButton = cr.getButton("okButton");

            listModel = new DefaultListModel<>();
            list.setModel(listModel);

            okButton.addActionListener(this);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "ListDialog()", ex);
        }
    }

    public <T> T showListDialog(String header, String text, List<T> items) {
        headerLabel.setText(header);
        textArea.setText(text);
        listModel.clear();
        for (T item : items)
            listModel.addElement(item.toString());
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);  // blocks until hidden
        int idx = list.getSelectedIndex();
        if (idx == -1)
            return null;
        else
            return items.get(idx);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == okButton)
            dialog.setVisible(false);
    }

    public void dispose() {
        dialog.dispose();
    }
}
