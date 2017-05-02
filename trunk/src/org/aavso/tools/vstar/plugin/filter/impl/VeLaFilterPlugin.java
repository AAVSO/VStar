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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaEvalError;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaParseError;

/**
 * This filter plug-in allows for the creation of complex VeLa expressions for
 * the purpose of filtering observations.
 */
public class VeLaFilterPlugin extends CustomFilterPluginBase {

	private VeLaInterpreter vela;

	private VeLaObservationFilterDialog dialog;

	public VeLaFilterPlugin() {
		vela = new VeLaInterpreter();
		dialog = new VeLaObservationFilterDialog(vela);
	}

	@Override
	public String getDisplayName() {
		return "VeLa " + LocaleProps.get("VIEW_MENU_FILTER") + "...";
	}

	@Override
	public String getDescription() {
		return "Uses VeLa to permit arbitrarily complex observation filters.";
	}

	@Override
	protected Pair<String, String> filter(List<ValidObservation> obs) {

		String velaFilterExpr = "";

		dialog.showDialog();

		if (!dialog.isCancelled()) {
			velaFilterExpr = dialog.getVeLaExpression();

			Map<String, Operand> environment = new HashMap<String, Operand>();

			try {
				for (ValidObservation ob : obs) {
					environment.clear();

					environment.put("time",
							new Operand(Type.DOUBLE, ob.getJD()));

					Magnitude mag = ob.getMagnitude();
					environment.put("magnitude",
							new Operand(Type.DOUBLE, mag.getMagValue()));
					environment.put("uncertainty",
							new Operand(Type.DOUBLE, mag.getUncertainty()));

					environment.put("band", new Operand(Type.STRING, ob
							.getBand().getShortName()));

					// TODO: create VeLaEnvironment class with get(varname)
					// and exists(varname) and a subclass or realisation for
					// ValidObservation with no need for an intermediate map
					vela.setEnvironment(environment);

					boolean does_match = false;

					does_match = vela.booleanExpression(velaFilterExpr);

					if (does_match) {
						/**
						 * Use logical implication (p => q which is the same as
						 * !p or q, where p is the observation's property and q
						 * is the inclusion property relating to p) to check
						 * that our inclusion criteria still permit a match for
						 * this observation. For example, taking fainter-thans
						 * we have this truth table, where A = ob fainter-than,
						 * B = include fainter-thans , C = result<br/>
						 * --+---+-- <br/>
						 * A | B | C <br/>
						 * --+---+-- <br/>
						 * F | F | T <br/>
						 * F | T | T <br/>
						 * T | F | F <br/>
						 * T | T | T <br/>
						 */
						does_match &= !ob.getMagnitude().isFainterThan()
								|| dialog.includeFainterThan();
						does_match &= !ob.isDiscrepant()
								|| dialog.includeDiscrepant();
						does_match &= !ob.isExcluded()
								|| dialog.includeExcluded();

						if (does_match) {
							addToSubset(ob);
						}
					}
				}
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

		return new Pair<String, String>(dialog.getFilterName(), velaFilterExpr);
	}

	@Override
	public boolean filtersAreParsable() {
		return true;
	}
}
