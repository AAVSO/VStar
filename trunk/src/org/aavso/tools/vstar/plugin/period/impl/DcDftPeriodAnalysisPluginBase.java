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
package org.aavso.tools.vstar.plugin.period.impl;

import javax.swing.JDialog;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DResultDialog;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * Date Compensated Discrete Fourier Transform period analysis plugin.
 */
abstract public class DcDftPeriodAnalysisPluginBase extends
		PeriodAnalysisPluginBase {

	protected NewStarMessage newStarMessage;
	protected TSDcDft periodAnalysisAlgorithm;

	/**
	 * Constructor.
	 */
	public DcDftPeriodAnalysisPluginBase() {
		super();
	}

	// ** Mandatory interface methods **

	@Override
	public String getDisplayName() {
		return LocaleProps.get("DCDFT_DISPLAY_NAME");
	}

	@Override
	public String getDescription() {
		return LocaleProps.get("DCDFT_DESC");
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.PluginBase#getGroup()
	 */
	@Override
	public String getGroup() {
		return "DC DFT";
	}

	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {

		return new PeriodAnalysis2DResultDialog(LocaleProps
				.get("DCDFT_RESULT_TITLE")
				+ " " + newStarMessage.getStarInfo().getDesignation(), "("
				+ LocaleProps.get("SERIES") + ": "
				+ sourceSeriesType.getDescription() + ")",
				periodAnalysisAlgorithm);
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase#interrupt()
	 */
	@Override
	public void interrupt() {
		if (periodAnalysisAlgorithm != null) {
			periodAnalysisAlgorithm.interrupt();
		}
	}

	// ** Mandatory message listeners. **

	@Override
	protected void newStarAction(NewStarMessage message) {
		newStarMessage = message;
		reset();
	}
}
