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
package org.aavso.tools.vstar.data;

/**
 * This enum represents "comment code" found in all data sources.
 */
public enum CommentType {

	// These are the current acceptable comment codes (per
	// http://sourceforge.net/tracker/?func=detail&aid=2858640&group_id=263306&atid=1152052):

	// The below was used to auto generate the rest of the functions of this
	// class.

	// COMMENTCODE!! B: Sky is bright, moon, twilight, light pollution, aurorae:
	// SKY_BRIGHT
	// COMMENTCODE!! U: Clouds, dust, smoke, haze, etc.: CLOUDS
	// COMMENTCODE!! W: Poor seeing: POOR_SEEING
	// COMMENTCODE!! L: Low in the sky, near horizon, in trees, obstructed view:
	// LOW_IN_SKY
	// COMMENTCODE!! D: Unusual Activity (fading, flare, bizarre behavior,
	// etc.): UFOS
	// COMMENTCODE!! Y: Outburst: OUTBURST
	// COMMENTCODE!! K: Non-AAVSO chart: NON_AAVSO_CHART
	// COMMENTCODE!! S: Comparison sequence problem: COMP_SEQ_PROBLEM
	// COMMENTCODE!! Z: Magnitude of star uncertain: MAG_UNCERTAIN
	// COMMENTCODE!! I: Identification of star uncertain: IDENT_UNCERTAIN
	// COMMENTCODE!! V: Faint star, near observing limit, only glimpsed:
	// FAINT_STAR

	// These are Obsolete Comment Codes: These comment codes are no longer
	// accepted, but may be found in archival observations.

	// OBSCOMMENTCODE!! A: AAVSO Atlas: AAVSO_ATLAS
	// OBSCOMMENTCODE!! F: Unconventional method (out of focus, visual
	// photometer, etc.): UNCONVENTIONAL
	// OBSCOMMENTCODE!! G: Non-AAVSO chart with Guide Star Catalog magnitudes:
	// NON_AAVSO_GUIDE_STAR_CAT
	// OBSCOMMENTCODE!! H: Haze, mist, fog: HAZE
	// OBSCOMMENTCODE!! J: Non-AAVSO chart with HIPPARCOS magnitudes:
	// NON_AAVSO_HIPPARCOS
	// OBSCOMMENTCODE!! M: Moon present and inteferes: MOON
	// OBSCOMMENTCODE!! N: Angle, position angle: ANGLE
	// OBSCOMMENTCODE!! O: "Other" comment: OTHER
	// OBSCOMMENTCODE!! P: Magnitude derived from step magnitude: MAG_FROM_STEP_MAG
	// OBSCOMMENTCODE!! Q: Questioned by HQ: QUESTIONED_BY_HQ
	// OBSCOMMENTCODE!! R: Color comment (star is red, blue, etc.):
	// COLOR_COMMENT
	// OBSCOMMENTCODE!! T: Non-AAVSO chart with Tycho magnitudes:
	// NON_AAVSO_TYCHO
	// OBSCOMMENTCODE!! X: Rejected by HQ: REJECTED
	//
	// Multiple comment codes should be separated by spaces or not separated at
	// all. (Ex: "A Z Y" or "AZY")

	SKY_BRIGHT, CLOUDS, POOR_SEEING, LOW_IN_SKY, UFOS, OUTBURST, NON_AAVSO_CHART, COMP_SEQ_PROBLEM, MAG_UNCERTAIN, IDENT_UNCERTAIN, FAINT_STAR, AAVSO_ATLAS, UNCONVENTIONAL, NON_AAVSO_GUIDE_STAR_CAT, HAZE, NON_AAVSO_HIPPARCOS, MOON, ANGLE, OTHER, MAG_FROM_STEP_MAG, QUESTIONED_BY_HQ, COLOR_COMMENT, NON_AAVSO_TYCHO, REJECTED;

	/**
	 * Given a commentcode from an input file or database, return the
	 * corresponding comment type.
	 */
	public static CommentType getTypeFromFlag(String commentflag) {
		CommentType commenttype = null;

		if ("B".equals(commentflag)) {
			commenttype = SKY_BRIGHT;
		} else if ("U".equals(commentflag)) {
			commenttype = CLOUDS;
		} else if ("W".equals(commentflag)) {
			commenttype = POOR_SEEING;
		} else if ("L".equals(commentflag)) {
			commenttype = LOW_IN_SKY;
		} else if ("D".equals(commentflag)) {
			commenttype = UFOS;
		} else if ("Y".equals(commentflag)) {
			commenttype = OUTBURST;
		} else if ("K".equals(commentflag)) {
			commenttype = NON_AAVSO_CHART;
		} else if ("S".equals(commentflag)) {
			commenttype = COMP_SEQ_PROBLEM;
		} else if ("Z".equals(commentflag)) {
			commenttype = MAG_UNCERTAIN;
		} else if ("I".equals(commentflag)) {
			commenttype = IDENT_UNCERTAIN;
		} else if ("V".equals(commentflag)) {
			commenttype = FAINT_STAR;
		} else if ("A".equals(commentflag)) {
			commenttype = AAVSO_ATLAS;
		} else if ("F".equals(commentflag)) {
			commenttype = UNCONVENTIONAL;
		} else if ("G".equals(commentflag)) {
			commenttype = NON_AAVSO_GUIDE_STAR_CAT;
		} else if ("H".equals(commentflag)) {
			commenttype = HAZE;
		} else if ("J".equals(commentflag)) {
			commenttype = NON_AAVSO_HIPPARCOS;
		} else if ("M".equals(commentflag)) {
			commenttype = MOON;
		} else if ("N".equals(commentflag)) {
			commenttype = ANGLE;
		} else if ("O".equals(commentflag)) {
			commenttype = OTHER;
		} else if ("P".equals(commentflag)) {
			commenttype = MAG_FROM_STEP_MAG;
		} else if ("Q".equals(commentflag)) {
			commenttype = QUESTIONED_BY_HQ;
		} else if ("R".equals(commentflag)) {
			commenttype = COLOR_COMMENT;
		} else if ("T".equals(commentflag)) {
			commenttype = NON_AAVSO_TYCHO;
		} else if ("X".equals(commentflag)) {
			commenttype = REJECTED;
		}

		if (commenttype == null) {
			commenttype = OTHER;
		}

		return commenttype;
	}

