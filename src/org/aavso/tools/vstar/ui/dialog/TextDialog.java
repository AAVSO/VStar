/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2012  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * This class implements a dialog to obtain one or more string values from text
 * boxes.
 */
@SuppressWarnings("serial")
public class TextDialog extends AbstractOkCancelDialog {

    private List<ITextComponent<String>> textFields;

    /**
     * Constructor<br/>
     * 
     * If there are only two fields, a split pane is used to contain the fields.
     * 
     * @param title      The title to be used for the dialog.
     * @param fields     A list of text fields.
     * @param show       Show the dialog immediately?
     * @param scrollable Are text fields scrollable?
     */
    public TextDialog(String title, List<ITextComponent<String>> fields, boolean show, boolean scrollable) {
        super(title);
        this.setModal(true);

        Container contentPane = this.getContentPane();

        JPanel topPane = new JPanel();

        textFields = new ArrayList<ITextComponent<String>>();

        for (ITextComponent<String> field : fields) {
            textFields.add(field);
        }

        if (fields.size() == 2) {
            topPane.setLayout(new BorderLayout());
            JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
            splitter.add(createTextFieldPane(fields.get(0), scrollable));
            splitter.add(createTextFieldPane(fields.get(1), scrollable));
            splitter.setResizeWeight(0.5);
            topPane.add(splitter, BorderLayout.CENTER);
        } else {
            topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
            for (ITextComponent<String> field : fields) {
                topPane.add(createTextFieldPane(field, scrollable));
                topPane.add(Box.createRigidArea(new Dimension(75, 10)));
            }
        }

        topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createButtonPane(), BorderLayout.CENTER);
        topPane.add(bottomPanel, BorderLayout.PAGE_END);

        contentPane.add(topPane);

        this.pack();

        if (show) {
            showDialog();
        }
    }

    /**
     * Constructor<br/>
     * 
     * If there are only two fields, a split pane is used to contain the fields.
     * 
     * @param title  The title to be used for the dialog.
     * @param fields A list of text fields.
     * @param Show   the dialog immediately?
     */
    public TextDialog(String title, List<ITextComponent<String>> fields, boolean show) {
        this(title, fields, show, false);
    }

    /**
     * Constructor.
     * 
     * @param title  The title to be used for the dialog.
     * @param fields A list of text fields.
     */
    public TextDialog(String title, List<ITextComponent<String>> fields) {
        this(title, fields, true);
    }

    /**
     * Constructor.
     * 
     * @param title      The title to be used for the dialog.
     * @param show       Show the dialog immediately?
     * @param scrollable Are text fields scrollable?
     * @param fields     A variable number of text fields.
     */
    public TextDialog(String title, boolean show, boolean scrollable, ITextComponent<String>... fields) {
        this(title, Arrays.asList(fields), show, scrollable);
    }

    /**
     * Constructor.
     * 
     * @param title  The title to be used for the dialog.
     * @param fields A variable number of text fields.
     */
    public TextDialog(String title, ITextComponent<String>... fields) {
        this(title, Arrays.asList(fields), true);
    }

    public List<ITextComponent<String>> getTextFields() {
        return textFields;
    }

    /**
     * Get a list of strings from the text fields.
     * 
     * @return a list of strings.
     */
    public List<String> getTextStrings() {
        List<String> strings = new ArrayList<String>();

        for (ITextComponent<String> field : textFields) {
            strings.add(field.getValue());
        }

        return strings;
    }

    private JPanel createTextFieldPane(ITextComponent<String> field, boolean scrollable) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        field.setEditable(!field.isReadOnly());

        if (scrollable) {
            JScrollPane scrollPane = new JScrollPane(field.getUIComponent());
            panel.add(scrollPane);
        } else {
            panel.add(field.getUIComponent());
        }

        return panel;
    }

    /**
     * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
     */
    protected void cancelAction() {
        // Nothing to do
    }

    /**
     * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
     */
    protected void okAction() {
        // If there is a field that cannot be empty, but is, we cannot dismiss
        // the dialog.
        for (ITextComponent<String> field : textFields) {
            if (!field.canBeEmpty() && field.getValue().trim().length() == 0) {
                return;
            }
        }

        cancelled = false;
        setVisible(false);
        dispose();
    }
}
