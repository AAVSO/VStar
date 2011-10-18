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
package org.aavso.tools.vstar.plugin.period;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.util.IStartAndCleanup;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This can be used as the base class for period analysis dialogs.
 */
abstract public class PeriodAnalysisDialogBase extends JDialog implements
		IStartAndCleanup {

	private JButton newPhasePlotButton;
	private JButton findHarmonicsButton;
	private JPanel topPane;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The dialog title.
	 * @param isModal
	 *            Should the dialog be modal?
	 * @param isAlwaysOnTop
	 *            Should the dialog always be on top?
	 */
	public PeriodAnalysisDialogBase(String title, boolean isModal,
			boolean isAlwaysOnTop) {
		super();
		this.setTitle(title);
		this.setModal(isModal);
		this.setAlwaysOnTop(isAlwaysOnTop);
	}

	/**
	 * Constructor for a non-modal dialog that is not always on top.
	 * 
	 * @param title
	 *            The dialog title.
	 */
	public PeriodAnalysisDialogBase(String title) {
		this(title, false, false);
	}

	/**
	 * A subclass must invoke this when it wants to add the dialog's content and
	 * prepare it for visibility. It will in turn call createContent() and
	 * createButtonPanel().
	 */
	protected void prepareDialog() {
		topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createContent());
		topPane.add(createButtonPanel());

		this.getContentPane().add(topPane);
		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
	}

	// Methods that must be overridden by subclasses.

	/**
	 * This method is invoked when the new-phase-plot button is clicked.
	 */
	abstract protected void newPhasePlotButtonAction();

	/**
	 * This method is invoked when the find-harmonics button is clicked.
	 */
	abstract protected void findHarmonicsButtonAction();

	/**
	 * Create the content to be added to the dialog's content pane.
	 */
	abstract protected Component createContent();

	// Protected methods for use by subclasses.

	protected JPanel createButtonPanel() {
		JPanel buttonPane = new JPanel(new FlowLayout());

		newPhasePlotButton = new JButton("New Phase Plot");
		newPhasePlotButton.addActionListener(createNewPhasePlotButtonHandler());
		newPhasePlotButton.setEnabled(false);
		buttonPane.add(newPhasePlotButton);

		findHarmonicsButton = new JButton("Find Harmonics");
		findHarmonicsButton
				.addActionListener(createFindHarmonicsButtonHandler());
		findHarmonicsButton.setEnabled(false);
		buttonPane.add(findHarmonicsButton);

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonHandler());
		buttonPane.add(dismissButton);

		return buttonPane;
	}

	/**
	 * Find all harmonics of the specified frequency in the data and return
	 * them, including the fundamental itself.
	 * 
	 * The first harmonic is just the frequency itself. This is a view expressed
	 * by Grant Foster which makes perfect sense to me; it generalises the
	 * notion of "harmonic", not making a special case of the fundamental.
	 * 
	 * Question: Is it an error to find the n-1th and n+1th harmonic in the data
	 * but not the nth? Is this just a by-product of trying to perform floating
	 * point comparison without tolerances?
	 * 
	 * @param freq
	 *            The frequency whose harmonics we seek.
	 * @param data
	 *            The data in which to search; assumed to be frequencies.
	 * @return A list of harmonic objects.
	 */
	protected List<Harmonic> findHarmonics(double freq, List<Double> data) {
		List<Harmonic> harmonics = new ArrayList<Harmonic>();
		harmonics.add(new Harmonic(freq, Harmonic.FUNDAMENTAL));
		int n = Harmonic.FUNDAMENTAL + 1;

		String fmt = NumericPrecisionPrefs.getOtherOutputFormat();

		for (int i = 0; i < data.size(); i++) {
//			if (data.get(i) == freq*n) {
			if (String.format(fmt, data.get(i)).equals(
					String.format(fmt, freq * n))) {
				harmonics.add(new Harmonic(freq * n, n));
				n++;
			}
		}

		return harmonics;
	}

	/**
	 * Sets the enabled state of the new-phase-plot button.
	 * 
	 * @param state
	 *            The desired boolean state.
	 */
	public void setNewPhasePlotButtonState(boolean state) {
		this.newPhasePlotButton.setEnabled(state);
	}

	/**
	 * Sets the enabled state of the find-harmonics button.
	 * 
	 * @param state
	 *            The desired boolean state.
	 */
	public void setFindHarmonicsButtonState(boolean state) {
		this.findHarmonicsButton.setEnabled(state);
	}

	// Dismiss button listener.
	private ActionListener createDismissButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				cleanup();
				dispose();
			}
		};
	}

	// Invoke concrete dialo class's handler when the new-phase-plot button is
	// clicked.
	private ActionListener createNewPhasePlotButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newPhasePlotButtonAction();
			}
		};
	}

	// Invoke concrete dialo class's handler when the find-harmomics button is
	// clicked.
	private ActionListener createFindHarmonicsButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findHarmonicsButtonAction();
			}
		};
	}
}
