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
package org.aavso.tools.vstar.input.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;

/**
 * This class reads variable star observations from an AAVSO database and yields
 * a collection of observations for one star.
 * 
 * REQ_VSTAR_AAVSO_DATABASE_READ REQ_VSTAR_DATABASE_READ_ONLY
 */
public class AAVSODatabaseObservationReader extends
		AbstractObservationRetriever {

	private ResultSet source;
	private InclusiveRangePredicate uncertaintyRangePredicate;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            A SQL result set that is the source of observations.
	 */
	public AAVSODatabaseObservationReader(ResultSet source) {
		super();
		this.source = source;
		this.uncertaintyRangePredicate = new InclusiveRangePredicate(0, 1);
	}

	/**
	 * @see org.aavso.tools.vstar.input.AbstractObservationRetriever#retrieveObservations()
	 */
	public void retrieveObservations() throws ObservationReadError,
			InterruptedException {
		try {
			while (source.next()) {
				ValidObservation validOb = getNextObservation();

				// TODO: When we do more field validation here,
				// we should do these checks in getNextObservation()
				// and just throw an exception; something like ValidationError?
				// May need to modify some validators to take types other than
				// string. Actually, another approach is to read all fields as
				// strings and then do *exactly* the same validation as for file
				// sourced data.

				if (validOb.getMagnitude().isBrighterThan()) {
					InvalidObservation invalidOb = new InvalidObservation("JD "
							+ validOb.getJD(),
							"A \"Brighter Than\" observation.");
					invalidObservations.add(invalidOb);
				} else if (validOb.getHqUncertainty() != null
						&& !uncertaintyRangePredicate.holds(validOb
								.getHqUncertainty())) {
					InvalidObservation invalidOb = new InvalidObservation("JD "
							+ validOb.getJD(),
							"HQ uncertainty value out of range "
									+ uncertaintyRangePredicate.toString());
					invalidObservations.add(invalidOb);
				} else {
					// Okay. Accept this as a valid observation, but
					// ignore non-standard magnitude type observations.
					if (validOb.getMType() == MTypeType.STD) {
						validObservations.add(validOb);
						categoriseValidObservation(validOb);
					}
				}
				// TODO: why am I not updating progress bar here?
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new ObservationReadError(
					"Error when attempting to read observation source.");
		}
	}

	// Helpers

	/**
	 * Get the next observation.
	 * 
	 * Note: It would be incrementally faster to use the numeric index forms of
	 * the ResultSet getter methods instead of strings. We use the string
	 * versions for clarity. We can change this to use named constants if it
	 * proves to be too inefficient.
	 */
	private ValidObservation getNextObservation() throws ObservationReadError {
		ValidObservation ob = new ValidObservation();

		try {
			ob.setDateInfo(new DateInfo(source.getDouble("jd")));
			ob.setMagnitude(getNextMagnitude());
			ob.setHqUncertainty(source.getDouble("hq_uncertainty"));
			SeriesType band = SeriesType.Unspecified;
			String bandNum = getNextPossiblyNullString("band");
			if (bandNum != null && !"".equals(bandNum)) {
				int num = Integer.parseInt(bandNum);
				band = SeriesType.getSeriesFromIndex(num);
			}
			ob.setBand(band);
			ob.setObsCode(getNextPossiblyNullString("observer_code"));
			ob.setCommentCode(getNextPossiblyNullString("comment_code"));
			ob.setCompStar1(getNextPossiblyNullString("comp_star_1"));
			ob.setCompStar2(getNextPossiblyNullString("comp_star_2"));
			ob.setCharts(getNextPossiblyNullString("charts"));
			ob.setComments(getNextPossiblyNullString("comments"));

			ob.setTransformed("yes"
					.equals(getNextPossiblyNullString("transformed")) ? true
					: false);

			ob.setAirmass(getNextPossiblyNullString("airmass"));
			ob.setValidationType(getNextValidationType());
			ob.setCMag(getNextPossiblyNullString("cmag"));
			ob.setKMag(getNextPossiblyNullString("kmag"));

			Double hjd = getNextPossiblyNullDouble("hjd");
			ob.setHJD(hjd != null ? new DateInfo(hjd) : null);

			ob.setName(getNextPossiblyNullString("name"));

			// If mtype is null or 0, we use the ValidObservation's
			// constructed default (standard magnitude type).
			Integer mtype = getNextPossiblyNullInteger("mtype");
			if (mtype != null && mtype != 0) {
				if (mtype == 1) {
					ob.setMType(MTypeType.DIFF);
				} else if (mtype == 2) {
					ob.setMType(MTypeType.STEP);
				}
			}

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new ObservationReadError(
					"Error when attempting to read observation source: "
							+ e.getMessage());
		}

		return ob;
	}

	private Magnitude getNextMagnitude() throws SQLException {

		int fainterThan = source.getInt("fainterthan");

		MagnitudeModifier modifier;
		if (fainterThan == 1) {
			modifier = MagnitudeModifier.FAINTER_THAN;
		} else if (fainterThan == 2) {
			modifier = MagnitudeModifier.BRIGHTER_THAN;
		} else {
			modifier = MagnitudeModifier.NO_DELTA;
		}

		boolean isUncertain = source.getInt("uncertain") != 0;

		return new Magnitude(source.getDouble("magnitude"), modifier,
				isUncertain, source.getDouble("uncertainty"));
	}

	/*
	 * According to:
	 * https://sourceforge.net/apps/mediawiki/vstar/index.php?title
	 * =AAVSO_International_Database_Schema
	 * (https://sourceforge.net/apps/mediawiki/vstar/index.php?title=Valflag:)
	 * we have: Z = Prevalidated, P = Published observation, T = Discrepant, 
	 * V = Good, Y = Deleted (filtered out via SQL). Our query converts any 
	 * occurrence of 'T' to 'D'. Currently we convert everything to Good (V,G), 
	 * Discrepant (D), or Prevalidated (Z) below.
	 */
	private ValidationType getNextValidationType() throws SQLException {
		ValidationType type;

		String valflag = getNextPossiblyNullString("valflag");

		if ("Z".equals(valflag)) {
			type = ValidationType.PREVALIDATION;
		} else if ("D".equals(valflag)) {
			type = ValidationType.DISCREPANT;
		} else {
			type = ValidationType.GOOD;
		}

		return type;
	}

	private String getNextPossiblyNullString(String colName)
			throws SQLException {
		String str = source.getString(colName);
		return !source.wasNull() ? str : null;
	}

	private Double getNextPossiblyNullDouble(String colName)
			throws SQLException {
		Double num = source.getDouble(colName);
		return !source.wasNull() ? num : null;
	}

	private Integer getNextPossiblyNullInteger(String colName)
			throws SQLException {
		Integer num = source.getInt(colName);
		return !source.wasNull() ? num : null;
	}
}