	/**
	 * Return the comment string corresponding to this this enum value.
	 */
	public String getCommentFlag() {
		String str = null;

		switch (this) {
		case SKY_BRIGHT:
			str = "B";
			break;
		case CLOUDS:
			str = "U";
			break;
		case POOR_SEEING:
			str = "W";
			break;
		case LOW_IN_SKY:
			str = "L";
			break;
		case UFOS:
			str = "D";
			break;
		case OUTBURST:
			str = "Y";
			break;
		case NON_AAVSO_CHART:
			str = "K";
			break;
		case COMP_SEQ_PROBLEM:
			str = "S";
			break;
		case MAG_UNCERTAIN:
			str = "Z";
			break;
		case IDENT_UNCERTAIN:
			str = "I";
			break;
		case FAINT_STAR:
			str = "V";
			break;
		case AAVSO_ATLAS:
			str = "A";
			break;
		case UNCONVENTIONAL:
			str = "F";
			break;
		case NON_AAVSO_GUIDE_STAR_CAT:
			str = "G";
			break;
		case HAZE:
			str = "H";
			break;
		case NON_AAVSO_HIPPARCOS:
			str = "J";
			break;
		case MOON:
			str = "M";
			break;
		case ANGLE:
			str = "N";
			break;
		case OTHER:
			str = "O";
			break;
		case MAG_FROM_STEP_MAG:
			str = "P";
			break;
		case QUESTIONED_BY_HQ:
			str = "Q";
			break;
		case COLOR_COMMENT:
			str = "R";
			break;
		case NON_AAVSO_TYCHO:
			str = "T";
			break;
		case REJECTED:
			str = "X";
			break;
		default:
			// Default to OTHER
			str = "O";
			break;
		}

		return str;
	}

	public String toString() {
		String str = null;

		switch (this) {
		case SKY_BRIGHT:
			str = "Sky is bright, moon, twilight, light pollution, aurorae\n";
			break;
		case CLOUDS:
			str = "Clouds, dust, smoke, haze, etc.\n";
			break;
		case POOR_SEEING:
			str = "Poor seeing\n";
			break;
		case LOW_IN_SKY:
			str = "Low in the sky, near horizon, in trees, obstructed view\n";
			break;
		case UFOS:
			str = "Unusual Activity (fading, flare, bizarre behavior, etc.)\n";
			break;
		case OUTBURST:
			str = "Outburst\n";
			break;
		case NON_AAVSO_CHART:
			str = "Non-AAVSO chart\n";
			break;
		case COMP_SEQ_PROBLEM:
			str = "Comparison sequence problem\n";
			break;
		case MAG_UNCERTAIN:
			str = "Magnitude of star uncertain\n";
			break;
		case IDENT_UNCERTAIN:
			str = "Identification of star uncertain\n";
			break;
		case FAINT_STAR:
			str = "Faint star, near observing limit, only glimpsed\n";
			break;
		case AAVSO_ATLAS:
			str = "AAVSO Atlas\n";
			break;
		case UNCONVENTIONAL:
			str = "Unconventional method (out of focus, visual photometer, etc.)\n";
			break;
		case NON_AAVSO_GUIDE_STAR_CAT:
			str = "Non-AAVSO chart with Guide Star Catalog magnitudes\n";
			break;
		case HAZE:
			str = "Haze, mist, fog\n";
			break;
		case NON_AAVSO_HIPPARCOS:
			str = "Non-AAVSO chart with HIPPARCOS magnitudes\n";
			break;
		case MOON:
			str = "Moon present and inteferes\n";
			break;
		case ANGLE:
			str = "Angle, position angle\n";
			break;
		case OTHER:
			str = "Other comment\n";
			break;
		case MAG_FROM_STEP_MAG:
			str = "Magnitude derived from step magnitude\n";
			break;
		case QUESTIONED_BY_HQ:
			str = "Questioned by HQ\n";
			break;
		case COLOR_COMMENT:
			str = "Color comment (star is red, blue, etc.)\n";
			break;
		case NON_AAVSO_TYCHO:
			str = "Non-AAVSO chart with Tycho magnitudes\n";
			break;
		case REJECTED:
			str = "Rejected by HQ\n";
			break;
		default:
			str = "\"Other\" comment\n";
			break;
		}

		return str;
	}

	public static String getRegex() {
		// Permit any character through. We don't want to
		// exclude a data-point just because of an invalid
		// comment code being used. Anything that is not known
		// (see methods above) will be treated as "other".
		return ".";
	}
}
