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
package org.aavso.tools.vstar.plugin.filter.impl;

import java.util.List;
import java.util.Optional;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.Logic;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaEvalError;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaParseError;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;

/**
 * This filter plug-in allows for the creation of complex VeLa expressions for
 * the purpose of filtering observations.
 */
public class VeLaFilterPlugin extends CustomFilterPluginBase {

	private VeLaInterpreter vela;

	private VeLaObservationFilterDialog dialog;

	public VeLaFilterPlugin() {
		vela = new VeLaInterpreter();
		dialog = new VeLaObservationFilterDialog();
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("VIEW_MENU_VELA_FILTER");
	}

	@Override
	public String getDescription() {
		return "Uses VeLa to permit arbitrarily complex observation filters.";
	}

	@Override
	protected Pair<String, String> filter(List<ValidObservation> obs) {

		String velaFilterExpr = "";

		Pair<String, String> repr = null;

		dialog.showDialog();

		if (!dialog.isCancelled()) {
			velaFilterExpr = dialog.getVeLaExpression();

			try {
				for (ValidObservation ob : obs) {
					vela.pushEnvironment(new VeLaValidObservationEnvironment(ob));

					Optional<Operand> result = vela.program(velaFilterExpr);
					
					boolean does_match = result.isPresent()
							&& result.get().getType() == Type.BOOLEAN
							&& result.get().booleanVal();

					if (does_match) {
						/**
						 * Use logical implication, p => q, where p is the
						 * observation's property and q is the inclusion
						 * property relating to p, to check that our inclusion
						 * criteria still permit a match for this observation.
						 */
						does_match &= Logic.imp(ob.getMagnitude()
								.isFainterThan(), dialog.includeFainterThan());
						does_match &= Logic.imp(ob.isDiscrepant(),
								dialog.includeDiscrepant());
						does_match &= Logic.imp(ob.isExcluded(),
								dialog.includeExcluded());

						if (does_match) {
							addToSubset(ob);
						}
					}

					vela.popEnvironment();
				}

				repr = new Pair<String, String>(dialog.getFilterName(),
						velaFilterExpr);

			} catch (VeLaParseError e) {
				MessageBox.showErrorDialog("Syntax Error",
						e.getLocalizedMessage());
			} catch (VeLaEvalError e) {
				MessageBox.showErrorDialog("Evaluation Error",
						e.getLocalizedMessage());
			} catch (Exception e) {
				MessageBox.showErrorDialog("VeLa Error",
						e.getLocalizedMessage());
			}
		}

		return repr;
	}

	@Override
	public boolean filtersAreParsable() {
		return true;
	}
}
