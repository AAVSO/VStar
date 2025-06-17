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

/**
 * See https://www.cosmos.esa.int/web/gaia-users/archive/programmatic-access#Sect_1_ss1.1
 * 
 * An example of the data file can be retrieved with the following URL:
 * https://gea.esac.esa.int/data-server/data?RETRIEVAL_TYPE=epoch_photometry&FORMAT=CSV&ID=1951343009975999744&RELEASE=Gaia+DR3
 * 
 */
package org.aavso.tools.vstar.external.plugin;

import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.external.lib.GaiaObSourceBase;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;

public class GAIADR2XformFileObSource extends GaiaObSourceBase {

	/**
	 * Constructor
	 */
	public GAIADR2XformFileObSource() {
		super();
		paramGaiaRelease = GAIADR2XformObSource.GaiaRelease.UNKNOWN;
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Gaia DR2/DR3 Photometry File Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Gaia DR2/DR3 Photometry File...";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "GAIAPluginDoc.pdf";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getObservationRetriever()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		GAIAParameterFileDialog paramDialog = new GAIAParameterFileDialog();
		if (paramDialog.isCancelled()) {
			return null;
		}
		paramIgnoreFlags = paramDialog.isIgnoreFlags();
		paramTransform = paramDialog.isTransform();
		
		return super.getObservationRetriever();
	}
	
	
	@SuppressWarnings("serial")
	private class GAIAParameterFileDialog extends AbstractOkCancelDialog {

		private JCheckBox transformCheckbox;
		private JCheckBox rejectCheckbox;

		/**
		 * Constructor
		 */
		public GAIAParameterFileDialog() {
			super("Gaia Load Parameters [Version " + GAIA_OB_SOURCE_VERSION + "]");

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createIgnoreRejectFlagsCheckboxPane(paramIgnoreFlags));

			topPane.add(createTransformCheckboxPane(paramTransform));

			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}

		private JPanel createIgnoreRejectFlagsCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Flags"));

			rejectCheckbox = new JCheckBox(
					"Ignore rejected_by_photometry, rejected_by_variability?", checked);
			panel.add(rejectCheckbox);

			return panel;
		}
		
		private JPanel createTransformCheckboxPane(boolean checked) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Passband Transformation"));

			transformCheckbox = new JCheckBox(
					"Transform Gaia passbands?", checked);
			panel.add(transformCheckbox);

			return panel;
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
		 * Return whether or not the passbands should be transformed.
		 * 
		 * @return Whether or not the passbands are transformed.
		 */
		public boolean isTransform() {
			return transformCheckbox.isSelected();
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
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}
	
}
