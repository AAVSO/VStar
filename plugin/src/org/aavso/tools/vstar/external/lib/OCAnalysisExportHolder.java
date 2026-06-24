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
package org.aavso.tools.vstar.external.lib;

import org.aavso.tools.vstar.external.lib.OCAnalysisLib.LinearFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.QuadraticFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Result;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TwoSegmentFit;

/**
 * Holds the most recent O-C analysis results for export via the observation
 * sink plug-in or the results dialog.
 */
public final class OCAnalysisExportHolder {

    public static class Bundle {
        public final Result result;
        public final LinearFit linearFit;
        public final TwoSegmentFit twoSegmentFit;
        public final QuadraticFit quadraticFit;

        public Bundle(Result result, LinearFit linearFit,
                TwoSegmentFit twoSegmentFit, QuadraticFit quadraticFit) {
            this.result = result;
            this.linearFit = linearFit;
            this.twoSegmentFit = twoSegmentFit;
            this.quadraticFit = quadraticFit;
        }
    }

    private static volatile Bundle latest;

    private OCAnalysisExportHolder() {
    }

    public static void setLatest(Bundle bundle) {
        latest = bundle;
    }

    public static Bundle getLatest() {
        return latest;
    }
}
