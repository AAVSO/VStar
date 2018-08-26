package example;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;

/**
 * This simple VStar plug-in counts the number of loaded observations.
 */
public class ObservationTool extends ObservationToolPluginBase {

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		int count = 0;

		StringBuilder buf = new StringBuilder();

		for (SeriesType series : seriesInfo.getSeriesKeys()) {
			int n = seriesInfo.getObservations(series).size();
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
