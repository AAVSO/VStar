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
import java.util.Optional;

import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoDataset;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoScenario;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.SelectableTextField;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * Observation source that loads Foster-style synthetic light curves for O-C
 * analysis tutorials.
 */
public class OCAnalysisDemoObservationSource extends ObservationSourcePluginBase {

    private static final String DOC_NAME = "OCAnalysis.md";

    private DemoDataset dataset;

    @Override
    public String getDisplayName() {
        return "O-C demo data...";
    }

    @Override
    public String getDescription() {
        return "Synthetic light curves for O-C diagram tutorials (Foster ch. 13)";
    }

    @Override
    public String getDocName() {
        return DOC_NAME;
    }

    @Override
    public String getGroup() {
        return "Timing";
    }

    @Override
    public InputType getInputType() {
        return InputType.NONE;
    }

    @Override
    public AbstractObservationRetriever getObservationRetriever() {
        List<String> labels = new ArrayList<String>();
        for (DemoScenario scenario : DemoScenario.values()) {
            labels.add(scenario.getLabel());
        }
        SelectableTextField scenarioField = new SelectableTextField(
                "Demo scenario", labels,
                DemoScenario.CORRECT_EPHEMERIS.getLabel());

        List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
        fields.add(scenarioField);

        MultiEntryComponentDialog dlg = new MultiEntryComponentDialog(
                getDisplayName(), DOC_NAME, fields, Optional.empty());
        if (dlg.isCancelled()) {
            return null;
        }

        DemoScenario scenario = OCAnalysisDemoData
                .scenarioFromLabel(scenarioField.getValue());
        if (scenario == null) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "The selected demo scenario is invalid.");
            return null;
        }

        dataset = OCAnalysisDemoData.generate(scenario);
        StringBuilder msg = new StringBuilder();
        msg.append(dataset.description);
        msg.append("\n\nSuggested ephemeris for O-C diagram (Foster test ");
        msg.append("theory): P = ");
        msg.append(dataset.modelPeriod);
        msg.append(" d, epoch = ");
        msg.append(dataset.modelEpoch);
        msg.append(" HJD.");
        if (dataset.suggestedBreakCycle != null) {
            msg.append("\nSuggested two-segment break cycle: ");
            msg.append(dataset.suggestedBreakCycle);
            msg.append(".");
        }
        msg.append("\n\nExpected O-C pattern: ");
        msg.append(dataset.expectedPattern);
        MessageBox.showMessageDialog(getDisplayName(), msg.toString());

        return new DemoRetriever(dataset);
    }

    private static class DemoRetriever extends AbstractObservationRetriever {

        private final DemoDataset dataset;

        DemoRetriever(DemoDataset dataset) {
            this.dataset = dataset;
        }

        @Override
        public void retrieveObservations() throws ObservationReadError {
            for (org.aavso.tools.vstar.data.ValidObservation ob : dataset.observations) {
                collectObservation(ob);
            }
        }

        @Override
        public String getSourceName() {
            return dataset.starName;
        }

        @Override
        public String getSourceType() {
            return "O-C demo data";
        }

        @Override
        public StarInfo getStarInfo() {
            return new StarInfo(this, dataset.starName, null, dataset.modelPeriod,
                    dataset.modelEpoch, null, null, null, null, null, null);
        }

        @Override
        public Integer getNumberOfRecords() throws ObservationReadError {
            return dataset.observations.size();
        }
    }
}
