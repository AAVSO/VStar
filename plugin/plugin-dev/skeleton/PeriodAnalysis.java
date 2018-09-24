import java.util.List;

import javax.swing.JDialog;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;

/**
 * Period analysis
 */
public class PeriodAnalysis extends PeriodAnalysisPluginBase {

	/**
	 * Get the display name for this plugin, e.g. for a menu item.
	 */
	@Override
	public String getDisplayName() {
		return "Skeleton period analysis";
	}

	/**
	 * Get a description of this plugin, e.g. for display in a plugin manager.
	 */
	@Override
	public String getDescription() {
		return "Skeleton period analysis";
	}

	/**
	 * Execute a period analysis algorithm instance for this plugin to be
	 * applied to the specified observations.
	 * 
	 * @param obs
	 *            The observations on which to perform the period analysis.
	 * @throws AlgorithmError
	 *             if an error occurs during period analysis.
	 * @throws CancellationException
	 *             if the operation is cancelled.
	 */
	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException {
		// TODO Auto-generated method stub

	}

	/**
	 * Interrupt the execution of the algorithm.
	 */
	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
	}

	/**
	 * Get the period analysis dialog for this plugin.
	 * 
	 * @param sourceSeriesType
	 *            The mean source series type to be used on the plot for display
	 *            purposes.
	 * @return A dialog to be invoked, or null, if there is no dialog.
	 */
	@Override
	public JDialog getDialog(SeriesType sourceSeriesType) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * When a new dataset is loaded, previous computation results and GUI
	 * components should be discarded, so the plugin will listen for such
	 * messages.
	 * 
	 * @param message
	 *            The new star message.
	 */
	@Override
	protected void newStarAction(NewStarMessage message) {
		// TODO Auto-generated method stub
	}

	/**
	 * Reset the plugin, e.g. clear internal algorithm and dialog objects.
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}
}
