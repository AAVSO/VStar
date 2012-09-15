package org.aavso.tools.vstar.external.plugin;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This simple VStar plug-in counts the number of loaded observations.
 */
public class ObservationCounter extends ObservationToolPluginBase {

	@Override
	public void invoke(Map<SeriesType, List<ValidObservation>> obsMap) {
		int count = 0;

		StringBuilder buf = new StringBuilder();

		for (SeriesType series : obsMap.keySet()) {
			int n = obsMap.get(series).size();
			buf.append(series.getDescription() + ": " + n);
			buf.append("\n");
			count += n;
		}

		MessageBox.showMessageDialog("Observation Count", String.format(
				"There are %d observations in the dataset.\n\n%s", count, buf
						.toString()));
	}

	@Override
	public String getDescription() {
		return "Observation counting tool plugin";
	}

	@Override
	public String getDisplayName() {
		return "Observation Counter";
	}
}
