package com.illcode.meterman.ui.swingui;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Window;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

public class SelectItemDialog
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JLabel promptLabel;
    JComboBox<String> itemComboBox;
    JButton okButton, cancelButton;

    public SelectItemDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman/ui/swingui/SelectItemDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");

        } catch (Exception ex) {
            logger.log(Level.WARNING, "PromptDialog()", ex);
        }
    }
}
