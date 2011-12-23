package org.aavso.tools.vstar.input.ws.endpoint;

import javax.jws.WebMethod;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.UnknownAUIDError;
import org.aavso.tools.vstar.exception.UnknownStarError;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

public interface IObsWebService {

	/**
	 * Return information about the named star.
	 * 
	 * @param name
	 *            The star name or alias.
	 * @return A StarInfo object containing information about the star, e.g.
	 *         name, AUID, period.
	 * 
	 * @throws ConnectionException
	 *             , UnknownStarError
	 */
	@WebMethod
	public abstract StarInfo getStarInfoByName(String starName)
			throws ConnectionException, UnknownStarError;

	/**
	 * Return information about the star given an AUID.
	 * 
	 * @param auid
	 *            The AUID.
	 * @return A StarInfo object containing information about the star, e.g.
	 *         name, AUID, period.
	 * 
	 * @throws ConnectionException
	 *             , ObservationReadError, UnknownAUIDError
	 */
	@WebMethod
	public abstract StarInfo getStarInfoByAUID(String auid)
			throws ConnectionException, UnknownAUIDError;


	/**
	 * Given information about the star and a JD range, return a list of
	 * observations.
	 * 
	 * @param info
	 *            The object containing information about the star. It must at
	 *            least have a non-null AUID member.
	 * @param minJD
	 *            The minimum Julian Day.
	 * @param maxJD
	 *            The maximum Julian Day.
	 * @return An array of resulting observations.
	 * 
	 * @throws ConnectionException
	 *             , ObservationReadError
	 */
	@WebMethod
	public Ob[] getObservationsInRange(StarInfo info, double minJD, double maxJD)
			throws ConnectionException, ObservationReadError;

	/**
	 * Given information about the star, return a list of observations.
	 * 
	 * @param info
	 *            The object containing information about the star. It must at
	 *            least have a non-null AUID member.
	 * @param minJD
	 *            The minimum Julian Day.
	 * @param maxJD
	 *            The maximum Julian Day.
	 * @return An array of resulting observations.
	 * 
	 * @throws ConnectionException
	 *             , ObservationReadError
	 */
	@WebMethod
	public abstract ValidObservation[] getObservationsWithJDRange(
			StarInfo info, double minJD, double maxJD)
			throws ConnectionException, ObservationReadError;

	/**
	 * Given information about the star and a JD range, return a list of
	 * observations.
	 * 
	 * @param info
	 *            The object containing information about the star. It must at
	 *            least have a non-null AUID member.
	 * @return An array of resulting observations.
	 * 
	 * @throws ConnectionException
	 *             , ObservationReadError
	 */
	@WebMethod
	public abstract ValidObservation[] getAllObservations(StarInfo info, int n)
			throws ConnectionException, ObservationReadError;
}