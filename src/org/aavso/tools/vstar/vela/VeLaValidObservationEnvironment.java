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
package org.aavso.tools.vstar.vela;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.list.ITableColumnInfoSource;

/**
 * A VeLa environment that is backed by a ValidObservation instance.
 */
public class VeLaValidObservationEnvironment extends VeLaEnvironment<Operand> {

	private static Map<String, String> symbol2CanonicalSymbol;

	static {
		symbol2CanonicalSymbol = new TreeMap<String, String>();
	}

	private static ITableColumnInfoSource columnInfoSource = null;

	private ValidObservation ob;

	public VeLaValidObservationEnvironment(ValidObservation ob) {
		super();
		this.ob = ob;
		reset();
	}

	@Override
	public Optional<Operand> lookup(String name) {
		boolean contained = false;
		Operand operand = null;

		name = name.toUpperCase();

		contained = symbol2CanonicalSymbol.containsKey(name);

		if (contained) {
			name = symbol2CanonicalSymbol.get(name);
		}

		if ("TIME".equals(name)) {
			operand = operand(name, ob.getJD());
		} else if ("MAGNITUDE".equals(name)) {
			operand = operand(name, ob.getMag());
		} else if ("UNCERTAINTY".equals(name)) {
			operand = operand(name, ob.getMagnitude().getUncertainty());
		} else if ("BAND".equals(name)) {
			operand = operand(name, ob.getBand().getDescription());
		} else if ("SHORTBAND".equals(name)) {
			operand = operand(name, ob.getBand().getShortName());
		} else if ("SERIES".equals(name)) {
			operand = operand(name, ob.getSeries().getDescription());
		} else if ("OBS_CODE".equals(name)) {
			operand = operand(name, ob.getObsCode());
		} else if ("STANDARDPHASE".equals(name)) {
			contained &= ob.getStandardPhase() != null;
			if (contained) {
				operand = operand(name, ob.getStandardPhase());
			}
		} else if ("PREVIOUSCYCLEPHASE".equals(name)) {
			contained &= ob.getPreviousCyclePhase() != null;
			if (contained) {
				operand = operand(name, ob.getPreviousCyclePhase());
			}
		} else {
			if (columnInfoSource != null) {
				int index = columnInfoSource.getColumnIndexByName(name);
				operand = objToOperand(name, columnInfoSource.getTableColumnValue(index, ob));
			}
		}

		return Optional.ofNullable(operand);
	}

	// Cached operand creation methods

	protected Operand operand(String name, Integer value) {
		return operand(Type.INTEGER, name, value);
	}

	protected Operand operand(String name, Double value) {
		return operand(Type.REAL, name, value);
	}

	protected Operand operand(String name, String value) {
		return operand(Type.STRING, name, value);
	}

	protected Operand operand(String name, Boolean value) {
		return operand(Type.BOOLEAN, name, value);
	}

	protected Operand objToOperand(String name, Object value) {
		Type type = Type.NONE;

		if (value instanceof Integer) {
			type = Type.INTEGER;
		} else if (value instanceof Double) {
			type = Type.REAL;
		} else if (value instanceof String) {
			type = Type.STRING;
		} else if (value instanceof Boolean) {
			type = Type.BOOLEAN;
		}

		return operand(type, name, value);
	}

	// Common operand factory method

	protected Operand operand(Type type, String name, Object value) {
		Operand operand = null;

		name = name.toUpperCase();

		if (cache.containsKey(name)) {
			operand = cache.get(name);
		} else {
			switch (type) {
			case INTEGER:
				operand = new Operand(type, (int) value);
				break;
			case REAL:
				operand = new Operand(type, (double) value);
				break;
			case STRING:
				operand = new Operand(type, (String) value);
				break;
			case BOOLEAN:
				operand = new Operand(type, (boolean) value);
				break;
			case LIST:
				operand = new Operand(type, (List<Operand>) value);
			}

			bind(name, operand, true);
		}

		return operand;
	}

	/**
	 * Return the symbols associated with currently loaded observations.
	 * 
	 * @param info A new star information message.
	 * @return An array of symbol names.
	 */
	public static String[] symbols() {
		reset();

		String[] symbols = new String[symbol2CanonicalSymbol.size()];
		int i = 0;
		for (String symbol : symbol2CanonicalSymbol.keySet()) {
			symbols[i++] = symbol.toLowerCase();
		}

		return symbols;
	}

	/**
	 * Clear the canonical symbol map.
	 */
	public static void reset() {
		populateMap();
	}

	// Helpers

	private static void populateMap() {
		symbol2CanonicalSymbol.clear();

		// Use current observation list column names as VeLa variables

		Mediator mediator = Mediator.getInstance();
		NewStarMessage newStarMsg = mediator.getLatestNewStarMessage();
		AnalysisType analysisType = mediator.getAnalysisType();

		if (newStarMsg != null) {

			NewStarType newStarType = newStarMsg.getNewStarType();

			if (analysisType == AnalysisType.RAW_DATA) {
				columnInfoSource = newStarType.getRawDataTableColumnInfoSource();
			} else {
				columnInfoSource = newStarType.getPhasePlotTableColumnInfoSource();
			}

			if (columnInfoSource != null) {
				Collection<String> columnNames = columnInfoSource.getColumnNames();

				for (String columnName : columnNames) {
					if (columnName != null) {
						String velaName = columnName.replace(" ", "_");
						symbol2CanonicalSymbol.put(velaName.toUpperCase(), columnName);
					}
				}
			}
		}
		// Add common variables

		symbol2CanonicalSymbol.put("TIME", "TIME");
		symbol2CanonicalSymbol.put("T", "TIME");
		symbol2CanonicalSymbol.put("JD", "TIME");

		symbol2CanonicalSymbol.put("MAGNITUDE", "MAGNITUDE");
		symbol2CanonicalSymbol.put("MAG", "MAGNITUDE");

		symbol2CanonicalSymbol.put("UNCERTAINTY", "UNCERTAINTY");
		symbol2CanonicalSymbol.put("ERROR", "UNCERTAINTY");

		symbol2CanonicalSymbol.put("BAND", "BAND");
		symbol2CanonicalSymbol.put("SERIES", "SERIES");
		symbol2CanonicalSymbol.put("SHORTBAND", "SHORTBAND");
		
		symbol2CanonicalSymbol.put("OBSCODE", "OBS_CODE");

		// Add phase variables if we're in phase plot mode

		if (analysisType == AnalysisType.PHASE_PLOT) {
			symbol2CanonicalSymbol.put("STANDARDPHASE", "STANDARDPHASE");
			symbol2CanonicalSymbol.put("PHASE", "STANDARDPHASE");
			symbol2CanonicalSymbol.put("PREVIOUSCYCLEPHASE", "PREVIOUSCYCLEPHASE");
		}
	}
}
