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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationTransformerPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;
import org.aavso.tools.vstar.ui.vela.VeLaDialog;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;

/**
 * This plugin allows a VeLa function to be used for observation transformation.
 */
public class VeLaObservationTransformer extends
		ObservationTransformerPluginBase {

	private VeLaInterpreter vela;
	private boolean shouldInvokeDialog;
	private boolean firstInvocation;

	@Override
	public String getDisplayName() {
		return "VeLa Observation Transformer";
	}

	@Override
	public String getDescription() {
		return "VeLa observation transformation tool";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "VeLa Observation Transformer Plug-In.pdf";
	}

	@Override
	public IUndoableAction createAction(ISeriesInfoProvider seriesInfo,
			Set<SeriesType> series) {
		return new IUndoableAction() {

			private Map<SeriesType, List<Double>> mags = new HashMap<SeriesType, List<Double>>();
			private Map<SeriesType, List<Double>> errs = new HashMap<SeriesType, List<Double>>();

			@Override
			public boolean execute(UndoableActionType type) {
				boolean ok = true;

				if (firstInvocation) {
					Mediator.getInstance().getNewStarNotifier()
							.addListener(getNewStarListener());

					firstInvocation = false;
				}

				switch (type) {
				case DO:
					vela = new VeLaInterpreter();

					Pair<Boolean, String> pair = invokeDialog(vela);

					ok = pair.first;

					if (ok && pair.second.trim().length() != 0) {
						vela.program(pair.second);
					} else {
						break;
					}
					// Note: there being no unconditional break here is on purpose!
				case REDO:
					for (SeriesType seriesType : series) {
						for (ValidObservation ob : seriesInfo
								.getObservations(seriesType)) {
							// Store old magnitude for undo
							Magnitude magnitude = ob.getMagnitude();

							if (mags.get(seriesType) == null) {
								mags.put(seriesType, new ArrayList<Double>());
							}
							mags.get(seriesType).add(magnitude.getMagValue());

							if (errs.get(seriesType) == null) {
								errs.put(seriesType, new ArrayList<Double>());
							}
							errs.get(seriesType)
									.add(magnitude.getUncertainty());

							// Push an environment that makes the
							// observation available to VeLa code...
							vela.pushEnvironment(new VeLaValidObservationEnvironment(
									ob));

							// ...and call the function with the current
							// observation's magnitude and error values.
							String funCall = String.format("do()",
									magnitude.getMagValue(),
									magnitude.getUncertainty());

							Optional<Operand> result = vela.program(funCall);

							if (result.isPresent()
									&& result.get().getType() == Type.LIST) {
								Operand op = result.get();

								if (op.listVal().size() == 2) {
									boolean bothReal = op
											.listVal()
											.stream()
											.allMatch(
													x -> x.convert(Type.REAL) == Type.REAL);

									if (bothReal) {
										double mag = op.listVal().get(0)
												.doubleVal();
										double err = op.listVal().get(1)
												.doubleVal();
										ob.setMagnitude(new Magnitude(mag, err));
									} else {
										// Need two real numbers
										ok = false;
									}
								} else {
									// Need two real numbers
									ok = false;
								}
							} else {
								// Need (two real numbers in) a list
								ok = false;
							}

							if (!ok) {
								ok = false;
								MessageBox.showErrorDialog("VeLa Error",
										"Expected a 2 element result list");
							}

							// Push current observation's environment.
							vela.popEnvironment();
						}
					}
					break;
				case UNDO:
					// Undo by restoring magnitude and error values.
					for (SeriesType seriesType : series) {
						for (int i = 0; i < seriesInfo.getObservations(
								seriesType).size(); i++) {
							double mag = mags.get(seriesType).get(i);
							double err = errs.get(seriesType).get(i);
							ValidObservation ob = seriesInfo.getObservations(
									seriesType).get(i);
							ob.setMagnitude(new Magnitude(mag, err));
						}
					}
					break;
				}

				return ok;
			}

			@Override
			public String getDisplayString() {
				return "VeLa observation transformation";
			}
		};
	}

	/**
	 * Get the new star listener for this plugin.
	 */
	protected Listener<NewStarMessage> getNewStarListener() {
		return new Listener<NewStarMessage>() {
			public void update(NewStarMessage info) {
				shouldInvokeDialog = true;
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Invoke dialog to request magnitude shift value.
	 * 
	 * @return A pair containing a Boolean and string: whether the dialog's OK
	 *         button was clicked and the VeLa code string.
	 */
	private Pair<Boolean, String> invokeDialog(VeLaInterpreter vela) {
		VeLaDialog velaDialog = new VeLaDialog(
				"Define VeLa function: do():list");

		boolean ok = !velaDialog.isCancelled();
		String code = velaDialog.getCode();

		return new Pair<Boolean, String>(ok, code);
	}
}
