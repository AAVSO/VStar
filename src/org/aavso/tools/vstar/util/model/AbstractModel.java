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

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.ui.model.plot.ICoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.StandardPhaseCoordSource;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.comparator.StandardPhaseComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
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

    protected double sumSqResiduals = 0;

    protected double aic = Double.NaN;
    protected double bic = Double.NaN;
    protected double rms = Double.NaN;

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
     * Collect fit and residual observations and incrementally compute the sum of
     * squared residuals for use in fit metrics.
     * 
     * @param modelValue A model-computed value
     * @param ob The observation (at time t) being modeled
     */
    public void collectObs(double modelValue, ValidObservation ob, String comment) {
        ValidObservation fitOb = new ValidObservation();
        fitOb.setDateInfo(new DateInfo(ob.getJD()));
        if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
            fitOb.setPreviousCyclePhase(ob.getPreviousCyclePhase());
            fitOb.setStandardPhase(ob.getStandardPhase());
        }
        fitOb.setMagnitude(new Magnitude(modelValue, 0));
        fitOb.setBand(SeriesType.Model);
        fitOb.setComments(comment);
        fit.add(fitOb);

        ValidObservation resOb = new ValidObservation();
        resOb.setDateInfo(new DateInfo(ob.getJD()));
        if (Mediator.getInstance().getAnalysisType() == AnalysisType.PHASE_PLOT) {
            resOb.setPreviousCyclePhase(ob.getPreviousCyclePhase());
            resOb.setStandardPhase(ob.getStandardPhase());
        }
        double residual = ob.getMag() - modelValue;
        resOb.setMagnitude(new Magnitude(residual, 0));
        resOb.setBand(SeriesType.Residuals);
        resOb.setComments(comment);
        residuals.add(resOb);
        
        sumSqResiduals += (residual * residual);
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
     * Compute the Bayesian and Aikake Information Criteria (BIC and AIC) fit
     * metrics
     *
     * pre-condition: assumes sum of squared residuals has been computed
     * 
     * @param numberOfEstimatedParams The number of estimated parameters, e.g.
     *                                polynomial degree
     */
    public void informationCriteria(double numberOfEstimatedParams) {
        int n = residuals.size();
        if (n != 0 && sumSqResiduals / n != 0) {
            double commonIC = n * Math.log(sumSqResiduals / n);
            aic = commonIC + 2 * numberOfEstimatedParams;
            bic = commonIC + numberOfEstimatedParams * Math.log(n);
        }
    }

    /**
     * Compute the root mean square.
     * 
     * pre-condition: assumes sum of squared residuals has been computed
     */
    public void rootMeanSquare() {
        rms = Math.sqrt(sumSqResiduals / residuals.size());
    }

    /**
     * Gather fit metrics string.
     * 
     * @throws AlgorithmError
     */
    public void fitMetricsString() throws AlgorithmError {
        String strRepr = functionStrMap.get(LocaleProps.get("MODEL_INFO_FIT_METRICS_TITLE"));

        if (strRepr == null) {
            // Goodness of fit
            if (rms != Double.NaN) {
                strRepr = "RMS: " + NumericPrecisionPrefs.formatOther(rms);
            }

            // Akaike and Bayesean Information Criteria
            if (aic != Double.NaN && bic != Double.NaN) {
                strRepr += "\nAIC: " + NumericPrecisionPrefs.formatOther(aic);
                strRepr += "\nBIC: " + NumericPrecisionPrefs.formatOther(bic);
            }
        }

        functionStrMap.put(LocaleProps.get("MODEL_INFO_FIT_METRICS_TITLE"), strRepr);
    }

    /**
     * @return Aikake Information Criteria
     */
    public double getAIC() {
        return aic;
    }

    /**
     * @return Bayesian Information Criteria
     */
    public double getBIC() {
        return bic;
    }

    /**
     * @return Root mean square
     */
    public double getRMS() {
        return rms;
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
