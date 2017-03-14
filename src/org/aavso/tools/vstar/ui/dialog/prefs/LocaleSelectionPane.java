/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2017  AAVSO (http://www.aavso.org/)
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * Locale selection panel.
 */
@SuppressWarnings("serial")
public class LocaleSelectionPane extends JPanel implements IPreferenceComponent {

	private JComboBox<Locale> localeSelector;
	private Locale currentLocale;

	/**
	 * Constructor.
	 */
	public LocaleSelectionPane() {
		super();

		JPanel seriesSizePane = new JPanel();
		seriesSizePane.setLayout(new BoxLayout(seriesSizePane,
				BoxLayout.PAGE_AXIS));
		seriesSizePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Map<String, Locale> localeMap = new TreeMap<String, Locale>();
		for (Locale locale : Locale.getAvailableLocales()) {
			localeMap.put(locale.toLanguageTag(), locale);
		}
		
		List<Locale> sortedLocales = new ArrayList<Locale>();
		for (String languageTag : localeMap.keySet()) {
			sortedLocales.add(localeMap.get(languageTag));
		}
		
		localeSelector = new JComboBox<Locale>(sortedLocales.toArray(new Locale[0]));
		localeSelector.setToolTipText("Select Locale");
		localeSelector.addActionListener(createLocaleSelectorActionListener());
		localeSelector.setBorder(BorderFactory.createTitledBorder("Locale"));
		seriesSizePane.add(localeSelector);

		seriesSizePane.add(Box.createRigidArea(new Dimension(10, 10)));

		seriesSizePane.add(createButtonPane());

		this.add(seriesSizePane);
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Default Locale");
		setDefaultsButton
				.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}

	// Series selector action listener creator.
	private ActionListener createLocaleSelectorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentLocale = (Locale) localeSelector.getSelectedItem();
			}
		};
	}

	// Set defaults action button listener.
	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LocaleProps.setDefaultLocalePref();
				reset();
			}
		};
	}

	// Set apply button listener.
	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}

	/**
	 * Updates the locale.
	 */
	@Override
	public void update() {
		LocaleProps.setLocalePref(currentLocale);
	}

	/**
	 * Prepare this pane for use by resetting whatever needs to be, in
	 * particular, updating the selector with the current locale preferences.
	 */
	@Override
	public void reset() {
		localeSelector.setSelectedItem((Locale) LocaleProps.getLocale());
	}
}