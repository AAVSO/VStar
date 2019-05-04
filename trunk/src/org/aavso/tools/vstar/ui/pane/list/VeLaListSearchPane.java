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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
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
	private JTextField searchField;

	public VeLaListSearchPane(ValidObservationTableModel model,
			TableRowSorter<S> rowSorter) {
		super();

		this.model = model;

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		//this.setBorder(BorderFactory.createTitledBorder("VeLa Search"));

		this.rowSorter = rowSorter;
		defaultRowFilter = rowSorter.getRowFilter();

		searchField = new JTextField();
		searchField.setToolTipText("Enter a VeLa expression...");
		this.add(searchField);

		searchButton = new JButton("Apply");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRowFilter(new VeLaRowFilter(new VeLaInterpreter(),
						searchField.getText()));
			}
		});
		this.add(searchButton);

		// TODO: just need one Reset button in observation list pane so move it
		// there
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
				restoreDefaultRowFilter();
			}
		});
		this.add(resetButton);
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
		private String prog;

		public VeLaRowFilter(VeLaInterpreter vela, String prog) {
			this.vela = vela;
			this.prog = prog;
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
				Optional<Operand> value = vela.program(prog);
				result = value.isPresent()
						&& value.get().getType() == Type.BOOLEAN
						&& value.get().booleanVal();
				vela.popEnvironment();
			}

			return result;
		}
	}

}
