package org.aavso.tools.vstar.external.plugin;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This is a simple observation counting VStar tool plug-in.
 */
public class ObservationCounter extends ObservationToolPluginBase {

	@Override
	public void invoke(List<ValidObservation> obs) {
		int count = obs.size();
		MessageBox.showMessageDialog("Observation Count", String.format(
				"There are %d observations in the loaded dataset.", count));
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
