package org.aavso.tools.vstar.exception;

import org.aavso.tools.vstar.data.ValidObservation;

import junit.framework.TestCase;

public class ExceptionTest extends TestCase {

	public ExceptionTest(String name) {
		super(name);
	}

	public void testAlgorithmError() {
		AlgorithmError e = new AlgorithmError("algo msg");
		assertEquals("algo msg", e.getMessage());
	}

	public void testAuthenticationError() {
		AuthenticationError e = new AuthenticationError("auth failed");
		assertEquals("auth failed", e.getMessage());
	}

	public void testCancellationException() {
		CancellationException e = new CancellationException("cancelled");
		assertEquals("cancelled", e.getMessage());
	}

	public void testConnectionException() {
		ConnectionException e = new ConnectionException("no route");
		assertEquals("no route", e.getMessage());
	}

	public void testObservationReadError() {
		ObservationReadError e = new ObservationReadError("read fail");
		assertEquals("read fail", e.getMessage());
	}

	public void testObservationValidationError() {
		ObservationValidationError e = new ObservationValidationError("invalid");
		assertEquals("invalid", e.getMessage());
	}

	public void testObservationValidationWarningWithOb() {
		ValidObservation ob = new ValidObservation();
		ObservationValidationWarning e = new ObservationValidationWarning("warn", ob);
		assertEquals("warn", e.getMessage());
		assertSame(ob, e.getObservation());
	}

	public void testObservationValidationWarningNoMessage() {
		ValidObservation ob = new ValidObservation();
		ObservationValidationWarning e = new ObservationValidationWarning(ob);
		assertNull(e.getMessage());
		assertSame(ob, e.getObservation());
	}

	public void testUnknownStarError() {
		UnknownStarError e = new UnknownStarError("XY Tau");
		assertTrue(e.getMessage().contains("XY Tau"));
		assertTrue(e.getMessage().toLowerCase().contains("unknown"));
	}

	public void testUnknownAUIDError() {
		UnknownAUIDError e = new UnknownAUIDError("000-BCD-123");
		assertTrue(e.getMessage().contains("000-BCD-123"));
		assertTrue(e.getMessage().toLowerCase().contains("auid"));
	}
}
