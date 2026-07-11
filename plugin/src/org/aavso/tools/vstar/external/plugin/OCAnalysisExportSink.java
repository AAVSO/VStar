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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisExportHolder;
import org.aavso.tools.vstar.external.lib.OCAnalysisExportHolder.Bundle;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;

/**
 * Observation sink that exports the most recent O-C diagram results as CSV.
 *
 * <p>
 * Run the O-C diagram tool first so results are available for export.
 * </p>
 */
public class OCAnalysisExportSink extends ObservationSinkPluginBase {

    @Override
    public void save(PrintWriter writer, List<ValidObservation> obs,
            String delimiter) throws IOException {
        Bundle bundle = OCAnalysisExportHolder.getLatest();
        if (bundle == null || bundle.result == null) {
            throw new IOException(
                    "No O-C results are available. Run O-C diagram first.");
        }
        OCAnalysisLib.writeCsv(bundle.result, writer, bundle.linearFit,
                bundle.twoSegmentFit, bundle.quadraticFit);
    }

    @Override
    public Map<String, String> getDelimiterNameValuePairs() {
        Map<String, String> delims = new LinkedHashMap<String, String>();
        delims.put("Comma", ",");
        return delims;
    }

    @Override
    public Map<String, String> getDelimiterSuffixValuePairs() {
        Map<String, String> suffixes = new LinkedHashMap<String, String>();
        suffixes.put("Comma", "csv");
        return suffixes;
    }

    @Override
    public String getDescription() {
        return "Export the most recent O-C diagram results as CSV";
    }

    @Override
    public String getDisplayName() {
        return "O-C diagram CSV export";
    }

    @Override
    public String getDocName() {
        return "OCAnalysis.md";
    }

    @Override
    public String getGroup() {
        return "Timing";
    }
}
