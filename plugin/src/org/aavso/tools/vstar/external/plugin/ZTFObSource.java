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
package org.aavso.tools.vstar.external.plugin;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.external.lib.ZTFObSourceBase;
import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSource;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * 
 * @author max (PMAK)
 * 
 */
public class ZTFObSource extends ZTFObSourceBase {

	private final String BASE_URL = "https://irsa.ipac.caltech.edu/cgi-bin/ZTF/nph_light_curves?FORMAT=TSV&";
	
	// Create static VeLa filter field here since cannot create it in
	// inner dialog class.
	private static Pair<TextArea, JPanel> velaFilterFieldPanelPair;

	static {
		velaFilterFieldPanelPair = PluginComponentFactory.createVeLaFilterPane();
	}
	
	private ZTFParameterDialog paramDialog;

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.URL;
	}
	
	@Override
	public List<URL> getURLs() throws Exception {
		List<URL> urls = new ArrayList<URL>();

		if (paramDialog == null) {
			paramDialog = new ZTFParameterDialog(isAdditive());
		}
		paramDialog.showDialog();
		if (!paramDialog.isCancelled()) {
			setAdditive(paramDialog.isLoadAdditive());
			
			String url = paramDialog.getZtfURL();
			
			try {
				urls.add(new URL(url));
			} catch (MalformedURLException e) {
				throw new ObservationReadError("Cannot construct ZTF URL (reason: " + e.getLocalizedMessage() + ")");
			}
			
			setVelaFilterStr(paramDialog.getVelaFilterStr());
		} else {
			throw new CancellationException();
		}
		return urls;
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "ZTF Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from ZTF Photometry ...";
	}

	@SuppressWarnings("serial")
	class ZTFParameterDialog extends AbstractOkCancelDialog {

		private TextField objectIDField;
		private TextField objectRAField;
		private TextField objectDecField;
		private TextField objectRadiusField;
		private TextField objectVSXNameField;
		private JCheckBox additiveLoadCheckbox;
		private JCheckBox catflagsZeroCheckbox;

		private JTabbedPane searchParamPane;
		private JTabbedPane searchParamPane2;
		
		private String ztfURL = null;
		
		private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
		
		public ZTFParameterDialog(boolean additiveChecked) {
			super("ZTF Photometry");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			createParameterPane();
			topPane.add(searchParamPane);

			searchParamPane.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							searchParamPaneUpdateFocus();
						}
					} );
			
			searchParamPane2.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							searchParamPane2UpdateFocus();
						}
					} );

			topPane.add(createCatflagsZeroCheckboxPane());			
			
			topPane.add(createGetURLPane());
			
			topPane.add(Box.createRigidArea(new Dimension(400, 20)));
			
			topPane.add(velaFilterFieldPanelPair.second);

			topPane.add(createAdditiveLoadCheckboxPane(additiveChecked));
			
			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
		}
		
		private void searchParamPaneUpdateFocus() {
			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					switch (searchParamPane.getSelectedIndex()) {
						case 0:
							searchParamPane2UpdateFocus();
							break;
						case 1:
							objectIDField.getUIComponent().requestFocusInWindow();								
							break;
						default:
							break;
					}
				}
			} );
		}
		

		private void searchParamPane2UpdateFocus() {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					switch (searchParamPane2.getSelectedIndex()) {
						case 0:
							objectRAField.getUIComponent().requestFocusInWindow();
							break;
						case 1:
							objectVSXNameField.getUIComponent().requestFocusInWindow();
							break;
						default:
							break;
					}
				}
			} );
		}

		
		private void createParameterPane() {
			searchParamPane = new JTabbedPane();
			
			searchParamPane2 = new JTabbedPane();
			
			JPanel panelCoord = new JPanel();
			panelCoord.setLayout(new BoxLayout(panelCoord, BoxLayout.LINE_AXIS));
			objectRAField = new TextField("RA (degrees)", "0");
			panelCoord.add(objectRAField.getUIComponent());
			objectDecField = new TextField("Dec (degrees)", "0");
			panelCoord.add(objectDecField.getUIComponent());
			
			searchParamPane2.addTab("Coordinates", null, panelCoord, "Search by coordinates");
			
			JPanel panelVSX = new JPanel();
			panelVSX.setLayout(new BoxLayout(panelVSX, BoxLayout.LINE_AXIS));
			objectVSXNameField = new TextField("VSX name", "");
			panelVSX.add(objectVSXNameField.getUIComponent());
			
			searchParamPane2.addTab("VSX", null, panelVSX, "Search by VSX name");
			
			JPanel panelCoordAndVSX = new JPanel();
			panelCoordAndVSX.setLayout(new BoxLayout(panelCoordAndVSX, BoxLayout.PAGE_AXIS));
			panelCoordAndVSX.add(searchParamPane2);
			
			objectRadiusField = new TextField("Radius (degrees)", String.format(Locale.getDefault(), "%.4f", 0.0004));
			panelCoordAndVSX.add(objectRadiusField.getUIComponent());
			
			searchParamPane.addTab("Coordinates or VSX name", panelCoordAndVSX);
			
			JPanel panelID = new JPanel();
			panelID.setLayout(new BoxLayout(panelID, BoxLayout.PAGE_AXIS));
			panelID.add(Box.createRigidArea(new Dimension(100, 40)));
			objectIDField = new TextField("ZTF object ID", "");
			panelID.add(objectIDField.getUIComponent());
			panelID.add(Box.createRigidArea(new Dimension(100, 40)));
			
			searchParamPane.addTab("Object ID", null, panelID, "Search by ZTF object identifier");
		}

		private JPanel createCatflagsZeroCheckboxPane() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Catflags"));

			catflagsZeroCheckbox = new JCheckBox("ZTF data with catflags 0 only?", true);
			panel.add(catflagsZeroCheckbox);

			return panel;
		}

		private JPanel createGetURLPane() {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("URL"));

			JButton getUrlButton = new JButton("Copy ZTF URL to clipboard");
			
			getUrlButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String url = constructURL();
							if (url != null) {
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(new StringSelection(url), null);
								MessageBox.showMessageDialog("ZTF", "ZTF URL has been copied to the clipboard");
							}
						}
					}
				);
			
			panel.add(getUrlButton);

			return panel;
		}
		
		private JPanel createAdditiveLoadCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?", checked);
			panel.add(additiveLoadCheckbox);

			return panel;
		}
		
		public String constructURL() {
			String url = null;
			if (searchParamPane.getSelectedIndex() == 1) {
				String objectID = objectIDField.getValue();
				if (objectID != null) objectID = objectID.trim();
				if (objectID == null || "".equals(objectID) || !objectID.matches("[0-9]+")) {
					objectIDField.getUIComponent().requestFocusInWindow();
					MessageBox.showErrorDialog("ZTF", "ZTF object ID must be numeric");
					return null;
				}
				url = BASE_URL + "ID=" + objectID;
			} else {
				Double objectRA = null;
				Double objectDec = null;
				Double objectRadius = getDouble(objectRadiusField, 0, 0.005, true, true, "Radius must be >= 0 and <= 0.005");
				if (objectRadius == null) {
					return null;
				}
				if (searchParamPane2.getSelectedIndex() == 0) {
					objectRA = getDouble(objectRAField, 0, 360, true, false, "RA must be >= 0 and < 360");
					if (objectRA == null) {
						return null;
					}
					objectDec = getDouble(objectDecField, -90, 90, true, true, "Dec must be >= -90 and <= 90");
					if (objectDec == null) {
						return null;
					}
				}
				else {
					String vsxName = objectVSXNameField.getValue();
					if (vsxName != null) vsxName = vsxName.trim();
					if (vsxName == null || "".equals(vsxName)) {
						objectVSXNameField.getUIComponent().requestFocusInWindow();
						MessageBox.showErrorDialog("VSX", "VSX name must be specified");
						return null;
					}
					
					try {
						StarInfo starInfo = ResolveVSXidentifier(vsxName);
						objectRA = starInfo.getRA().toDegrees();
						objectDec = starInfo.getDec().toDegrees();
					} catch (Exception e) {
						objectVSXNameField.getUIComponent().requestFocusInWindow();
						MessageBox.showErrorDialog("VSX", "Cannot resolve the VSX identifier.\nError message:\n" + 
								e.getLocalizedMessage());
						return null;
					}
				}

				url = BASE_URL + "POS=CIRCLE%20" + String.format(Locale.ENGLISH, "%.5f%%20%.5f%%20%.5f", objectRA, objectDec, objectRadius);  

				if (catflagsZeroCheckbox.isSelected())
					url += "&BAD_CATFLAGS_MASK=65535";
			}
			return url;			
		}

		public String getZtfURL() {
			return ztfURL;
		}
		
		/**
		 * Return whether or not the load is additive.
		 * 
		 * @return Whether or not the load is additive.
		 */
		public boolean isLoadAdditive() {
			return additiveLoadCheckbox.isSelected();
		}

		/**
		 * @return The VeLa filter string.
		 */
		public String getVelaFilterStr() {
			return velaFilterFieldPanelPair.first.getValue().trim();
		}

		@Override
		public void showDialog() {
			ztfURL = null;
			searchParamPaneUpdateFocus();			
			super.showDialog();			
		}
		
		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
		 */
		@Override
		protected void cancelAction() {
			// Nothing to do.
		}
		
		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
		 */
		@Override
		protected void okAction() {
			ztfURL = constructURL();
			if (ztfURL == null)
				return;
			
			cancelled = false;
			setVisible(false);
			dispose();
		}
		
		private StarInfo ResolveVSXidentifier(String id) {
			Cursor defaultCursor = getCursor();
			setCursor(waitCursor);
			try {
				VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
				return infoSrc.getStarByName(id);
			} finally {
				setCursor(defaultCursor);
			}
		}
		
		private Double getDouble(TextField f, double min, double max, boolean min_inclusive, boolean max_inclusive, String errorMessage) {
			Double v;
			try {
				v = NumberParser.parseDouble(f.getValue());
			} catch (Exception e) {
				v = null;
				errorMessage = e.getLocalizedMessage();
			}
			if (v != null && (v > min || min_inclusive && v == min) && (v < max || max_inclusive && v == max)) {
				return v;
			} else {
				f.getUIComponent().requestFocusInWindow();
				MessageBox.showErrorDialog(f.getName(), errorMessage);
				return null;
			}
		}

	}

}
