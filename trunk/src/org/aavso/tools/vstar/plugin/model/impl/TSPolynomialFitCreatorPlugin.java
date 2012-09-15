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
package org.aavso.tools.vstar.plugin.model.impl;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.PolynomialDegreeDialog;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.IPolynomialFitter;
import org.aavso.tools.vstar.util.model.TSPolynomialFitter;

/**
 * A polynomial fitter model creator plugin that is logically equivalent to the
 * AAVSO TS Fortran program's polynomial fitter algorithm.
 */
public class TSPolynomialFitCreatorPlugin extends ModelCreatorPluginBase {

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		IModel model = null;

		IPolynomialFitter polynomialFitter = new TSPolynomialFitter(obs);

		int minDegree = polynomialFitter.getMinDegree();
		int maxDegree = polynomialFitter.getMaxDegree();

		PolynomialDegreeDialog dialog = new PolynomialDegreeDialog(minDegree,
				maxDegree);

		if (!dialog.isCancelled()) {
			polynomialFitter.setDegree(dialog.getDegree());
			model = polynomialFitter;
		}

		return model;
	}

	@Override
	public String getDescription() {
		return LocaleProps.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
	}
}
