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
package org.aavso.tools.vstar.ui.pane.list;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.IOrderedObservationSource;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.IFilterDescription;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.MultipleObservationSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.InvalidObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
import org.aavso.tools.vstar.util.comparator.DoubleAsStringComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a GUI component that renders information about
 * observation data, including one or both of valid and invalid observation
 * data. If both are present, they are rendered as tables in a vertical split
 * pane. Otherwise, a single table will appear.
 */
@SuppressWarnings("serial")
public class ObservationListPane extends JPanel implements
		ListSelectionListener {

	private JTable validDataTable;
	private JTable invalidDataTable;
	private ValidObservationTableModel validDataModel;
	private TableRowSorter<ValidObservationTableModel> rowSorter;
	private VisibleSeriesRowFilter rowFilter;
	private RowFilter<IOrderedObservationSource, Integer> currFilter;
	private JButton selectAllButton;
	private JButton createFilterButton;

	private ListSearchPane<ValidObservationTableModel> searchPanel;
	private VeLaListSearchPane<ValidObservationTableModel> velaSearchPanel;

	private Set<ValidObservation> selectedObs;

	private ValidObservation lastObSelected = null;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the table.
	 * @param validDataModel
	 *            A table data model that encapsulates valid observations.
	 * @param invalidDataModel
	 *            A table data model that encapsulates invalid observations.
	 * @param enableAutoResize
	 *            Enable auto-resize of columns? If true, we won't get a
	 *            horizontal scrollbar for valid observation table. The source
	 *            of column information for the table.
	 * @param analysisType
	 *            The analysis type (raw, phase) under which this table was
	 *            created.
	 */
	public ObservationListPane(String title,
			ValidObservationTableModel validDataModel,
			InvalidObservationTableModel invalidDataModel,
			boolean enableAutoResize, Set<SeriesType> initialVisibleSeries,
			AnalysisType analysisType) {

		super(new BorderLayout());

		selectedObs = new LinkedHashSet<ValidObservation>();

		JScrollPane validDataScrollPane = null;

		if (validDataModel != null) {
			this.validDataModel = validDataModel;

			validDataTable = new JTable(validDataModel);
			if (!enableAutoResize) {
				// Ensure we get a horizontal scrollbar if necessary rather than
				// trying to cram all the columns into the visible pane.
				validDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}

			// Set selection mode to be row-only.
			// These appear to be the defaults anyway.
			validDataTable.setColumnSelectionAllowed(false);
			validDataTable.setRowSelectionAllowed(true);

			// Enable table sorting by clicking on a column.
			// We need to treat JD, magnitude, and uncertainty as doubles.
			rowSorter = new TableRowSorter<ValidObservationTableModel>(
					validDataModel);
			int jdColIndex = validDataModel.getColumnInfoSource()
					.getColumnIndexByName("Julian Day");
			rowSorter.setComparator(jdColIndex, new DoubleAsStringComparator());
			int magColIndex = validDataModel.getColumnInfoSource()
					.getColumnIndexByName("Magnitude");
			rowSorter
					.setComparator(magColIndex, new DoubleAsStringComparator());
			int uncertaintyColIndex = validDataModel.getColumnInfoSource()
					.getColumnIndexByName("Uncertainty");
			rowSorter.setComparator(uncertaintyColIndex,
					new DoubleAsStringComparator());
			validDataTable.setRowSorter(rowSorter);

			// Add a row filter that shows data from series that are visible in
			// the main plot.
			rowFilter = new VisibleSeriesRowFilter(validDataModel,
					initialVisibleSeries, analysisType);
			rowSorter.setRowFilter(rowFilter);

			validDataScrollPane = new JScrollPane(validDataTable);
		}

		JScrollPane invalidDataScrollPane = null;

		if (invalidDataModel != null) {
			invalidDataTable = new JTable(invalidDataModel);

			// Ensure we get a horizontal scrollbar if necessary rather than
			// trying to cram all the columns into the visible pane. This is
			// particularly pertinent to the invalid data table since one of
			// its columns contains the original line in the case of a file
			// source.
			invalidDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			// Set the columns containing the observation and error to be
			// greater than the default width of the others.
			TableColumnModel colModel = invalidDataTable.getColumnModel();
			int totalWidth = colModel.getTotalColumnWidth();
			colModel.getColumn(1).setPreferredWidth((int) (totalWidth * 2.5));
			colModel.getColumn(2).setPreferredWidth((int) (totalWidth * 2));

			// Enable table sorting by clicking on a column.
			invalidDataTable.setAutoCreateRowSorter(true);

			invalidDataScrollPane = new JScrollPane(invalidDataTable);
		}

		// In the presence of both valid and invalid data, we put
		// them into a split pane.
		if (validDataScrollPane != null && invalidDataScrollPane != null) {
			JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitter.setToolTipText("Valid and invalid data");
			splitter.setTopComponent(validDataScrollPane);
			splitter.setBottomComponent(invalidDataScrollPane);
			splitter.setResizeWeight(0.5);
			// splitter.setBorder(BorderFactory.createTitledBorder(title));
			this.add(splitter, BorderLayout.CENTER);
		} else if (validDataScrollPane != null) {
			// Just valid data.
			// validDataScrollPane.setBorder(BorderFactory.createTitledBorder(title));
			this.add(validDataScrollPane, BorderLayout.CENTER);
		} else if (invalidDataScrollPane != null) {
			// Just invalid data.
			// invalidDataScrollPane.setBorder(BorderFactory.createTitledBorder(title));
			this.add(invalidDataScrollPane, BorderLayout.CENTER);
		} else {
			// We have no data at all. Let's say so.
			JLabel label = new JLabel("There is no data to be displayed");
			label.setHorizontalAlignment(JLabel.CENTER);
			this.setLayout(new BorderLayout());
			this.add(label, BorderLayout.CENTER);
		}

		this.add(createControlPanel(), BorderLayout.NORTH);

		// Listen for observation selection events. Notice that this class
		// also generates these, but ignores them if sent by itself.
		Mediator.getInstance().getObservationSelectionNotifier()
				.addListener(createObservationSelectionListener());

		// List row selection handling.
		this.validDataTable.getSelectionModel().addListSelectionListener(this);
	}

	/**
	 * Create a control panel for the table.
	 */
	private JPanel createControlPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder());

		// A checkbox to determine whether to display all the data in the table.
		JCheckBox allDataCheckBox = new JCheckBox("Show all data?");
		allDataCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox checkBox = (JCheckBox) e.getSource();
				if (checkBox.isSelected()) {
					// Show all data, no filtering, no search permitted.
					currFilter = null;
					searchPanel.disable();
					velaSearchPanel.disable();
				} else {
					// Filter the data displayed, permit search.
					currFilter = rowFilter;
					searchPanel.enable();
					velaSearchPanel.enable();
				}
				rowSorter.setRowFilter(currFilter);
			}
		});
		allDataCheckBox.setSelected(false);
		panel.add(allDataCheckBox);

		panel.add(Box.createRigidArea(new Dimension(10, 10)));

		// Selectable search pane: pattern and VeLa search
		JPanel selectableSearchPanes = new JPanel(new CardLayout());

		searchPanel = new ListSearchPane<ValidObservationTableModel>(
				validDataModel, rowSorter);
		selectableSearchPanes.add(searchPanel, "Regex");

		velaSearchPanel = new VeLaListSearchPane<ValidObservationTableModel>(
				validDataModel, rowSorter);
		selectableSearchPanes.add(velaSearchPanel, "VeLa");

		JRadioButton patternSearchSelector = new JRadioButton("Regular Expression");
		patternSearchSelector.setSelected(true);
		patternSearchSelector.addActionListener(e -> {
			CardLayout cl = (CardLayout) selectableSearchPanes.getLayout();
			cl.show(selectableSearchPanes, "Regex");
		});

		JRadioButton velaSearchSelector = new JRadioButton("VeLa Expression");
		velaSearchSelector.addActionListener(e -> {
			CardLayout cl = (CardLayout) selectableSearchPanes.getLayout();
			cl.show(selectableSearchPanes, "VeLa");
		});

		ButtonGroup searchSelectionRadioButtons = new ButtonGroup();
		searchSelectionRadioButtons.add(patternSearchSelector);
		searchSelectionRadioButtons.add(velaSearchSelector);

		JPanel searchSelectorPane = new JPanel();
		searchSelectorPane.setLayout(new BoxLayout(searchSelectorPane,
				BoxLayout.LINE_AXIS));
		searchSelectorPane.add(patternSearchSelector);
		searchSelectorPane.add(velaSearchSelector);

		JPanel searchPane = new JPanel();
		searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.PAGE_AXIS));
		searchPane.setBorder(BorderFactory.createTitledBorder("Search"));
		searchPane.add(searchSelectorPane);
		searchPane.add(selectableSearchPanes);

		panel.add(searchPane);

		final JPanel parent = this;

		selectAllButton = new JButton(LocaleProps.get("SELECT_ALL"));
		selectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});
		panel.add(selectAllButton);

		createFilterButton = new JButton(
				LocaleProps.get("CREATE_SELECTION_FILTER"));
		createFilterButton.setEnabled(false);
		createFilterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Request a name for the filter.
				String defaultName = Mediator.getInstance()
						.getDocumentManager().getNextUntitledFilterName();
				List<ITextComponent<String>> fields = new ArrayList<ITextComponent<String>>();
				fields.add(new TextField("Name", defaultName, false, false));
				final TextDialog nameDlg = new TextDialog("Filter Name", fields);

				// Create an observation filter message and notify listeners.
				IFilterDescription desc = new IFilterDescription() {

					@Override
					public boolean isParsable() {
						return false;
					}

					@Override
					public String getFilterName() {
						return nameDlg.getTextStrings().get(0);
					}

					@Override
					public String getFilterDescription() {
						// Return a machine-readable (able to be parsed)
						// representation.
						StringBuffer buf = new StringBuffer();

						int i = 0;
						for (ValidObservation ob : selectedObs) {
							String jdStr = NumericPrecisionPrefs.formatTime(ob
									.getJD());
							buf.append("JD = " + jdStr);
							if (i < selectedObs.size() - 1) {
								buf.append(" AND\n");
								i++;
							}
						}

						return buf.toString();
					}
				};

				FilteredObservationMessage message = new FilteredObservationMessage(
						parent, desc, new LinkedHashSet<ValidObservation>(
								selectedObs));

				Mediator.getInstance().getFilteredObservationNotifier()
						.notifyListeners(message);
			}
		});
		panel.add(createFilterButton);

		return panel;
	}

	/**
	 * Select all observations visible in the list.
	 */
	public void selectAll() {
		validDataTable.selectAll();
		ListSelectionModel selModel = validDataTable.getSelectionModel();
		valueChanged(new ListSelectionEvent(selModel,
				selModel.getMinSelectionIndex(),
				selModel.getMaxSelectionIndex(), false));
	}

	/**
	 * @return the validDataTable
	 */
	public JTable getValidDataTable() {
		return validDataTable;
	}

	/**
	 * @return the invalidDataTable
	 */
	public JTable getInvalidDataTable() {
		return invalidDataTable;
	}

	/**
	 * @return the lastObSelected
	 */
	public ValidObservation getLastObSelected() {
		return lastObSelected;
	}

	/**
	 * Retrieve the valid observations that are currently in the table's view.
	 * 
	 * @return The observation list.
	 */
	public List<ValidObservation> getObservationsInView() {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();

		// TODO: Is there some other way of getting the in-view objects? Ask the
		// rowfilter for or to keep a list of the current indices?
		validDataTable.selectAll();

		for (int row : validDataTable.getSelectedRows()) {
			row = validDataTable.convertRowIndexToModel(row);
			obs.add(validDataModel.getObservations().get(row));
		}

		return obs;
	}

	// Returns an observation selection listener.
	protected Listener<ObservationSelectionMessage> createObservationSelectionListener() {
		final JPanel parent = this;
		return new Listener<ObservationSelectionMessage>() {
			public void update(ObservationSelectionMessage message) {
				if (message.getSource() != parent) {
					ValidObservation ob = message.getObservation();
					Integer rowIndex = validDataModel
							.getRowIndexFromObservation(ob);
					if (rowIndex != null && rowIndex >= 0) {
						try {
							// Convert to view index!
							rowIndex = validDataTable
									.convertRowIndexToView(rowIndex);

							if (rowIndex >= 0
									&& rowIndex < validDataTable.getRowCount()) {

								// Scroll to an arbitrary column (zeroth) within
								// the selected row, then select that row.
								// Assumption: we are specifying the zeroth cell
								// within row i as an x,y coordinate relative to
								// the top of the table pane.
								// Note that we could call this on the scroll
								// pane, which would then forward the request to
								// the table pane anyway.
								int colWidth = (int) validDataTable
										.getCellRect(rowIndex, 0, true)
										.getWidth();
								int rowHeight = validDataTable
										.getRowHeight(rowIndex);
								validDataTable
										.scrollRectToVisible(new Rectangle(
												colWidth, rowHeight * rowIndex,
												colWidth, rowHeight));

								try {
									validDataTable.setRowSelectionInterval(
											rowIndex, rowIndex);
								} catch (IllegalArgumentException e) {
									// We ignore this since this is entirely
									// possible when filtering is enabled.
								}

								lastObSelected = ob;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							String msg = "Could not select row with index "
									+ rowIndex
									+ " (table model: "
									+ validDataModel.getColumnInfoSource()
											.getClass().getSimpleName() + ")";
							MessageBox.showMessageDialog(Mediator.getUI()
									.getComponent(),
									"Observation List Index Error", msg);
						}
					}
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	/**
	 * We send an observation selection message when the value or values have
	 * "settled". This event could be consumed by other views such as plots or
	 * undo managers.
	 * 
	 * @param e
	 *            The list selection event.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == validDataTable.getSelectionModel()
				&& validDataTable.getRowSelectionAllowed()
				&& !e.getValueIsAdjusting()) {

			int[] rows = validDataTable.getSelectedRows();

			if (rows.length > 1) {
				// This is a multiple observation selection.
				List<ValidObservation> obs = new ArrayList<ValidObservation>();
				for (int row : rows) {
					row = validDataTable.convertRowIndexToModel(row);
					ValidObservation ob = validDataModel.getObservations().get(
							row);
					obs.add(ob);
				}
				MultipleObservationSelectionMessage message = new MultipleObservationSelectionMessage(
						obs, this);

				Mediator.getInstance()
						.getMultipleObservationSelectionNotifier()
						.notifyListeners(message);

				selectedObs.clear();
				selectedObs.addAll(obs);
				createFilterButton.setEnabled(true);
			} else {
				// This is a single observation selection.
				int row = validDataTable.getSelectedRow();

				if (row >= 0) {
					row = validDataTable.convertRowIndexToModel(row);
					ValidObservation ob = validDataModel.getObservations().get(
							row);
					lastObSelected = ob;
					ObservationSelectionMessage message = new ObservationSelectionMessage(
							ob, this);
					Mediator.getInstance().getObservationSelectionNotifier()
							.notifyListeners(message);

					selectedObs.clear();
					selectedObs.add(ob);
					createFilterButton.setEnabled(true);
				} else {
					createFilterButton.setEnabled(false);
				}
			}
		}
	}
}