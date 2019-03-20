/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.plugin.filter.impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.VeLaFileLoadChooser;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;

/**
 * This dialog permits the user to specify a VeLa expression for the purpose of
 * filtering observations.
 */
@SuppressWarnings("serial")
public class VeLaObservationFilterDialog extends AbstractOkCancelDialog {

	private JTextField nameField;

	private JCheckBox includeFainterThanObservationCheckbox;
	private JCheckBox includeDiscrepantObservationCheckbox;
	private JCheckBox includeExcludedObservationCheckbox;

	private JTextArea velaFilterField;

	private JComboBox<String> obsPropsList;

	/**
	 * Constructor
	 */
	public VeLaObservationFilterDialog() {
		super("Filter Observations");

		Mediator.getInstance().getNewStarNotifier()
				.addListener(createNewStarListener());

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createNamePane());
		topPane.add(createFilterPane());
		topPane.add(createObsPropsListPane());
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
	}

	public String getFilterName() {
		return nameField.getText();
	}

	public String getVeLaExpression() {
		return velaFilterField.getText();
	}

	public boolean includeFainterThan() {
		return includeFainterThanObservationCheckbox.isSelected();
	}

	public boolean includeDiscrepant() {
		return includeDiscrepantObservationCheckbox.isSelected();
	}

	public boolean includeExcluded() {
		return includeExcludedObservationCheckbox.isSelected();
	}

	@Override
	public void showDialog() {
		String defaultName = Mediator.getInstance().getDocumentManager()
				.getNextUntitledFilterName();
		nameField.setText(defaultName);
		super.showDialog();
	}

	private JPanel createNamePane() {
		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		nameField = new JTextField();
		nameField.setBorder(BorderFactory.createTitledBorder("Filter Name"));
		panel.add(nameField);

		return panel;
	}

	private JPanel createFilterPane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		velaFilterField = new JTextArea();
		velaFilterField.setRows(8);

		panel.add(new JScrollPane(velaFilterField));

		return panel;
	}

	private JPanel createObsPropsListPane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		obsPropsList = new JComboBox<String>();
		obsPropsList.setBorder(BorderFactory
				.createTitledBorder("Observation Properties"));

		obsPropsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String propText = (String) obsPropsList.getSelectedItem();
				velaFilterField.append(propText);
			}
		});

		panel.add(obsPropsList);

		return panel;
	}

	@Override
	protected JPanel createButtonPane() {
		JPanel extraButtonPanel = new JPanel(new FlowLayout());

		JPanel includePanel = new JPanel(new FlowLayout());
		// JPanel includePanel = new JPanel();
		// includePanel.setLayout(new BoxLayout(includePanel,
		// BoxLayout.LINE_AXIS));
		includePanel.setBorder(BorderFactory.createTitledBorder("Include"));

		includeFainterThanObservationCheckbox = new JCheckBox("Fainter Than?");
		includePanel.add(includeFainterThanObservationCheckbox);
		includeDiscrepantObservationCheckbox = new JCheckBox("Discrepant?");
		includePanel.add(includeDiscrepantObservationCheckbox);
		includeExcludedObservationCheckbox = new JCheckBox("Excluded?");
		includePanel.add(includeExcludedObservationCheckbox);

		extraButtonPanel.add(includePanel);

		JButton loadButton = new JButton(LocaleProps.get("LOAD_BUTTON"));
		loadButton.addActionListener(e -> {
			StringBuffer code = new StringBuffer();

			VeLaFileLoadChooser chooser = Mediator.getInstance()
					.getVelaFileLoadDialog();

			if (chooser.showDialog(this)) {
				try (Stream<String> stream = Files.lines(Paths.get(chooser
						.getSelectedFile().getAbsolutePath()))) {
					stream.forEachOrdered(line -> {
						code.append(line);
						code.append("\n");
					});
					velaFilterField.setText(code.toString());
				} catch (IOException ex) {
					// Nothing to do
			}
		}
	})	;
		extraButtonPanel.add(loadButton);

		JButton clearButton = new JButton(LocaleProps.get("CLEAR_BUTTON"));
		clearButton.addActionListener(createClearButtonListener());
		extraButtonPanel.add(clearButton, BorderLayout.LINE_END);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(extraButtonPanel);
		panel.add(super.createButtonPane());

		return panel;
	}

	// Return a listener for the reset button.
	private ActionListener createClearButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				velaFilterField.setText("");
				includeDiscrepantObservationCheckbox.setSelected(false);
				includeExcludedObservationCheckbox.setSelected(false);
				includeFainterThanObservationCheckbox.setSelected(false);
			}
		};
	}

	/**
	 * @return A new star listener for the filter dialog.
	 */
	public Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {

			@Override
			public void update(NewStarMessage info) {
				velaFilterField.setText("");
				String[] props = VeLaValidObservationEnvironment.symbols();
				obsPropsList.setModel(new DefaultComboBoxModel<String>(props));
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	@Override
	protected void cancelAction() {
		// Nothing to be done.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	@Override
	protected void okAction() {
		if (velaFilterField.getText().length() != 0) {
			// TODO: call a VeLa interpreter parse method and catch
			// VeLaParseError before calling these
			setCancelled(false);
			setVisible(false);
		}
	}
}
