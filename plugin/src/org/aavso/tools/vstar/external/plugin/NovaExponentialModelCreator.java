/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.external.lib.NovaExponentialModel;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.model.AbstractModel;

/**
 * <p>
 * A model creator plug-in that fits a nova decline light curve with an
 * exponential function of the form:
 * </p>
 * 
 * <p>
 * mag(t) = P1 - P2*exp(-P3*(t - t0))
 * </p>
 * 
 * <p>
 * after Kok, Y. 2010, JAAVSO, 38, 193, equation (10), where t0 is the time of
 * the brightest observation. The fit is applied to the observations from the
 * brightest observation onward.
 * </p>
 * 
 * <p>
 * The fitted parameters yield the decline times t2 and t3 used by the MMRD
 * (Maximum Magnitude vs Rate of Decline) nova distance calculator plug-in.
 * </p>
 */
public class NovaExponentialModelCreator extends ModelCreatorPluginBase {

    public NovaExponentialModelCreator() {
        super();
    }

    @Override
    public String getDescription() {
        return "Nova exponential decline model (Kok 2010)";
    }

    @Override
    public String getDisplayName() {
        return "Nova Exponential Decline Model";
    }

    @Override
    public String getDocName() {
        return "NovaExponentialDeclineModel.pdf";
    }

    @Override
    public AbstractModel getModel(List<ValidObservation> obs) {
        return new NovaExponentialModel(obs);
    }

    // Plug-in test

    @Override
    public Boolean test() {
        boolean result = true;

        setTestMode(true);

        try {
            // Synthetic light curve generated from Kok (2010) equation (10)
            // with P1=16, P2=9, P3=0.05/day, t0=2455000, i.e. peak mag 7.0.
            final double P1 = 16;
            final double P2 = 9;
            final double P3 = 0.05;
            final double T0 = 2455000;

            List<ValidObservation> obs = new ArrayList<ValidObservation>();
            for (int day = 0; day <= 120; day += 2) {
                ValidObservation ob = new ValidObservation();
                ob.setJD(T0 + day);
                ob.setMagnitude(new Magnitude(P1 - P2 * Math.exp(-P3 * day), 0));
                obs.add(ob);
            }

            NovaExponentialModel model = (NovaExponentialModel) getModel(obs);
            model.execute();

            result &= Tolerance.areClose(P1, model.getP1(), 1e-4, true);
            result &= Tolerance.areClose(P2, model.getP2(), 1e-4, true);
            result &= Tolerance.areClose(P3, model.getP3(), 1e-6, true);

            // Closed-form decline times: t(delta) = ln(P2/(P2-delta))/P3
            double expectedT2 = Math.log(P2 / (P2 - 2)) / P3;
            double expectedT3 = Math.log(P2 / (P2 - 3)) / P3;
            result &= Tolerance.areClose(expectedT2, model.getT2(), 1e-3, true);
            result &= Tolerance.areClose(expectedT3, model.getT3(), 1e-3, true);
        } catch (AlgorithmError e) {
            result = false;
        } finally {
            setTestMode(false);
        }

        return result;
    }
}
