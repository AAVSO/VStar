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
package org.aavso.tools.vstar.input.ws.endpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.UnknownAUIDError;
import org.aavso.tools.vstar.exception.UnknownStarError;
import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;
import org.aavso.tools.vstar.input.database.AAVSODatabaseObservationReader;
import org.aavso.tools.vstar.input.database.DatabaseType;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * This class exposes a SOAP-based web service for retrieval of AAVSO
 * observations.
 * 
 * A number of sources may be consulted, e.g. VSX and AID databases.
 * 
 * TODO: o Rather than returning ValidObservation[], we may want to return some
 * other simpler data structure or object. It needs to be easily consumable by
 * other languages. o Either disallow getAllObservations() or add a limit value.
 * o Another approach would be to add a method such as: obCount =
 * getObservationCount(info, minJD, maxJD) then repeated calls over the range
 * 0..obCount-1 to: getObservations(minObNum, maxObNum)
 */
@WebService
public class AAVSOObsWebService implements IObsWebService {

	private DatabaseType obSourceDatabaseType;

	/**
	 * Parameterless constructor for web service.
	 */
	public AAVSOObsWebService() {
		this(DatabaseType.OBSERVATION);
	}

	/**
	 * Constructor.
	 * 
	 * @param obSourceDatabaseType
	 *            The database type.
	 */
	public AAVSOObsWebService(DatabaseType obSourceDatabaseType) {
		this.obSourceDatabaseType = obSourceDatabaseType;
	}

	/**
	 * @see org.aavso.tools.vstar.input.ws.endpoint.IObsWebService#getStarInfoByName(java.lang.String)
	 */
	@WebMethod
	public StarInfo getStarInfoByName(String starName)
			throws ConnectionException, UnknownStarError {

		StarInfo starInfo = null;

		try {
			AAVSODatabaseConnector vsxConnector = null;
			if (obSourceDatabaseType == DatabaseType.OBSERVATION) {
				vsxConnector = AAVSODatabaseConnector.vsxDBConnector;
			} else {
				vsxConnector = AAVSODatabaseConnector.utDBConnector;
			}

			Connection vsxConnection = vsxConnector.createConnection();

			starInfo = vsxConnector.getAUID(vsxConnection, starName);

			if (starInfo.getAuid() == null) {
				throw new UnknownStarError(starName);
			}
		} catch (SQLException e) {
			throw new ConnectionException();
		}

		return starInfo;
	}

	/**
	 * @see org.aavso.tools.vstar.input.ws.endpoint.IObsWebService#getStarInfoByAUID(java.lang.String)
	 */
	@WebMethod
	public StarInfo getStarInfoByAUID(String auid) throws ConnectionException,
			UnknownAUIDError {

		StarInfo starInfo = null;

		try {
			AAVSODatabaseConnector vsxConnector = null;
			if (obSourceDatabaseType == DatabaseType.OBSERVATION) {
				vsxConnector = AAVSODatabaseConnector.vsxDBConnector;
			} else {
				vsxConnector = AAVSODatabaseConnector.utDBConnector;
			}

			Connection vsxConnection = vsxConnector.createConnection();

			starInfo = vsxConnector.getStarName(vsxConnection, auid);

			if (starInfo.getDesignation() == null) {
				throw new UnknownAUIDError(auid);
			}
		} catch (SQLException e) {
			throw new ConnectionException();
		}

		return starInfo;
	}

	/**
	 * @see org.aavso.tools.vstar.input.ws.endpoint.IObsWebService#getObservationsInRange(org.aavso.tools.vstar.ui.mediator.StarInfo,
	 *      double, double) <br/>
	 *      TODO: rename this method!
	 */
	@WebMethod
	public Ob[] getObservationsInRange(StarInfo info, double minJD,
			double maxJD) throws ConnectionException, ObservationReadError {
		List<Ob> obList = new ArrayList<Ob>();

		for (ValidObservation ob : getObservationsWithJDRange(info, minJD,
				maxJD)) {
			Ob sob = new Ob(ob.getJD(), ob.getMag(), ob
					.getMagnitude().getUncertainty());
			obList.add(sob);
		}

		return obList.toArray(new Ob[0]);
	}

