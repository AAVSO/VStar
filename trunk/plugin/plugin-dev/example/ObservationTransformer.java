package example;

import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationTransformerPluginBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This plugin allows the magnitude baseline of a set of observations to be
 * reversibly shifted up or down by a set amount.
 */
public class ObservationTransformer extends ObservationTransformerPluginBase {

	private static final int SHIFT_AMOUNT = 2;
	
	private double shift;
	private boolean firstInvocation;

	public ObservationTransformer() {
		super();
		shift = SHIFT_AMOUNT;
		firstInvocation = true;
	}

	@Override
	public String getDisplayName() {
		return "Magnitude Shifter";
	}

	@Override
	public String getDescription() {
		return "Magnitude Shifter";
	}

	@Override
	public IUndoableAction createAction(ISeriesInfoProvider seriesInfo,
			Set<SeriesType> series) {

		if (firstInvocation) {
			Mediator.getInstance().getNewStarNotifier()
					.addListener(getNewStarListener());

			firstInvocation = false;
		}

		return new IUndoableAction() {
			@Override
			public String getDisplayString() {
				return "shifted magnitude";
			}

			@Override
			public boolean execute(UndoableActionType type) {
				boolean ok = true;
				
				switch (type) {
				case DO:
					// For a do operation, set the shift value.
					shift = SHIFT_AMOUNT;
					break;
					
				case UNDO:
				case REDO:
					// For an undo or a redo operation, negate the shift.
					shift = -shift;
					break;
				}

				if (shift != 0) {
					for (SeriesType seriesType : series) {
						for (ValidObservation ob : seriesInfo
								.getObservations(seriesType)) {
							ob.setMag(ob.getMag() + shift);
						}
					}
				}
				
				return ok;
			}
		};
	}

	/**
	 * Get the new star listener for this plugin.
	 */
	protected Listener<NewStarMessage> getNewStarListener() {
		return new Listener<NewStarMessage>() {
			public void update(NewStarMessage info) {
				shift = SHIFT_AMOUNT;
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
