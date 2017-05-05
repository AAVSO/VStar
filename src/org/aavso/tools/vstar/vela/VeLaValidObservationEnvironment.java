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

import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.Pair;

/**
 * A VeLa environment that is backed by a ValidObservation instance.
 */
public class VeLaValidObservationEnvironment extends AbstractVeLaEnvironment {

	private static Map<String, String> symbol2CanonicalSymbol;

	static {
		symbol2CanonicalSymbol = new TreeMap<String, String>();
	}

	private ValidObservation ob;

	public VeLaValidObservationEnvironment(ValidObservation ob) {
		super();
		this.ob = ob;
	}

	@Override
	public Pair<Boolean, Operand> lookup(String name) {
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
		} else if ("SERIES".equals(name)) {
			operand = operand(name, ob.getBand().getDescription());
		} else if ("PHASE".equals(name)) {
			contained &= ob.getStandardPhase() != null;
			if (contained) {
				operand = operand(name, ob.getStandardPhase());
			}
		} else if ("MTYPE".equals(name)) {
			operand = operand(name, ob.getMType().getShortName());
		} else if ("VAL_FLAG".equals(name)) {
			contained &= ob.getValidationType() != null;
			if (contained) {
				operand = operand(name, ob.getValidationType().getValflag());
			}
		} else if ("TRANSFORMED".equals(name)) {
			operand = operand(name, ob.isTransformed());
		} else if ("COMMENT_CODES".equals(name)) {
			contained &= ob.getCommentCode() != null;
			if (contained) {
				operand = operand(name, ob.getCommentCode().toString());
			}
		} else {
			contained = ob.nonEmptyDetailExists(name);

			if (contained) {
				operand = operand(name, ob.getDetail(name));
			}
		}

		// TODO:
		// - discrepant, excluded, fainter-than: for model creation, later
		// - HJD later

		return new Pair<Boolean, Operand>(contained, operand);
	}

	/**
	 * Return the symbols associated with currently loaded observations.
	 * 
	 * @param info
	 *            A new star information message.
	 * @return An array of symbol names.
	 */
	public static String[] symbols(NewStarMessage info) {
		populateMap(info);

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
		symbol2CanonicalSymbol.clear();
	}

	// Helpers

	private static void populateMap(NewStarMessage info) {
		reset();

		// TODO: actually, can't assume any conditionality below; remove...
		
		symbol2CanonicalSymbol.put("TIME", "TIME");
		symbol2CanonicalSymbol.put("JD", "TIME");

		symbol2CanonicalSymbol.put("MAGNITUDE", "MAGNITUDE");
		symbol2CanonicalSymbol.put("MAG", "MAGNITUDE");

		symbol2CanonicalSymbol.put("UNCERTAINTY", "UNCERTAINTY");

		symbol2CanonicalSymbol.put("BAND", "SERIES");
		symbol2CanonicalSymbol.put("SERIES", "SERIES");

		symbol2CanonicalSymbol.put("PHASE", "PHASE");

		// AAVSO upload format obs source uses this; not all plug-ins do.
		// Can live with this being in the list for all obs sources for now
		// though.
		symbol2CanonicalSymbol.put("OBS_CODE", "OBS_CODE");

		switch (info.getNewStarType()) {
		case NEW_STAR_FROM_DATABASE:
		case NEW_STAR_FROM_DOWNLOAD_FILE:
			symbol2CanonicalSymbol.put("COMMENTCODES", "COMMENT_CODES");
			symbol2CanonicalSymbol.put("COMMENT_CODES", "COMMENT_CODES");
		case NEW_STAR_FROM_SIMPLE_FILE:
			symbol2CanonicalSymbol.put("VALFLAG", "VAL_FLAG");
			symbol2CanonicalSymbol.put("VAL_FLAG", "VAL_FLAG");
		case NEW_STAR_FROM_ARBITRARY_SOURCE:
			symbol2CanonicalSymbol.put("MTYPE", "MTYPE");
			symbol2CanonicalSymbol.put("TRANSFORMED", "TRANSFORMED");
			break;
		}

		for (String detailKey : ValidObservation.getDetailTitles().keySet()) {
			switch (info.getNewStarType()) {
			// Other than what we are adding above, don't include standard
			// detail keys for simple or arbitrary observation sources.
			case NEW_STAR_FROM_SIMPLE_FILE:
			case NEW_STAR_FROM_ARBITRARY_SOURCE:
				if (ValidObservation.getStandardDetailKeys()
						.contains(detailKey)) {
					detailKey = null;
				}
				break;
			default:
				break;
			}

			if (detailKey != null) {
				symbol2CanonicalSymbol.put(detailKey, detailKey);
			}
		}

		// Underscore-less variations on standard keys that contain
		// underscores!
		if (symbol2CanonicalSymbol.containsKey("OBS_CODE")) {
			symbol2CanonicalSymbol.put("OBSCODE", "OBS_CODE");
		}

		if (symbol2CanonicalSymbol.containsKey("COMP_STAR1")) {
			symbol2CanonicalSymbol.put("COMPSTAR1", "COMP_STAR1");
		}

		if (symbol2CanonicalSymbol.containsKey("COMP_STAR2")) {
			symbol2CanonicalSymbol.put("COMPSTAR2", "COMP_STAR2");
		}
	}
}
