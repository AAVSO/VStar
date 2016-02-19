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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.aavso.tools.vstar.input.IStarInfoSource;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * This class obtains star name and AUID information from the VSX database.
 */
public class VSXStarNameAndAUIDSource implements IStarInfoSource {

	private static final String STARTABLE = "vsx_objects";
	private static final String ALIASTABLE = "vsx_crossids";

	private static final int RADEC_EPOCH = 1950;

	private PreparedStatement findAUIDFromNameStatement;
	private PreparedStatement findAUIDFromAliasStatement;
	private PreparedStatement findStarNameFromAUID;

	// TODO: connection should be retrieved not passed

	/**
	 * Return the AUID and other information about the named star.
	 * 
	 * @param name
	 *            The star name or alias.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByName(Connection connection, String name)
			throws SQLException {
		String auid = null;
		Double period = null;
		Double epoch = null;
		String varType = null;
		String spectralType = null;
		String discoverer = null;
		RAInfo ra = null;
		DecInfo dec = null;

		createFindAUIDFromNameStatement(connection);
		findAUIDFromNameStatement.setString(1, name);
		findAUIDFromNameStatement.setString(2, name);
		findAUIDFromNameStatement.setString(3, name);
		ResultSet rs = findAUIDFromNameStatement.executeQuery();
		if (!rs.first()) {
			createFindAUIDFromAliasStatement(connection);
			findAUIDFromAliasStatement.setString(1, name);
			findAUIDFromAliasStatement.setString(2, name);
			rs = findAUIDFromAliasStatement.executeQuery();
			if (rs.first()) {
				auid = rs.getString("o_auid");
				period = getPossiblyNullPeriod(rs);
				epoch = getPossiblyNullEpoch(rs);
				varType = getPossiblyNullStringValue(rs, "o_varType");
				spectralType = getPossiblyNullStringValue(rs, "o_specType");
				discoverer = getPossiblyNullStringValue(rs, "o_discoverer");
				ra = getRA(rs);
				dec = getDec(rs);
			}
		} else {
			auid = rs.getString("o_auid");
			period = getPossiblyNullPeriod(rs);
			epoch = getPossiblyNullEpoch(rs);
			varType = getPossiblyNullStringValue(rs, "o_varType");
			spectralType = getPossiblyNullStringValue(rs, "o_specType");
			discoverer = getPossiblyNullStringValue(rs, "o_discoverer");
			ra = getRA(rs);
			dec = getDec(rs);
		}

		return new StarInfo(name, auid, period, epoch, varType, spectralType,
				discoverer, ra, dec);
	}

	/**
	 * Return the name and other information about a star given an AUID.
	 * 
	 * @param name
	 *            The AUID.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByAUID(Connection connection, String auid)
			throws SQLException {
		String starName = null;
		Double period = null;
		Double epoch = null;
		String varType = null;
		String spectralType = null;
		String discoverer = null;
		RAInfo ra = null;
		DecInfo dec = null;
		
		createFindStarNameFromAUIDStatement(connection);
		findStarNameFromAUID.setString(1, auid);

		ResultSet rs = findStarNameFromAUID.executeQuery();

		if (rs.first()) {
			starName = rs.getString("o_designation");
			period = getPossiblyNullPeriod(rs);
			epoch = getPossiblyNullEpoch(rs);
			varType = getPossiblyNullStringValue(rs, "o_varType");
			spectralType = getPossiblyNullStringValue(rs, "o_specType");
			discoverer = getPossiblyNullStringValue(rs, "o_discoverer");
			ra = getRA(rs);
			dec = getDec(rs);
		}

		return new StarInfo(starName, auid, period, epoch, varType,
				spectralType, discoverer, ra, dec);
	}

	// Helpers

	private RAInfo getRA(ResultSet rs) throws SQLException {
		Integer raH = null;
		Integer raM = null;
		Double raS = null;

		raH = getPossiblyNullIntegerValue(rs, "o_rah1950");
		raM = getPossiblyNullIntegerValue(rs, "o_ram1950");
		raS = getPossiblyNullDoubleValue(rs, "o_ras1950");

		RAInfo ra = null;
		if (raH != null && raM != null && raS != null) {
			ra = new RAInfo(RADEC_EPOCH, raH, raM, raS);
		}

		return ra;
	}

	private DecInfo getDec(ResultSet rs) throws SQLException {
		Integer decD = null;
		Integer decM = null;
		Double decS = null;
		Integer decSign = null;

		decD = getPossiblyNullIntegerValue(rs, "o_ded1950");
		decM = getPossiblyNullIntegerValue(rs, "o_dem1950");
		decS = getPossiblyNullDoubleValue(rs, "o_des1950");
		decSign = getDecSign(rs);

		DecInfo dec = null;
		if (decD != null && decM != null && decS != null && decSign != null) {
			dec = new DecInfo(RADEC_EPOCH, decSign * decD, decM, decS);
		}

		return dec;
	}

	private Double getPossiblyNullPeriod(ResultSet rs) throws SQLException {
		Double period = null;

		period = rs.getDouble("o_period");
		if (rs.wasNull()) {
			period = null; // TODO: necessary?
		}

		return period;
	}

	private Double getPossiblyNullEpoch(ResultSet rs) throws SQLException {
		Double epoch = null;

		epoch = rs.getDouble("o_epoch");
		if (rs.wasNull()) {
			epoch = null; // TODO: necessary?
		}

		// Convert RJD to HJD.
		if (epoch != null) {
			epoch = epoch.doubleValue() + 2400000.0;
		}

		return epoch;
	}

	private Integer getDecSign(ResultSet rs) throws SQLException {
		String sign = null;

		sign = getPossiblyNullStringValue(rs, "o_deSign1950");

		Integer signVal = null;

		if (sign != null) {
			signVal = sign.equals("-") ? signVal = -1 : 1;
		}

		return signVal;
	}

	private Double getPossiblyNullDoubleValue(ResultSet rs, String colName)
			throws SQLException {
		Double val = null;

		val = rs.getDouble(colName);
		if (rs.wasNull()) {
			val = null; // TODO: necessary?
		}

		return val;
	}

	private Integer getPossiblyNullIntegerValue(ResultSet rs, String colName)
			throws SQLException {
		Integer val = null;

		val = rs.getInt(colName);
		if (rs.wasNull()) {
			val = null; // TODO: necessary?
		}

		return val;
	}

	private String getPossiblyNullStringValue(ResultSet rs, String colName)
			throws SQLException {
		String value = rs.getString(colName);

		if (rs.wasNull()) {
			value = null; // TODO: necessary?
		}

		return value;
	}

	// Create statements to retrieve AUID from star name.

	// TODO: Look more closely at CONCAT and REPLACE function usage in next
	// two queries (REPLACE: substitution of 'V0' with 'V'). Can we use LIKE
	// or REGEX instead? Would that be more or less performant?

	protected PreparedStatement createFindAUIDFromNameStatement(
			Connection connect) throws SQLException {
		if (findAUIDFromNameStatement == null) {
			findAUIDFromNameStatement = connect
					.prepareStatement("SELECT o_auid, o_designation, "
							+ "o_period, o_epoch, o_varType, o_specType, o_discoverer, "
							+ "o_rah1950, o_ram1950, o_ras1950, o_ded1950, o_dem1950, o_des1950, o_deSign1950"
							+ " FROM "
							+ STARTABLE
							+ " WHERE (o_auid = ? OR o_designation = ? OR REPLACE(o_designation, \"V0\", \"V\") = ?) "
							+ "AND o_auid is not null");
		}
		return findAUIDFromNameStatement;
	}

	protected PreparedStatement createFindAUIDFromAliasStatement(
			Connection connect) throws SQLException {
		if (findAUIDFromAliasStatement == null) {
			findAUIDFromAliasStatement = connect
					.prepareStatement("SELECT o_auid, o_designation, "
							+ "o_period, o_epoch, o_varType, o_specType, o_discoverer, "
							+ "o_rah1950, o_ram1950, o_ras1950, o_ded1950, o_dem1950, o_des1950, o_deSign1950"
							+ " FROM "
							+ STARTABLE
							+ ", "
							+ ALIASTABLE
							+ " WHERE "
							+ STARTABLE
							+ ".oid = "
							+ ALIASTABLE
							+ ".oid "
							+ "AND (x_catName = ? OR CONCAT(x_catAcronym, REPLACE(x_catName,\" \",\"\")) "
							+ "= REPLACE(?,\" \",\"\")) AND o_auid is not null");
		}
		return findAUIDFromAliasStatement;
	}

	// Create statement to retrieve star name from AUID.
	protected PreparedStatement createFindStarNameFromAUIDStatement(
			Connection connect) throws SQLException {

		if (findStarNameFromAUID == null) {
			findStarNameFromAUID = connect
					.prepareStatement("SELECT o_designation, o_period, o_epoch, "
							+ "o_varType, o_specType, o_discoverer, "
							+ "o_rah1950, o_ram1950, o_ras1950, o_ded1950, o_dem1950, o_des1950, o_deSign1950"
							+ " FROM " + STARTABLE + " WHERE o_auid = ?");
		}

		return findStarNameFromAUID;
	}
}
