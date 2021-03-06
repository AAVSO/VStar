package example;

import java.util.List;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.util.Pair;

/**
 * This is an example Custom Filter plug-in that demonstrates that any condition
 * can be used to determine whether or not an observation should be included in
 * the filtered subset, including some relationship between observations.
 * 
 * In this example, an observation is included only if the previous observation
 * has a JD whose integer portion is divisible by 9 and the band of the previous
 * and current observations is Johnson V or Johnson B.
 * 
 * This is completely arbitrary and of no particular use, but it serves to
 * demonstrate the condition can be arbitrarily complex.
 */
public class CustomFilter extends CustomFilterPluginBase {

	@Override
	protected Pair<String,String> filter(List<ValidObservation> obs) {
		ValidObservation last = null;

		for (ValidObservation curr : obs) {
			if (last != null) {
				int lastIntegerJD = (int) last.getJD();
				SeriesType lastBand = last.getBand();
				SeriesType currBand = curr.getBand();
				if (lastIntegerJD % 9 == 0
						&& (currBand == SeriesType.Johnson_V || currBand == SeriesType.Johnson_B)
						&& (lastBand == SeriesType.Johnson_V || lastBand == SeriesType.Johnson_B)) {
					addToSubset(curr);
				}
			}
			last = curr;
		}
		
		return new Pair<String,String>("Custom filter test", "Custom filter test");
	}

	@Override
	public String getDescription() {
		return "Includes an observation if some condition holds for the current and previous one";
	}

	@Override
	public String getDisplayName() {
		return "Previous Observation Condition";
	}
}
