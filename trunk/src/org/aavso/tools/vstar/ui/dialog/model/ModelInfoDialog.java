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
package org.aavso.tools.vstar.ui.dialog.model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.RelativeAmplitudeAndPhaseCreator;

/**
 * This dialog displays information about a model.
 */
@SuppressWarnings("serial")
public class ModelInfoDialog extends JDialog {

	// TODO: make precision (4) a preference.
	private final static int precision = 4;

	private IModel model;

	private JTextArea relAmplAndPhaseTextArea;
	private JCheckBox useCyclesCheckBox;

	private RelativeAmplitudeAndPhaseCreator creator;

	private String starName;
	private double startTime;
	private double endTime;
	private double averageTime;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent dialog.
	 * @param model
	 *            The model about which information is to be displayed.
	 */
	public ModelInfoDialog(JDialog parent, IModel model) {
		super(parent);
		this.model = model;

		starName = Mediator.getInstance().getLatestNewStarMessage().getStarInfo()
				.getDesignation();

		// TODO: should these times be from the original input series JDs rather
		// than from the model.
		startTime = model.getFit().get(0).getJD();
		endTime = model.getFit().get(model.getFit().size() - 1).getJD();

		averageTime = 0;
		for (ValidObservation ob : model.getFit()) {
			averageTime += ob.getJD();
		}
		averageTime /= model.getFit().size();

		this.setTitle(LocaleProps.get("MODEL_INFO_DLG_TITLE"));
		this.setModal(true);
		this.setSize(200, 200);

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea textArea = new JTextArea(model.toString());
		textArea.setBorder(BorderFactory.createTitledBorder(LocaleProps
				.get("MODEL_INFO_FUNCTION_TITLE")));
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		topPane.add(scrollPane);

		if (model.getParameters() != null) {
			creator = new RelativeAmplitudeAndPhaseCreator(model
					.getParameters());

			if (creator.hasHarmonics()) {
				topPane.add(Box.createRigidArea(new Dimension(10, 10)));
				topPane.add(createRelAmplAndPhasePanel());
			}
		}

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel buttonPane = new JPanel();
		JButton okButton = new JButton(LocaleProps.get("OK_BUTTON"));
		okButton.addActionListener(createOKButtonHandler());
		buttonPane.add(okButton, BorderLayout.CENTER);
		topPane.add(buttonPane);

		this.getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(okButton);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	private JPanel createRelAmplAndPhasePanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		String title = LocaleProps
				.get("RELATIVE_AMPLITUDE_AND_PHASE_PANE_TITLE");
		pane.setBorder(BorderFactory.createTitledBorder(title));

		relAmplAndPhaseTextArea = new JTextArea();
		relAmplAndPhaseTextArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(relAmplAndPhaseTextArea);
		pane.add(scrollPane);

		JPanel cyclesPane = new JPanel();
		useCyclesCheckBox = new JCheckBox("Show as cycles?");
		useCyclesCheckBox.setSelected(false);
		useCyclesCheckBox.addActionListener(createUseCyclesCheckbox());
		cyclesPane.add(useCyclesCheckBox, BorderLayout.CENTER);
		pane.add(cyclesPane);

		setRelAmplAndPhaseText(useCyclesCheckBox.isSelected());

		return pane;
	}

	private void setRelAmplAndPhaseText(boolean cycles) {
		// fundamental: star-name start-time end-time average-time rel-ampl-1
		// rel-phase-1 ... rel-ampl-n rel-phase-n
		String fmt = "%1." + precision + "f: %s %1." + precision + "f %1."
				+ precision + "f %1." + precision + "f ";

		String relStr = "";

		for (Double fundamental : creator.getFundamentals()) {

			String relSeqStr = creator.getRelativeSequenceString(fundamental,
					precision, cycles);

			if (!"".equals(relSeqStr)) {
				relStr += String.format(fmt, fundamental, starName, startTime,
						endTime, averageTime)
						+ relSeqStr + "\n";
			}

		}

		relAmplAndPhaseTextArea.setText(relStr);
	}

	private ActionListener createUseCyclesCheckbox() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRelAmplAndPhaseText(useCyclesCheckBox.isSelected());
			}
		};
	}

	private ActionListener createOKButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}
}
