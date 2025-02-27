/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.external.plugin;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.PiecewiseLinearModel;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.model.AbstractModel;

/**
 * This plug-in creates a piecewise linear model from the current means series.
 */
public class PiecewiseLinearMeanSeriesModel extends ModelCreatorPluginBase {

    private final String DESC = "Piecewise linear model from Means";

    public PiecewiseLinearMeanSeriesModel() {
        super();
    }

    @Override
    public AbstractModel getModel(List<ValidObservation> obs) {
        // Get the mean observation list for the current mode
        Mediator mediator = Mediator.getInstance();
        ObservationAndMeanPlotModel plotModel = mediator.getObservationPlotModel(mediator.getAnalysisType());

        return new PiecewiseLinearModel(obs, plotModel.getMeanObsList());
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getDisplayName() {
        return DESC;
    }
}