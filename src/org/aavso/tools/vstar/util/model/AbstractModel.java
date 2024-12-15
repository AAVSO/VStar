/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.stats.DescStats;

/**
 * Model classes can use this abstract base class to implement common code
 * instead of implementing the IModel interface.
 */
public abstract class AbstractModel implements IModel {

    protected boolean interrupted;
    protected List<ValidObservation> obs;
    protected List<ValidObservation> fit;
    protected List<ValidObservation> residuals;
    protected Map<String, String> functionStrMap;
    protected ICoordSource timeCoordSource;
    protected Comparator<ValidObservation> timeComparator;
    protected double zeroPoint;

    public AbstractModel(List<ValidObservation> obs) {
        this.obs = obs;
        fit = new ArrayList<ValidObservation>();
        residuals = new ArrayList<ValidObservation>();
        functionStrMap = new TreeMap<String, String>();
        interrupted = false;

        // Select time mode (JD or phase).
        switch (Mediator.getInstance().getAnalysisType()) {
        case RAW_DATA:
            timeCoordSource = JDCoordSource.instance;
            timeComparator = JDComparator.instance;
            this.obs = obs;
            zeroPoint = DescStats.calcTimeElementMean(obs, JDTimeElementEntity.instance);
            break;

        case PHASE_PLOT:
            timeCoordSource = StandardPhaseCoordSource.instance;
            timeComparator = StandardPhaseComparator.instance;
            this.obs = new ArrayList<ValidObservation>(obs);
            Collections.sort(this.obs, timeComparator);
            zeroPoint = 0;
            break;
        }
    }

    /**
     * Default behaviour for model run interrupt: set flag.
     */
    @Override
    public void interrupt() {
        interrupted = true;
    }

    /**
     * Return the fitted observations, after having executed the algorithm.
     * 
     * @return A list of observations that represent the fit.
     */
    public List<ValidObservation> getFit() {
        return fit;
    }

    /**
     * Return the residuals as observations, after having executed the algorithm.
     * 
     * @return A list of observations that represent the residuals.
     */
    public List<ValidObservation> getResiduals() {
        return residuals;
    }

    /**
     * Return the list of coefficients that gives rise to the model. May return
     * null.
     * 
     * @return A list of fit coefficients or null if none available.
     */
    public List<PeriodFitParameters> getParameters() {
        return null;
    }

    /**
     * Does this model have a function-based description?
     * 
     * @return True or false.
     */
    public boolean hasFuncDesc() {
        return false;
    }

    /**
     * Return a mapping from names to strings representing model functions.
     * 
     * @return The model function string map.
     */
    public Map<String, String> getFunctionStrings() {
        return functionStrMap;
    }

    /**
     * Returns the model function and context. This is required for creating a line
     * plot to show the model as a continuous function. If a model creator cannot
     * sensibly return such a function, it may return null and no such plot will be
     * possible. It could also be useful for analysis purposes, e.g. analytic
     * extrema finding.<br/>
     * 
     * @return The function object.
     */
    public ContinuousModelFunction getModelFunction() {
        return null;
    }

    /**
     * Return a human-readable description of this model.
     * 
     * @return The model description.
     */
    abstract public String getDescription();

    /**
     * Return a human-readable 'kind' string (e.g. "Model", "Polynomial Fit")
     * indicating what kind of a model this is.
     * 
     * @return The 'kind' string.
     */
    abstract public String getKind();
}
