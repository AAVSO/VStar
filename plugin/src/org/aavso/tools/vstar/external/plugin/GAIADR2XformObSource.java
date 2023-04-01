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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.external.lib.GaiaObSourceBase;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.util.Pair;

public class GAIADR2XformObSource extends GaiaObSourceBase {

	private final String BASE_URL = "https://gea.esac.esa.int/data-server/data?RETRIEVAL_TYPE=epoch_photometry&FORMAT=CSV&ID=";

	// Create static VeLa filter field here since cannot create it in
	// inner dialog class.
	private static Pair<TextArea, JPanel> velaFilterFieldPanelPair;

	static {
		velaFilterFieldPanelPair = PluginComponentFactory
				.createVeLaFilterPane();
	}
	
	GAIAParameterDialog paramDialog;

	/**
	 * Constructor
	 */
	public GAIADR2XformObSource() {
		super();
	}

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
			paramDialog = new GAIAParameterDialog(isAdditive());
		}
		paramDialog.showDialog();

		if (!paramDialog.isCancelled()) {
			
			paramTransform = paramDialog.isTransform();
			paramIgnoreFlags = paramDialog.isIgnoreFlags();
			paramGaiaRelease = paramDialog.isGaiaDR2() ? GaiaRelease.DR2 : GaiaRelease.DR3;			
			setAdditive(paramDialog.isLoadAdditive());

			try {
				String releaseStr = "&RELEASE=Gaia+DR";  
				switch (paramGaiaRelease) {
					case DR2:
						releaseStr += "2";
						break;
					case DR3:
						releaseStr += "3";
						break;
					default:
						releaseStr = "";
				}
				String url = BASE_URL + paramDialog.getSourceID() + releaseStr;
				urls.add(new URL(url));
			} catch (MalformedURLException e) {
				throw new ObservationReadError("Cannot construct Gaia"
						+ " URL (reason: " + e.getLocalizedMessage() + ")");
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
		return "Gaia DR2/DR3 Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Gaia DR2/DR3 Photometry ...";
	}

	@SuppressWarnings("serial")
	private class GAIAParameterDialog extends AbstractOkCancelDialog {

		private TextField sourceIDField;
		private JCheckBox transformCheckbox;
		private JCheckBox rejectCheckbox;
		private JCheckBox additiveLoadCheckbox;
		private JRadioButton gaiaDR2;
		private JRadioButton gaiaDR3;
		
		private String sourceID;

		/**
		 * Constructor
		 */
		public GAIAParameterDialog(boolean additiveChecked) {
			super("Gaia Load Parameters");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createReleaseRadioButtonsPane(false));
			
			topPane.add(createParameterPane());
			
			topPane.add(createIgnoreRejectFlagsCheckboxPane(false));

			topPane.add(createTransformCheckboxPane(false));

			topPane.add(velaFilterFieldPanelPair.second);

			topPane.add(createAdditiveLoadCheckboxPane(additiveChecked));

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
		}

		private JPanel createParameterPane() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			sourceIDField = new TextField("Gaia source_id");
			panel.add(sourceIDField.getUIComponent());
			//panel.add(Box.createRigidArea(new Dimension(75, 10)));

			return panel;
		}

		private JPanel createAdditiveLoadCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Additive Load"));

			additiveLoadCheckbox = new JCheckBox("Add to current?", checked);
			panel.add(additiveLoadCheckbox);

			return panel;
		}

		private JPanel createIgnoreRejectFlagsCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Flags"));

			rejectCheckbox = new JCheckBox(
					"Ignore rejected_by_photometry, rejected_by_variability?", checked);
			panel.add(rejectCheckbox);

			return panel;
		}
		
		private JPanel createReleaseRadioButtonsPane(boolean dr2) {
			JPanel panel = new JPanel();
			
			panel.setBorder(BorderFactory.createTitledBorder("Gaia data release"));

			ButtonGroup bg = new ButtonGroup();   
			
			gaiaDR2 = new JRadioButton("Gaia DR2", dr2);
			gaiaDR3 = new JRadioButton("Gaia DR3", !dr2);			
			bg.add(gaiaDR2);
			bg.add(gaiaDR3);
			
			panel.add(gaiaDR2);
			panel.add(gaiaDR3);
			
			return panel;
		}
		
		private JPanel createTransformCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Passband Transformation"));

			transformCheckbox = new JCheckBox(
					"Transform Gaia passbands to V,R,I?", checked);
			panel.add(transformCheckbox);

			return panel;
		}

		public String getSourceID() {
			return sourceID;
		}

		/**
		 * Should we ignore rejected_by_photometry and rejected_by_variability flags?
		 * 
		 * @return Whether or not to ignore rejected_by_photometry and rejected_by_variability
		 */
		public boolean isIgnoreFlags() {
			return rejectCheckbox.isSelected();
		}
		
		/**
		 * Use Gaia DR2 instead of Gaia DR3?
		 * 
		 * @return If true, use Gaia DR2 instead of Gaia DR3
		 */
		public boolean isGaiaDR2() {
			return gaiaDR2.isSelected();
		}
		
		/**
		 * Return whether or not the passbands should be transformed.
		 * 
		 * @return Whether or not the passbands are transformed.
		 */
		public boolean isTransform() {
			return transformCheckbox.isSelected();
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
			sourceID = null;

			SwingUtilities.invokeLater( new Runnable() { 
				public void run() {
					sourceIDField.getUIComponent().requestFocusInWindow();
				}
			} );
			
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
			sourceID = sourceIDField.getValue();
			if (sourceID != null) sourceID = sourceID.trim();
			if (sourceID == null || "".equals(sourceID) || !sourceID.matches("[0-9]+")) {
				sourceIDField.getUIComponent().requestFocusInWindow();
				MessageBox.showErrorDialog("Gaia", "Gaia source ID must be numeric");
				return;
			}

			cancelled = false;
			setVisible(false);
			dispose();
		}
	}

}
