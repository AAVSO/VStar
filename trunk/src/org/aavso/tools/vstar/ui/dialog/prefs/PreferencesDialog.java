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

import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;

/**
 * Preferences Dialog.
 * 
 * TODO: Use Java 1.4 Preferences as model. Is there corresponding GUI rendering
 * code?
 */
public class PreferencesDialog extends AbstractOkCancelDialog {

	private SeriesColorSelectionPane seriesColorPane;

	/**
	 * Constructor.
	 */
	private PreferencesDialog() {
		super("Preferences");

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createTabs());
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
	}

	// TODO: reuse old tabbed pane class?
	private JTabbedPane createTabs() {
		JTabbedPane tabs = new JTabbedPane();

		this.seriesColorPane = new SeriesColorSelectionPane();
		tabs.addTab("Series Colors", this.seriesColorPane);
		tabs.setToolTipText("Change Series Colors");

		return tabs;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	protected void cancelAction() {
		// Nothing to do.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	protected void okAction() {
		seriesColorPane.updateSeriesColors();
		
		this.setVisible(false);
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#reset()
	 */
	protected void reset() {
		seriesColorPane.reset();
	}

	/**
	 * Singleton.
	 */
	private static PreferencesDialog instance = new PreferencesDialog();

	public static PreferencesDialog getInstance() {
		return instance;
	}
}
