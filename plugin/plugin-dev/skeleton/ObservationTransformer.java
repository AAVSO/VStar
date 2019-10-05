package org.aavso.tools.vstar.example.plugin;

import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.ObservationTransformerPluginBase;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;

/**
 * Observation transformer
 */
public class ObservationTransformer extends ObservationTransformerPluginBase {

	@Override
	public IUndoableAction createAction(ISeriesInfoProvider seriesInfo,
			Set<SeriesType> series) {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Get the display name for this plugin, e.g. for a menu item.
     */
	@Override
	public String getDisplayName() {
		return null;
	}

    /**
     * Get a description of this plugin, e.g. for display in a plugin manager.
     */
	@Override
	public String getDescription() {
		return null;
	}
}
