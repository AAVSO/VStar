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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Parameters;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Point;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Result;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TimingMethod;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.LinearFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TwoSegmentFit;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.SelectableTextField;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.JDCoordSource;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

/**
 * O-C (observed minus computed) analysis tool for times of light-curve extrema.
 *
 * <p>
 * See Grant Foster, "Analyzing Light Curves", chapter 13.
 * </p>
 */
public class OCAnalysisTool extends ObservationToolPluginBase {

    private static final String DOC_NAME = "OCAnalysis.md";

    private static final String EPHEMERIS_PHASE = "Document period (phase plot / PA)";
    private static final String EPHEMERIS_STAR = "Star metadata";
    private static final String EPHEMERIS_MANUAL = "Manual entry";

    @Override
    public void invoke(ISeriesInfoProvider seriesInfo) {
        ObservationAndMeanPlotModel plotModel = Mediator.getInstance()
                .getObservationPlotModel(AnalysisType.RAW_DATA);

        SingleSeriesSelectionDialog seriesDlg = new SingleSeriesSelectionDialog(
                plotModel);
        if (seriesDlg.isCancelled()) {
            return;
        }

        SeriesType series = seriesDlg.getSeries();
        List<ValidObservation> obs = seriesInfo.getObservations(series);
        if (obs == null || obs.isEmpty()) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "The selected series has no observations.");
            return;
        }

        EphemerisDefaults defaults = resolveEphemerisDefaults();
        IModel selectedModel = currentModel();

        List<String> ephemerisSources = Arrays.asList(EPHEMERIS_PHASE,
                EPHEMERIS_STAR, EPHEMERIS_MANUAL);
        SelectableTextField ephemerisSourceField = new SelectableTextField(
                "Ephemeris source", ephemerisSources,
                defaults.sourceLabel, false, false);

        DoubleField periodField = new DoubleField("Period (days)", 0.0, null,
                defaults.period > 0 ? defaults.period : null);
        DoubleField epochField = new DoubleField("Epoch (HJD)", null, null,
                defaults.epoch != 0 ? defaults.epoch : null);

        ephemerisSourceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyEphemerisSource(ephemerisSourceField.getValue(),
                        periodField, epochField);
            }
        });

        List<String> eventLabels = new ArrayList<String>();
        for (EventType type : EventType.values()) {
            eventLabels.add(type.getLabel());
        }
        SelectableTextField eventField = new SelectableTextField("Event",
                eventLabels, EventType.MAXIMUM.getLabel());

        List<String> timingLabels = new ArrayList<String>();
        for (TimingMethod method : TimingMethod.values()) {
            if (method == TimingMethod.FROM_MODEL && !isModelTimingAvailable(selectedModel)) {
                continue;
            }
            timingLabels.add(method.getLabel());
        }
        SelectableTextField timingField = new SelectableTextField(
                "Timing method", timingLabels,
                TimingMethod.PARABOLIC.getLabel());

        IntegerField meanPercentField = new IntegerField(
                "Extreme N% (mean timing method)", 1, 100, 10);
        IntegerField minObsField = new IntegerField(
                "Minimum observations per cycle", 1, null, 3);
        IntegerField breakCycleField = new IntegerField(
                "Two-segment break cycle (optional)", null, null, null);

        List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
        fields.add(ephemerisSourceField);
        fields.add(periodField);
        fields.add(epochField);
        fields.add(eventField);
        fields.add(timingField);
        fields.add(meanPercentField);
        fields.add(minObsField);
        fields.add(breakCycleField);

        MultiEntryComponentDialog paramDlg = new MultiEntryComponentDialog(
                getDisplayName(), DOC_NAME, fields, Optional.empty());
        if (paramDlg.isCancelled()) {
            return;
        }

        Double period = periodField.getValue();
        Double epoch = epochField.getValue();
        if (period == null || period <= 0 || epoch == null) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "A positive period and an epoch (HJD) are required.");
            return;
        }

        EventType eventType = eventTypeFromLabel(eventField.getValue());
        TimingMethod timingMethod = timingMethodFromLabel(timingField.getValue());
        Integer meanPercent = meanPercentField.getValue();
        Integer minObs = minObsField.getValue();

        if (eventType == null || timingMethod == null || meanPercent == null
                || minObs == null) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "One or more parameters are invalid.");
            return;
        }

        Parameters params;
        try {
            IModel model = timingMethod == TimingMethod.FROM_MODEL
                    ? selectedModel : null;
            params = new Parameters(period, epoch, eventType, timingMethod,
                    meanPercent, minObs, model);
        } catch (IllegalArgumentException ex) {
            MessageBox.showErrorDialog(getDisplayName(), ex.getMessage());
            return;
        }

        Result result = OCAnalysisLib.analyze(obs, params);
        if (result.points.isEmpty()) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "No O-C points could be computed. Try lowering the minimum "
                            + "observations per cycle, then check the ephemeris.");
            return;
        }

        LinearFit linearFit = OCAnalysisLib.fitLinear(result.points);
        TwoSegmentFit twoSegmentFit = null;
        Integer breakCycle = breakCycleField.getValue();
        if (breakCycle != null) {
            twoSegmentFit = OCAnalysisLib.fitTwoSegment(result.points,
                    breakCycle);
        }

        new OCAnalysisResultDialog(series.getDescription(), result, linearFit,
                twoSegmentFit);
    }

    private static boolean isModelTimingAvailable(IModel model) {
        if (model == null || !model.hasFuncDesc()) {
            return false;
        }
        return model.getModelFunction() != null
                && model.getModelFunction().getCoordSrc() == JDCoordSource.instance;
    }

    private static IModel currentModel() {
        ModelSelectionMessage msg = Mediator.getInstance()
                .getModelSelectionMessage();
        return msg != null ? msg.getModel() : null;
    }

    private static void applyEphemerisSource(String source,
            DoubleField periodField, DoubleField epochField) {
        EphemerisDefaults d = resolveEphemerisDefaults();
        if (EPHEMERIS_PHASE.equals(source)) {
            DocumentManager dm = Mediator.getInstance().getDocumentManager();
            if (dm.getPeriod() > 0) {
                periodField.setValue(dm.getPeriod());
                epochField.setValue(dm.getEpoch());
            }
        } else if (EPHEMERIS_STAR.equals(source)) {
            StarInfo info = latestStarInfo();
            if (info != null) {
                if (info.getPeriod() != null && info.getPeriod() > 0) {
                    periodField.setValue(info.getPeriod());
                }
                if (info.getEpoch() != null) {
                    epochField.setValue(info.getEpoch());
                }
            }
        } else if (EPHEMERIS_MANUAL.equals(source)) {
            if (d.period > 0) {
                periodField.setValue(d.period);
            }
            if (d.epoch != 0) {
                epochField.setValue(d.epoch);
            }
        }
    }

    private static EphemerisDefaults resolveEphemerisDefaults() {
        DocumentManager dm = Mediator.getInstance().getDocumentManager();
        if (dm.getPeriod() > 0) {
            return new EphemerisDefaults(EPHEMERIS_PHASE, dm.getPeriod(),
                    dm.getEpoch());
        }

        StarInfo info = latestStarInfo();
        if (info != null && info.getPeriod() != null && info.getPeriod() > 0) {
            double epoch = info.getEpoch() != null ? info.getEpoch() : 0;
            return new EphemerisDefaults(EPHEMERIS_STAR, info.getPeriod(),
                    epoch);
        }

        return new EphemerisDefaults(EPHEMERIS_MANUAL, 0, 0);
    }

    private static StarInfo latestStarInfo() {
        List<NewStarMessage> msgs = Mediator.getInstance()
                .getNewStarMessageList();
        if (msgs.isEmpty()) {
            return null;
        }
        return msgs.get(msgs.size() - 1).getStarInfo();
    }

    private static EventType eventTypeFromLabel(String label) {
        for (EventType type : EventType.values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return null;
    }

    private static TimingMethod timingMethodFromLabel(String label) {
        for (TimingMethod method : TimingMethod.values()) {
            if (method.getLabel().equals(label)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "O-C (observed minus computed) analysis for times of extrema";
    }

    @Override
    public String getDisplayName() {
        return "O-C Analysis";
    }

    @Override
    public String getDocName() {
        return DOC_NAME;
    }

    @Override
    public String getGroup() {
        return "Timing Analysis";
    }

    private static final class EphemerisDefaults {
        final String sourceLabel;
        final double period;
        final double epoch;

        EphemerisDefaults(String sourceLabel, double period, double epoch) {
            this.sourceLabel = sourceLabel;
            this.period = period;
            this.epoch = epoch;
        }
    }

    @SuppressWarnings("serial")
    private static class OCAnalysisResultDialog extends JDialog {

        private enum XAxisMode {
            CYCLE("Cycle number"),
            TIME("Observed time (HJD)");

            private final String label;

            XAxisMode(String label) {
                this.label = label;
            }
        }

        private final Result result;
        private final LinearFit linearFit;
        private final TwoSegmentFit twoSegmentFit;
        private final YIntervalSeries ocSeries = new YIntervalSeries("O-C");
        private final ChartPanel chartPanel;
        private final JRadioButton cycleAxisButton;
        private final JRadioButton timeAxisButton;

        OCAnalysisResultDialog(String seriesName, Result result,
                LinearFit linearFit, TwoSegmentFit twoSegmentFit) {
            super(org.aavso.tools.vstar.ui.mediator.DocumentManager
                    .findActiveWindow(), "O-C Analysis: " + seriesName,
                    ModalityType.MODELESS);
            this.result = result;
            this.linearFit = linearFit;
            this.twoSegmentFit = twoSegmentFit;
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ActionListener dismissListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            };
            getRootPane().registerKeyboardAction(dismissListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            cycleAxisButton = new JRadioButton(XAxisMode.CYCLE.label, true);
            timeAxisButton = new JRadioButton(XAxisMode.TIME.label, false);
            ButtonGroup axisGroup = new ButtonGroup();
            axisGroup.add(cycleAxisButton);
            axisGroup.add(timeAxisButton);

            ItemListener axisListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        refreshChart();
                    }
                }
            };
            cycleAxisButton.addItemListener(axisListener);
            timeAxisButton.addItemListener(axisListener);

            JFreeChart chart = ChartFactory.createScatterPlot(
                    "O-C diagram", XAxisMode.CYCLE.label, "O-C (days)",
                    new YIntervalSeriesCollection(), PlotOrientation.VERTICAL,
                    false, true, false);
            chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(640, 360));

            refreshChart();

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("O-C diagram", createChartPane());
            tabs.addTab("Data table", createTablePane());
            tabs.addTab("Fit summary", createFitPane());

            Container contentPane = getContentPane();
            JPanel topPane = new JPanel();
            topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
            topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            topPane.add(tabs);
            topPane.add(createSummaryPane());
            topPane.add(createButtonPane(dismissListener));
            contentPane.add(topPane);

            pack();
            setLocationRelativeTo(Mediator.getUI().getContentPane());
            setVisible(true);
        }

        private JPanel createChartPane() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel axisPane = new JPanel();
            axisPane.setLayout(new BoxLayout(axisPane, BoxLayout.LINE_AXIS));
            axisPane.setBorder(BorderFactory.createTitledBorder("X axis"));
            axisPane.add(cycleAxisButton);
            axisPane.add(Box.createHorizontalStrut(10));
            axisPane.add(timeAxisButton);
            panel.add(axisPane);
            panel.add(chartPanel);
            return panel;
        }

        private JScrollPane createTablePane() {
            JTable table = new JTable(new OCTableModel(result.points));
            table.setCellSelectionEnabled(true);
            JScrollPane pane = new JScrollPane(table);
            pane.setPreferredSize(new Dimension(640, 240));
            return pane;
        }

        private JScrollPane createFitPane() {
            StringBuilder buf = new StringBuilder("<html>");
            if (linearFit != null) {
                buf.append("<p>");
                buf.append(OCAnalysisLib.interpretLinearFit(linearFit,
                        result.parameters.period));
                buf.append("</p>");
            } else {
                buf.append("<p>Not enough points for a linear fit.</p>");
            }
            if (twoSegmentFit != null) {
                buf.append("<p>");
                buf.append(OCAnalysisLib.interpretTwoSegmentFit(twoSegmentFit,
                        result.parameters.period));
                buf.append("</p>");
            }
            buf.append("<p>A horizontal O-C trend suggests an epoch offset; a "
                    + "linear slope suggests a period correction (Foster, ch. 13).</p>");
            buf.append("</html>");
            javax.swing.JLabel label = new javax.swing.JLabel(buf.toString());
            JScrollPane pane = new JScrollPane(label);
            pane.setPreferredSize(new Dimension(640, 240));
            return pane;
        }

        private JPanel createSummaryPane() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(BorderFactory.createTitledBorder("Summary"));

            StringBuilder buf = new StringBuilder();
            buf.append("Points: ");
            buf.append(result.points.size());
            buf.append("; period = ");
            buf.append(NumericPrecisionPrefs.formatOther(result.parameters.period));
            buf.append(" d; epoch = ");
            buf.append(NumericPrecisionPrefs.formatTime(result.parameters.epoch));
            buf.append(" HJD.");
            if (linearFit != null) {
                buf.append(" Slope = ");
                buf.append(NumericPrecisionPrefs.formatOther(linearFit.slope));
                buf.append(" d/cycle; intercept = ");
                buf.append(NumericPrecisionPrefs.formatOther(linearFit.intercept));
                buf.append(" d.");
            }

            panel.add(new javax.swing.JLabel(buf.toString()));
            return panel;
        }

        private JPanel createButtonPane(ActionListener dismissListener) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            JButton dismissButton = new JButton(
                    LocaleProps.get("DISMISS_BUTTON"));
            dismissButton.addActionListener(dismissListener);
            panel.add(dismissButton);
            return panel;
        }

        private void refreshChart() {
            XAxisMode mode = cycleAxisButton.isSelected() ? XAxisMode.CYCLE
                    : XAxisMode.TIME;
            ocSeries.clear();
            for (Point p : result.points) {
                double x = mode == XAxisMode.CYCLE ? p.cycle : p.observedTime;
                double yLow = p.oc;
                double yHigh = p.oc;
                if (!Double.isNaN(p.ocUncertainty) && p.ocUncertainty > 0) {
                    yLow = p.oc - p.ocUncertainty;
                    yHigh = p.oc + p.ocUncertainty;
                }
                ocSeries.add(x, p.oc, yLow, yHigh);
            }

            YIntervalSeriesCollection dataCollection = new YIntervalSeriesCollection();
            dataCollection.addSeries(ocSeries);

            JFreeChart chart = chartPanel.getChart();
            XYPlot plot = chart.getXYPlot();
            plot.setDataset(0, dataCollection);
            plot.setRenderer(0, new YIntervalRenderer());
            plot.getDomainAxis().setLabel(mode.label);

            if (linearFit != null || twoSegmentFit != null) {
                XYSeriesCollection fitCollection = buildFitSeries(mode, linearFit,
                        twoSegmentFit);
                plot.setDataset(1, fitCollection);
                XYLineAndShapeRenderer fitRenderer = new XYLineAndShapeRenderer(
                        true, false);
                plot.setRenderer(1, fitRenderer);
            } else if (plot.getDatasetCount() > 1) {
                plot.setDataset(1, null);
            }
        }

        private XYSeriesCollection buildFitSeries(XAxisMode mode,
                LinearFit singleFit, TwoSegmentFit segmentFit) {
            XYSeriesCollection collection = new XYSeriesCollection();
            if (segmentFit != null) {
                collection.addSeries(buildSegmentSeries("First segment",
                        segmentFit.firstSegment, result.points, mode, true,
                        segmentFit.breakCycle));
                collection.addSeries(buildSegmentSeries("Second segment",
                        segmentFit.secondSegment, result.points, mode, false,
                        segmentFit.breakCycle));
            } else if (singleFit != null) {
                collection.addSeries(buildFullSeries("Linear fit", singleFit,
                        result.points, mode));
            }
            return collection;
        }

        private XYSeries buildFullSeries(String name, LinearFit fit,
                List<Point> points, XAxisMode mode) {
            XYSeries series = new XYSeries(name);
            if (points.isEmpty()) {
                return series;
            }
            int minCycle = points.get(0).cycle;
            int maxCycle = points.get(points.size() - 1).cycle;
            addFitPoint(series, fit, minCycle, points.get(0), mode);
            addFitPoint(series, fit, maxCycle, points.get(points.size() - 1),
                    mode);
            return series;
        }

        private XYSeries buildSegmentSeries(String name, LinearFit fit,
                List<Point> points, XAxisMode mode, boolean firstSegment,
                int breakCycle) {
            XYSeries series = new XYSeries(name);
            Point start = null;
            Point end = null;
            for (Point p : points) {
                boolean inSegment = firstSegment ? p.cycle <= breakCycle
                        : p.cycle > breakCycle;
                if (!inSegment) {
                    continue;
                }
                if (start == null) {
                    start = p;
                }
                end = p;
            }
            if (start != null && end != null) {
                addFitPoint(series, fit, start.cycle, start, mode);
                addFitPoint(series, fit, end.cycle, end, mode);
            }
            return series;
        }

        private void addFitPoint(XYSeries series, LinearFit fit, int cycle,
                Point anchor, XAxisMode mode) {
            double x = mode == XAxisMode.CYCLE ? cycle : anchor.observedTime;
            double y = fit.evaluate(cycle);
            series.add(x, y);
        }
    }

    private static class OCTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = { "Cycle", "O (HJD)",
                "C (HJD)", "O-C (days)", "σ(O-C)", "Obs in cycle" };

        private final List<Point> points;

        OCTableModel(List<Point> points) {
            this.points = points;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public int getRowCount() {
            return points.size();
        }

        @Override
        public String getColumnName(int col) {
            return COLUMNS[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Point p = points.get(row);
            switch (col) {
            case 0:
                return p.cycle;
            case 1:
                return NumericPrecisionPrefs.formatTime(p.observedTime);
            case 2:
                return NumericPrecisionPrefs.formatTime(p.computedTime);
            case 3:
                return NumericPrecisionPrefs.formatOther(p.oc);
            case 4:
                return Double.isNaN(p.ocUncertainty) || p.ocUncertainty <= 0
                        ? ""
                        : NumericPrecisionPrefs.formatOther(p.ocUncertainty);
            case 5:
                return p.obsInCycle;
            default:
                return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}
