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
package org.aavso.tools.vstar.ui.pane.list;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
import org.aavso.tools.vstar.util.Logic;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;

/**
 * A VeLa list search pane component.
 */
@SuppressWarnings("serial")
public class VeLaListSearchPane<S extends TableModel> extends JPanel {

	private ValidObservationTableModel model;

	private TableRowSorter<S> rowSorter;
	private RowFilter defaultRowFilter;

	private JButton searchButton;
	private JButton resetButton;
	private JTextArea searchField;
	private JCheckBox includeFainterThanObservationCheckbox;
	private JCheckBox includeDiscrepantObservationCheckbox;
	private JCheckBox includeExcludedObservationCheckbox;

	public VeLaListSearchPane(ValidObservationTableModel model,
			TableRowSorter<S> rowSorter) {
		super();

		this.model = model;

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		// this.setBorder(BorderFactory.createTitledBorder("VeLa Search"));

		this.rowSorter = rowSorter;
		defaultRowFilter = rowSorter.getRowFilter();

		// JPanel includePanel = new JPanel(new FlowLayout());
		JPanel includePanel = new JPanel();
		includePanel
				.setLayout(new BoxLayout(includePanel, BoxLayout.LINE_AXIS));
		includePanel.setBorder(BorderFactory.createTitledBorder("Include"));

		includeFainterThanObservationCheckbox = new JCheckBox("Fainter Than?");
		includePanel.add(includeFainterThanObservationCheckbox);
		includeDiscrepantObservationCheckbox = new JCheckBox("Discrepant?");
		includePanel.add(includeDiscrepantObservationCheckbox);
		includeExcludedObservationCheckbox = new JCheckBox("Excluded?");
		includePanel.add(includeExcludedObservationCheckbox);
		this.add(includePanel);

		searchField = new JTextArea();
		searchField.setToolTipText("Enter a VeLa expression...");
		searchField.setBorder(BorderFactory.createEtchedBorder());
		this.add(searchField);

		JPanel applyResetPane = new JPanel();
		applyResetPane.setLayout(new BoxLayout(applyResetPane,
				BoxLayout.PAGE_AXIS));

		final VeLaListSearchPane<? extends TableModel> velaSearchPane = this;

		searchButton = new JButton("Apply");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRowFilter(new VeLaRowFilter(new VeLaInterpreter(),
						velaSearchPane));
			}
		});
		applyResetPane.add(searchButton);

		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
				restoreDefaultRowFilter();
			}
		});
		applyResetPane.add(resetButton);

		this.add(applyResetPane);
	}

	public String getVeLaExpression() {
		return searchField.getText();
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

	/**
	 * @param defaultRowFilter
	 *            the defaultRowFilter to set
	 */
	public void setDefaultRowFilter(RowFilter defaultRowFilter) {
		this.defaultRowFilter = defaultRowFilter;
	}

	private void restoreDefaultRowFilter() {
		rowSorter.setRowFilter(defaultRowFilter);
	}

	/**
	 * Set the filter on the row sorter.
	 * 
	 * @param rowFilter
	 *            the rowFilter to set
	 */
	public void setRowFilter(RowFilter rowFilter) {
		rowSorter.setRowFilter(rowFilter);
	}

	public void disable() {
		searchField.setText("");
		searchField.setEnabled(false);
		searchButton.setEnabled(false);
		resetButton.setEnabled(false);
	}

	public void enable() {
		searchField.setEnabled(true);
		searchButton.setEnabled(true);
		resetButton.setEnabled(true);
	}

	// VeLa row filter class

	class VeLaRowFilter extends RowFilter<Object, Object> {
		private VeLaInterpreter vela;
		private VeLaListSearchPane<? extends TableModel> searchPane;

		public VeLaRowFilter(VeLaInterpreter vela,
				VeLaListSearchPane<? extends TableModel> searchPane) {
			this.vela = vela;
			this.searchPane = searchPane;
		}

		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends Object, ? extends Object> entry) {
			// Default to inclusion.
			boolean result = true;

			Integer rowIndex = (Integer) entry.getIdentifier();

			if (rowIndex != null) {
				ValidObservation ob = model.getObservations().get(rowIndex);
				vela.pushEnvironment(new VeLaValidObservationEnvironment(ob));
				Optional<Operand> value = vela.program(searchPane
						.getVeLaExpression());
				result = value.isPresent()
						&& value.get().getType() == Type.BOOLEAN
						&& value.get().booleanVal();

				/**
				 * Use logical implication, p => q, where p is the observation's
				 * property and q is the inclusion property relating to p, to
				 * check that our inclusion criteria still permit a match for
				 * this observation.
				 */
				result &= Logic.imp(ob.getMagnitude().isFainterThan(),
						searchPane.includeFainterThan());
				result &= Logic.imp(ob.isDiscrepant(),
						searchPane.includeDiscrepant());
				result &= Logic.imp(ob.isExcluded(),
						searchPane.includeExcluded());

				vela.popEnvironment();
			}

			return result;
		}
	}
}
