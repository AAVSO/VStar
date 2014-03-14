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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.DiscrepantObservationMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This modeless dialog class displays info about a single observation.
 * 
 * It also allows us to change the discrepant status of the observation.
 * 
 * TODO: We should have a pool of these and clear the text for each use since
 * they take awhile to render otherwise and we are likely to create many per
 * session. Or just have one per observation. great way to have a memory leak
 * though.
 */
@SuppressWarnings("serial")
public class ObservationDetailsDialog extends JDialog implements FocusListener {

	private ValidObservation ob;
	private JButton okButton;
	private JCheckBox discrepantCheckBox;

	public ObservationDetailsDialog(ValidObservation ob) {
		super(DocumentManager.findActiveWindow());

		this.ob = ob;

		this.setTitle("Details");
		this.setModal(false);
		this.setSize(200, 200);

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea textArea = new JTextArea(ob.toString());
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		topPane.add(scrollPane);

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		// We currently disable the discrepant checkbox for anything other
		// than raw data mode due to this bug in which a chunk of data
		// disappears after marking a point as discrepant, then unmarking it.
		// Since the cross hair change is reflected in raw data mode also, this
		// is no great user interface problem. The problem should be fixed
		// though.
		// See
		// https://sourceforge.net/tracker/?func=detail&aid=2964224&group_id=263306&atid=1152052
		// for more detail.
		if (Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA) {
			// It doesn't make sense to mark a mean (etc) observation as
			// discrepant since it's a derived (computed) observation.
			if (!ob.getBand().isSynthetic()) {
				JPanel checkBoxPane = new JPanel();
				discrepantCheckBox = new JCheckBox("Discrepant?");
				discrepantCheckBox
						.addActionListener(createDiscrepantCheckBoxHandler());
				discrepantCheckBox.setSelected(ob.isDiscrepant());
				checkBoxPane.add(discrepantCheckBox);
				topPane.add(checkBoxPane, BorderLayout.CENTER);

				Mediator.getInstance().getDiscrepantObservationNotifier()
						.addListener(createDiscrepantChangeListener());
			}
		}

		JPanel buttonPane = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonHandler());
		buttonPane.add(okButton, BorderLayout.CENTER);
		topPane.add(buttonPane);

		this.getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(okButton);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		this.pack();
		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	private void toggleDiscrepantStatus() {
		ob.setDiscrepant(!ob.isDiscrepant());
	}

	/**
	 * OK button handler.
	 */
	private ActionListener createOKButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// Creates a discrepant checkbox handler.
	private ActionListener createDiscrepantCheckBoxHandler() {
		final ObservationDetailsDialog parent = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// We disable discrepant toggling here for now unless we are in
				// raw mode, in case this dialog was opened in raw data mode,
				// but we have since switched to phase plot mode.
				// See
				// https://sourceforge.net/tracker/?func=detail&aid=2964224&group_id=263306&atid=1152052
				// for more detail.
				if (Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA) {
					// Toggle the observation's discrepant status and
					// tell anyone who's listening about the change.
					toggleDiscrepantStatus();
					DiscrepantObservationMessage message = new DiscrepantObservationMessage(
							ob, parent);
					Mediator.getInstance().getDiscrepantObservationNotifier()
							.notifyListeners(message);

					// If the loaded dataset comes from AID, open report-to-HQ
					// dialog.
					Mediator.getInstance().reportDiscrepantObservation(ob,
							parent);
				}
			}
		};
	}

	// TODO: this method is not being invoked when the window regains focus;
	// FIX!
	public void focusGained(FocusEvent e) {
		this.getRootPane().setDefaultButton(okButton);
	}

	public void focusLost(FocusEvent e) {
		// Nothing to do.
	}

	/**
	 * Listen for discrepant observation change notification.
	 */
	protected Listener<DiscrepantObservationMessage> createDiscrepantChangeListener() {
		final ObservationDetailsDialog dialog = this;

		return new Listener<DiscrepantObservationMessage>() {

			// If it was not this object that generated a discrepant observation
			// change message, update the discrepant checkbox.
			@Override
			public void update(DiscrepantObservationMessage info) {
				if (info.getSource() != dialog
						&& info.getObservation() == dialog.ob) {
					boolean isDiscrepant = !discrepantCheckBox.isSelected();
					discrepantCheckBox.setSelected(isDiscrepant);
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}