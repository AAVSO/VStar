package org.aavso.tools.vstar.external.plugin;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

/**
 * A simple example observation source plug-in. All we do here is to generate a
 * a set of observations based upon small variations around a cosine function.
 */
public class ExampleObSource extends ObservationSourcePluginBase {

	@Override
	public String getCurrentStarName() {
		return "Noisy Cosine Star";
	}

	@Override
	public InputType getInputType() {
		return InputType.NONE;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new ObsRetriever();
	}

	@Override
	public String getDescription() {
		return "Get a set of observations from a cosine function plus noise.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from Cosine plus Noise";
	}

	/**
	 * This class is where the real work is done. We create a list of synthetic
	 * observations based upon small variations around a cosine curve and with
	 * random selection between two band types.
	 */
	class ObsRetriever extends AbstractObservationRetriever {

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			double jdBase = 2454000;

			for (int i = 1; i <= 1000 && !wasInterrupted(); i++) {
				double mag = Math.cos(Math.PI / 180 * i) * 3 + Math.random()
						* 2;
				double uncertainty = 0;

				ValidObservation ob = new ValidObservation();
				ob.setDateInfo(new DateInfo(jdBase + i));
				ob.setMagnitude(new Magnitude(mag, uncertainty));
				ob.setBand(Math.random() < 0.5 ? SeriesType.Visual
						: SeriesType.Johnson_V);
				ob.setRecordNumber(i);

				collectObservation(ob);
			}
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "Example Observation Source";
		}
	}
}
