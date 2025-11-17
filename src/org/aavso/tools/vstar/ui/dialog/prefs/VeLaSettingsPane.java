/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.vela.VeLaPrefs;

/**
 * VeLa Preferences pane
 */
@SuppressWarnings("serial")
public class VeLaSettingsPane extends JPanel implements IPreferenceComponent {

    private JFileChooser veLaCodeDirectoryChooser;
    private JTextArea veLaCodeDirectoriesField;

    private JCheckBox veLaDiagnosticCheckbox;

    public VeLaSettingsPane() {
        JPanel veLaSettingsPanel = new JPanel();
        veLaSettingsPanel.setLayout(new BoxLayout(veLaSettingsPanel, BoxLayout.PAGE_AXIS));
        veLaSettingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // VeLa code directories
        JPanel veLaCodeDirPanel = new JPanel();
        veLaCodeDirPanel.setLayout(new BoxLayout(veLaCodeDirPanel, BoxLayout.LINE_AXIS));
        veLaCodeDirPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        veLaCodeDirectoriesField = new JTextArea(VeLaPrefs.getCodeDirs());
        veLaCodeDirectoriesField.setEditable(false);
        veLaCodeDirectoriesField.setToolTipText("VeLa code directories");
        veLaCodeDirectoriesField.setBorder(BorderFactory.createTitledBorder("Directories"));

        veLaCodeDirPanel.add(veLaCodeDirectoriesField);

        veLaCodeDirectoryChooser = new JFileChooser();
        veLaCodeDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton addVeLaCodeDirButton = new JButton("Add VeLa Code Directory");
        addVeLaCodeDirButton.addActionListener(e -> requestDirToAdd());

        veLaCodeDirPanel.add(addVeLaCodeDirButton);

        veLaSettingsPanel.add(veLaCodeDirPanel);

        veLaSettingsPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // VeLa dialog diagnostic mode
        JPanel veLaCheckboxPanel = new JPanel();
        veLaCheckboxPanel.setLayout(new BoxLayout(veLaCheckboxPanel, BoxLayout.LINE_AXIS));
        veLaCheckboxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        veLaDiagnosticCheckbox = new JCheckBox("VeLa dialog diagnostic mode?");
        veLaDiagnosticCheckbox.setSelected(VeLaPrefs.getDiagnosticMode());
        veLaDiagnosticCheckbox.setToolTipText("Diagnostic mode yields ASTs as DOT and LISP");

        veLaCheckboxPanel.add(veLaDiagnosticCheckbox);
        veLaSettingsPanel.add(veLaCheckboxPanel);

        veLaSettingsPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // Add a local context button pane.
        veLaSettingsPanel.add(createButtonPane());

        add(veLaSettingsPanel);
    }

    protected JPanel createButtonPane() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton setDefaultsButton = new JButton("Set Defaults");
        setDefaultsButton.addActionListener(createSetDefaultsButtonActionListener());
        panel.add(setDefaultsButton, BorderLayout.LINE_START);

        JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
        applyButton.addActionListener(createApplyButtonActionListener());
        panel.add(applyButton, BorderLayout.LINE_END);

        return panel;
    }

    @Override
    public void update() {
        VeLaPrefs.setCodeDirs(veLaCodeDirectoriesField.getText());
        VeLaPrefs.setDiagnosticMode(veLaDiagnosticCheckbox.isSelected());
    }

    /**
     * Prepare this pane for use by resetting whatever needs to be, in particular,
     * updating widgets with current preferences.
     */
    @Override
    public void reset() {
        veLaCodeDirectoriesField.setText(VeLaPrefs.getCodeDirs());
        veLaDiagnosticCheckbox.setSelected(VeLaPrefs.getDiagnosticMode());
    }

    // Set defaults action button listener creator.
    private ActionListener createSetDefaultsButtonActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                veLaDiagnosticCheckbox.setSelected(Boolean.parseBoolean(VeLaPrefs.DEFAULT_DIAGNOSTIC_MODE_STR));
                veLaCodeDirectoriesField.setText(VeLaPrefs.DEFAULT_CODE_DIR_STR);
            }
        };
    }

    // Set apply button listener creator.
    private ActionListener createApplyButtonActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        };
    }

    /**
     * Open file chooser to ask for a code directory to add.
     * 
     * @return An optional directory
     */
    private void requestDirToAdd() {
        int retVal = veLaCodeDirectoryChooser.showOpenDialog(DocumentManager.findActiveWindow());

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File dir = veLaCodeDirectoryChooser.getSelectedFile();

            String dirStr = veLaCodeDirectoriesField.getText();
            if (!dirStr.isEmpty()) {
                // TODO: perhaps we really want a JList instead with dirList as model
                dirStr += "\n";
            }
            dirStr += dir.getAbsolutePath();
            veLaCodeDirectoriesField.setText(dirStr);
        }
    }
}
