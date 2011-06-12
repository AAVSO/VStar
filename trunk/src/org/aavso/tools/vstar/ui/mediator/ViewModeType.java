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
package org.aavso.tools.vstar.ui.mediator;

/**
 * View mode type enum and methods for transforming enum values into other
 * values of interest.
 */
public enum ViewModeType {

	// View modes.
	PLOT_OBS_MODE, LIST_OBS_MODE, LIST_MEANS_MODE, MODEL_MODE, RESIDUALS_MODE;

	public final static String PLOT_OBS_MODE_DESC = "Plot";
	public final static String LIST_OBS_MODE_DESC = "Observations";
	public final static String LIST_MEANS_MODE_DESC = "Means";
	public final static String MODEL_MODE_DESC = "Model";
	public final static String RESIDUALS_MODE_DESC = "Residuals";

	/**
	 * Return this mode type's description string.
	 * 
	 * @return The mode string.
	 */
	public String getModeDesc() {
		return getModeDesc(this);
	}

	// TODO: may want this to become viewName and add viewDesc for tool-tips etc

	/**
	 * Given the mode type, return mode description string.
	 * 
	 * @param viewModeType
	 *            The document type.
	 * @return The mode string.
	 */
	private static String getModeDesc(ViewModeType viewModeType) {
		String mode = null;

		switch (viewModeType) {
		case PLOT_OBS_MODE:
			mode = PLOT_OBS_MODE_DESC;
			break;
		case LIST_OBS_MODE:
			mode = LIST_OBS_MODE_DESC;
			break;
		case LIST_MEANS_MODE:
			mode = LIST_MEANS_MODE_DESC;
			break;
		case MODEL_MODE:
			mode = MODEL_MODE_DESC;
			break;
		case RESIDUALS_MODE:
			mode = RESIDUALS_MODE_DESC;
			break;
		}

		return mode;
	}

	/**
	 * Given a mode description string, return the mode type.
	 * 
	 * @param desc
	 *            The mode description string.
	 * @return The corresponding mode type value.
	 */
	public static ViewModeType getModeFromDesc(String desc) {
		ViewModeType mode = null;

		assert (PLOT_OBS_MODE_DESC.equals(desc)
				|| LIST_OBS_MODE_DESC.equals(desc)
				|| LIST_MEANS_MODE_DESC.equals(desc)
				|| MODEL_MODE_DESC.equals(desc) || RESIDUALS_MODE_DESC
				.equals(desc));

		if (PLOT_OBS_MODE_DESC.equals(desc)) {
			mode = PLOT_OBS_MODE;
		} else if (LIST_OBS_MODE_DESC.equals(desc)) {
			mode = LIST_OBS_MODE;
		} else if (LIST_MEANS_MODE_DESC.equals(desc)) {
			mode = LIST_MEANS_MODE;
		} else if (MODEL_MODE_DESC.equals(desc)) {
			mode = MODEL_MODE;
		} else if (RESIDUALS_MODE_DESC.equals(desc)) {
			mode = RESIDUALS_MODE;
		}

		return mode;
	}
}