	/**
	 * @see org.aavso.tools.vstar.input.ws.endpoint.IObsWebService#getObservationsWithJDRange(org.aavso.tools.vstar.ui.mediator.StarInfo,
	 *      double, double)
	 */
	@WebMethod
	public ValidObservation[] getObservationsWithJDRange(StarInfo info,
			double minJD, double maxJD) throws ConnectionException,
			ObservationReadError {

		AAVSODatabaseConnector obsConnector = null;
		if (obSourceDatabaseType == DatabaseType.OBSERVATION) {
			obsConnector = AAVSODatabaseConnector.observationDBConnector;
		} else {
			obsConnector = AAVSODatabaseConnector.utDBConnector;
		}

		Connection obsConnection = obsConnector.createConnection();

		PreparedStatement obsStmt = null;

		try {
			obsStmt = obsConnector
					.createObservationWithJDRangeQuery(obsConnection);

			obsConnector.setObservationWithJDRangeQueryParams(obsStmt, info
					.getAuid(), minJD, maxJD);

			ResultSet results = obsStmt.executeQuery();

			AAVSODatabaseObservationReader databaseObsReader = new AAVSODatabaseObservationReader(
					results);

			databaseObsReader.retrieveObservations();
			info.setRetriever(databaseObsReader);

			if (databaseObsReader.getValidObservations().isEmpty()) {
				throw new ObservationReadError(
						"No observations for the specified period.");
			}

		} catch (SQLException e) {
			throw new ConnectionException();
		} catch (InterruptedException e) {
			throw new ConnectionException();
		}

		return info.getRetriever().getValidObservations().toArray(
				new ValidObservation[0]);
	}

	/**
	 * @see org.aavso.tools.vstar.input.ws.endpoint.IObsWebService#getAllObservations(org.aavso.tools.vstar.ui.mediator.StarInfo)
	 */
	@WebMethod
	public ValidObservation[] getAllObservations(StarInfo info, int n)
			throws ConnectionException, ObservationReadError {

		AAVSODatabaseConnector obsConnector = null;
		if (obSourceDatabaseType == DatabaseType.OBSERVATION) {
			obsConnector = AAVSODatabaseConnector.observationDBConnector;
		} else {
			obsConnector = AAVSODatabaseConnector.utDBConnector;
		}
		Connection obsConnection = obsConnector.createConnection();

		PreparedStatement obsStmt = null;

		try {
			obsStmt = obsConnector
					.createObservationWithNoJDRangeQuery(obsConnection);

			obsConnector.setObservationWithNoJDRangeQueryParams(obsStmt, info
					.getAuid());

			ResultSet results = obsStmt.executeQuery();

			AAVSODatabaseObservationReader databaseObsReader = new AAVSODatabaseObservationReader(
					results);

			databaseObsReader.retrieveObservations();
			info.setRetriever(databaseObsReader);

			if (databaseObsReader.getValidObservations().isEmpty()) {
				throw new ObservationReadError(
						"No observations for the specified period.");
			}

		} catch (SQLException e) {
			throw new ConnectionException();
		} catch (InterruptedException e) {
			throw new ConnectionException();
		}

		return info.getRetriever().getValidObservations().toArray(
				new ValidObservation[0]);
	}

	/**
	 * Main invocation point.
	 * 
	 * @param args
	 *            The single element contains the port on which the web service
	 *            should listen.
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err
					.println("A port number on which to listen must be supplied.");
		} else {
			int port = Integer.parseInt(args[0]);
			AAVSOObsWebService service = new AAVSOObsWebService(
					DatabaseType.OBSERVATION);
			String url = "http://localhost:" + port + "/obs";
			Endpoint.publish(url, service);
		}
	}
}
