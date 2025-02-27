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

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.PiecewiseLinearModel.LinearFunction;
import org.aavso.tools.vstar.external.lib.PiecewiseLinearModel.PiecewiseLinearFunction;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.util.Tolerance;

import junit.framework.TestCase;

/**
 * Unit tests for piecewise linear model plug-in library.
 */
public class PiecewiseLinearModelTest extends TestCase {

    public PiecewiseLinearModelTest(String name) {
        super(name);
    }

    public void testLinearFunction() {
        double DELTA = 1e-6;

        LinearFunction function = new LinearFunction(2459645, 2459640, 10, 12.5);

        double m = -0.5;
        assertTrue(Tolerance.areClose(m, function.slope(), DELTA, true));
        assertTrue(Tolerance.areClose(10 - (m * 2459645), function.yIntercept(), DELTA, true));
        assertTrue(Tolerance.areClose(m * 2459642 + function.yIntercept(), function.value(2459642), DELTA, true));
    }

    public void testPiecewiseLinearFunction() {
        double DELTA = 1e-6;

        List<ValidObservation> meanObs = getTestMeanObs();
        PiecewiseLinearFunction plf = new PiecewiseLinearFunction(meanObs, JDCoordSource.instance);

        List<ValidObservation> obs = getTestObs();

        double t1 = obs.get(0).getJD();
        LinearFunction function1 = plf.getFunctions().get(0);
        assertTrue(Tolerance.areClose(function1.slope() * t1 + function1.yIntercept(), plf.value(t1), DELTA, true));

        double t2 = obs.get(1).getJD();
        LinearFunction function2 = plf.getFunctions().get(1);
        assertTrue(Tolerance.areClose(function2.slope() * t2 + function2.yIntercept(), plf.value(t2), DELTA, true));
    }

    // Helpers

    private List<ValidObservation> getTestMeanObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        ValidObservation ob1 = new ValidObservation();
        ob1.setDateInfo(new DateInfo(2459644));
        ob1.setMagnitude(new Magnitude(4.5, 0));
        obs.add(ob1);

        ValidObservation ob2 = new ValidObservation();
        ob2.setDateInfo(new DateInfo(2459645.5));
        ob2.setMagnitude(new Magnitude(5.5, 0));
        obs.add(ob2);

        ValidObservation ob3 = new ValidObservation();
        ob3.setDateInfo(new DateInfo(22459645.5));
        ob3.setMagnitude(new Magnitude(5.5, 0));
        obs.add(ob3);

        ValidObservation ob4 = new ValidObservation();
        ob4.setDateInfo(new DateInfo(2459647));
        ob4.setMagnitude(new Magnitude(7, 0));
        obs.add(ob4);

        return obs;
    }

    private List<ValidObservation> getTestObs() {
        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        ValidObservation ob1 = new ValidObservation();
        ob1.setDateInfo(new DateInfo(2459645.1134785));
        ob1.setMagnitude(new Magnitude(5, 0));
        obs.add(ob1);

        ValidObservation ob2 = new ValidObservation();
        ob2.setDateInfo(new DateInfo(2459646.2));
        ob2.setMagnitude(new Magnitude(6, 0));
        obs.add(ob2);

        return obs;
    }
}
