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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EventType;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.ImportedTiming;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.ImportFileMetadata;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Parameters;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Point;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.QuadraticFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.Result;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TimingMethod;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.LinearFit;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.TwoSegmentFit;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.NumberFieldBase;
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
import org.aavso.tools.vstar.util.help.Help;
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
public class OCAnalysisTool extends GeneralToolPluginBase {

    private static final String DOC_NAME = "OCAnalysis.md";

    private static final String EPHEMERIS_PHASE = "Phase plot";
    private static final String EPHEMERIS_PHASE_TOOLTIP =
            "Period and epoch from the active phase plot. Set via Phase Plot or "
                    + "Period Analysis → New Phase Plot.";
    private static final String EPHEMERIS_STAR = "Star metadata";
    private static final String EPHEMERIS_MANUAL = "Manual entry";

    private static final String DATA_OBSERVATIONS = "From observations";
    private static final String DATA_IMPORTED = "Imported timings file";

    private File lastImportFile;

    @Override
    public void invoke() {
        EphemerisDefaults defaults = resolveEphemerisDefaults();
        IModel selectedModel = currentModel();

        List<String> dataSources = Arrays.asList(DATA_OBSERVATIONS,
                DATA_IMPORTED);
        SelectableTextField dataSourceField = new SelectableTextField(
                "Data source", dataSources, DATA_OBSERVATIONS);

        List<String> ephemerisSources = Arrays.asList(EPHEMERIS_PHASE,
                EPHEMERIS_STAR, EPHEMERIS_MANUAL);
        SelectableTextField ephemerisSourceField = new SelectableTextField(
                "Ephemeris source", ephemerisSources, defaults.sourceLabel);
        ephemerisSourceField.getUIComponent().setToolTipText(
                EPHEMERIS_PHASE_TOOLTIP);

        DoubleField periodField = new DoubleField("Period (days)", 0.0, null,
                defaults.period > 0 ? defaults.period : null);
        DoubleField epochField = new DoubleField("Epoch", null, null,
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

        dataSourceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyDataSourceFields(dataSourceField.getValue(),
                        ephemerisSourceField, timingField, meanPercentField,
                        minObsField, periodField, epochField, eventField);
            }
        });
        applyDataSourceFields(dataSourceField.getValue(), ephemerisSourceField,
                timingField, meanPercentField, minObsField, periodField,
                epochField, eventField);

        List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
        fields.add(dataSourceField);
        fields.add(ephemerisSourceField);
        fields.add(periodField);
        fields.add(epochField);
        fields.add(eventField);
        fields.add(timingField);
        fields.add(meanPercentField);
        fields.add(minObsField);

        ParameterDialog paramDlg = new ParameterDialog(getDisplayName(), fields,
                dataSourceField);
        if (paramDlg.isCancelled()) {
            return;
        }

        boolean fromImportedTimings = DATA_IMPORTED
                .equals(dataSourceField.getValue());

        List<String> importLines = null;
        if (fromImportedTimings) {
            try {
                importLines = readImportFileLines();
            } catch (IOException ex) {
                MessageBox.showErrorDialog(getDisplayName(), ex.getMessage());
                return;
            }
            if (importLines == null) {
                return;
            }
        }

        Double period;
        Double epoch;
        EventType eventType;
        TimingMethod timingMethod;
        Integer meanPercent;
        Integer minObs;

        if (fromImportedTimings) {
            period = periodField.getValue();
            epoch = epochField.getValue();
            eventType = eventTypeFromLabel(eventField.getValue());
            timingMethod = TimingMethod.PARABOLIC;
            meanPercent = 10;
            minObs = 1;
            if (importLines != null) {
                ImportFileMetadata meta = OCAnalysisLib
                        .parseImportFileMetadata(importLines);
                if (meta.period != null && meta.period > 0) {
                    period = meta.period;
                }
                if (meta.epoch != null) {
                    epoch = meta.epoch;
                }
                if (meta.eventType != null) {
                    eventType = meta.eventType;
                }
            }
        } else {
            period = periodField.getValue();
            epoch = epochField.getValue();
            eventType = eventTypeFromLabel(eventField.getValue());
            timingMethod = timingMethodFromLabel(timingField.getValue());
            meanPercent = meanPercentField.getValue();
            minObs = minObsField.getValue();
        }

        if (period == null || period <= 0 || epoch == null) {
            MessageBox.showErrorDialog(getDisplayName(),
                    fromImportedTimings
                            ? "A positive period and an epoch are required "
                                    + "(enter in the dialog or use an O-C export CSV "
                                    + "with # period= and epoch= comments)."
                            : "A positive period and an epoch are required.");
            return;
        }

        if (eventType == null || meanPercent == null
                || (!fromImportedTimings && (timingMethod == null
                        || minObs == null))) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "One or more parameters are invalid.");
            return;
        }

        String resultLabel;
        List<ValidObservation> obs = null;
        if (fromImportedTimings) {
            resultLabel = resolveStarLabel();
        } else {
            ISeriesInfoProvider seriesInfo = currentSeriesInfo();
            if (seriesInfo == null) {
                MessageBox.showErrorDialog(getDisplayName(),
                        "No observations are loaded. Choose imported timings "
                                + "or load a light curve first.");
                return;
            }
            ObservationAndMeanPlotModel plotModel = Mediator.getInstance()
                    .getObservationPlotModel(AnalysisType.RAW_DATA);
            SingleSeriesSelectionDialog seriesDlg = new SingleSeriesSelectionDialog(
                    plotModel);
            if (seriesDlg.isCancelled()) {
                return;
            }
            SeriesType series = seriesDlg.getSeries();
            obs = seriesInfo.getObservations(series);
            if (obs == null || obs.isEmpty()) {
                MessageBox.showErrorDialog(getDisplayName(),
                        "The selected series has no observations.");
                return;
            }
            resultLabel = series.getDescription();
        }

        Result result;
        Parameters params;
        try {
            if (fromImportedTimings) {
                List<ImportedTiming> timings = OCAnalysisLib
                        .parseImportedTimings(importLines, epoch, period);
                IModel model = null;
                params = new Parameters(period, epoch, eventType,
                        TimingMethod.PARABOLIC, meanPercent, 1, model);
                result = OCAnalysisLib.analyzeImported(timings, params);
                if (lastImportFile != null) {
                    resultLabel = resultLabel + " (" + lastImportFile.getName()
                            + ")";
                }
            } else {
                IModel model = timingMethod == TimingMethod.FROM_MODEL
                        ? selectedModel : null;
                params = new Parameters(period, epoch, eventType, timingMethod,
                        meanPercent, minObs, model);
                result = OCAnalysisLib.analyze(obs, params);
            }
        } catch (IllegalArgumentException ex) {
            MessageBox.showErrorDialog(getDisplayName(), ex.getMessage());
            return;
        } catch (IOException ex) {
            MessageBox.showErrorDialog(getDisplayName(), ex.getMessage());
            return;
        }

        if (result.points.isEmpty()) {
            String message;
            if (fromImportedTimings) {
                message = "No O-C points could be computed from the imported "
                        + "timings file. Check the file format and ephemeris.";
            } else {
                message = "No O-C points could be computed. Try lowering the minimum "
                        + "observations per cycle, then check the ephemeris.";
            }
            MessageBox.showErrorDialog(getDisplayName(), message);
            return;
        }

        LinearFit linearFit = OCAnalysisLib.fitLinear(result.points);
        QuadraticFit quadraticFit = OCAnalysisLib.fitQuadratic(result.points);

        new OCAnalysisResultDialog(resultLabel, result, linearFit,
                quadraticFit);
    }

    private static void applyDataSourceFields(String dataSource,
            SelectableTextField ephemerisSourceField,
            SelectableTextField timingField, IntegerField meanPercentField,
            IntegerField minObsField, DoubleField periodField,
            DoubleField epochField, SelectableTextField eventField) {
        boolean fromObs = DATA_OBSERVATIONS.equals(dataSource);
        boolean fromImport = DATA_IMPORTED.equals(dataSource);

        setFieldEnabled(ephemerisSourceField, fromObs);
        setFieldEnabled(timingField, fromObs);
        setFieldEnabled(meanPercentField, fromObs);
        setFieldEnabled(minObsField, fromObs);
        setFieldEnabled(eventField, fromObs || fromImport);
        setFieldEnabled(periodField, fromObs || fromImport);
        setFieldEnabled(epochField, fromObs || fromImport);
    }

    private static void setFieldEnabled(ITextComponent<?> field,
            boolean enabled) {
        field.getUIComponent().setEnabled(enabled);
    }

    /**
     * Compact parameter dialog: labels in a left column, controls on the right
     * (avoids the extra vertical space of per-field titled borders).
     */
    @SuppressWarnings("serial")
    private static class ParameterDialog extends AbstractOkCancelDialog {

        private final List<ITextComponent<?>> fields;
        private final SelectableTextField dataSourceField;

        ParameterDialog(String title, List<ITextComponent<?>> fields,
                SelectableTextField dataSourceField) {
            super(title);
            this.fields = fields;
            this.dataSourceField = dataSourceField;

            Container contentPane = getContentPane();
            JPanel topPane = new JPanel();
            topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
            topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            topPane.add(createParameterPane());
            topPane.add(createButtonPane2());
            contentPane.add(topPane);

            pack();
            setLocationRelativeTo(Mediator.getUI().getContentPane());
            setVisible(true);
        }

        private JPanel createParameterPane() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.anchor = GridBagConstraints.EAST;
            labelConstraints.insets = new Insets(2, 0, 2, 6);

            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(2, 0, 2, 0);

            int row = 0;
            for (ITextComponent<?> field : fields) {
                labelConstraints.gridy = row;
                fieldConstraints.gridy = row;

                panel.add(new JLabel(field.getName() + ":"), labelConstraints);

                JComponent comp = field.getUIComponent();
                comp.setBorder(BorderFactory.createEmptyBorder());
                if (field.isReadOnly()) {
                    field.setEditable(false);
                }
                tightenFieldHeight(comp);
                panel.add(comp, fieldConstraints);
                row++;
            }
            return panel;
        }

        private static void tightenFieldHeight(JComponent comp) {
            Dimension pref = comp.getPreferredSize();
            int height = Math.min(pref.height, 24);
            comp.setPreferredSize(new Dimension(pref.width, height));
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        }

        @Override
        protected void helpAction() {
            Help.openPluginHelp(DOC_NAME);
        }

        @Override
        protected void cancelAction() {
            // Default cancelled remains true.
        }

        @Override
        protected void okAction() {
            boolean imported = DATA_IMPORTED.equals(dataSourceField.getValue());
            for (ITextComponent<?> field : fields) {
                if (imported && isOptionalForImportFile(field.getName())) {
                    continue;
                }
                if (field.getValue() == null || !field.canBeEmpty()
                        && field.getStringValue().trim().length() == 0) {
                    String errorMessage = "Invalid value entered in "
                            + field.getName() + ".";
                    if (field instanceof NumberFieldBase<?>) {
                        NumberFieldBase<?> nf = (NumberFieldBase<?>) field;
                        if (nf.getMin() == null && nf.getMax() != null) {
                            errorMessage += "\nOnly values <= " + nf.getMax()
                                    + " allowed.";
                        } else if (nf.getMin() != null && nf.getMax() == null) {
                            errorMessage += "\nOnly values >= " + nf.getMin()
                                    + " allowed.";
                        } else if (nf.getMin() != null && nf.getMax() != null) {
                            errorMessage += "\nOnly values between "
                                    + nf.getMin() + " and " + nf.getMax()
                                    + " allowed.";
                        }
                    }
                    MessageBox.showErrorDialog(this, getTitle(), errorMessage);
                    return;
                }
            }
            cancelled = false;
            setVisible(false);
            dispose();
        }

        private static boolean isOptionalForImportFile(String fieldName) {
            return "Period (days)".equals(fieldName)
                    || "Epoch".equals(fieldName)
                    || "Event".equals(fieldName);
        }
    }

    private static String resolveStarLabel() {
        StarInfo info = latestStarInfo();
        if (info != null && info.getDesignation() != null
                && !info.getDesignation().trim().isEmpty()) {
            return info.getDesignation();
        }
        return "Imported timings";
    }

    private List<String> readImportFileLines() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select imported timings file");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Text files", "txt", "csv", "dat"));
        if (lastImportFile != null) {
            chooser.setSelectedFile(lastImportFile);
        }
        if (chooser.showOpenDialog(DocumentManager.findActiveWindow())
                != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        lastImportFile = chooser.getSelectedFile();
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(
                new FileReader(lastImportFile));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            reader.close();
        }
        return lines;
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

    private static ISeriesInfoProvider currentSeriesInfo() {
        Mediator mediator = Mediator.getInstance();
        if (mediator.getValidObservationCategoryMap() == null) {
            return null;
        }
        return mediator.getObservationPlotModel(mediator.getAnalysisType());
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
        return "O-C (observed minus computed) diagram for times of extrema";
    }

    @Override
    public String getDisplayName() {
        return "O-C";
    }

    @Override
    public String getDocName() {
        return DOC_NAME;
    }

    @Override
    public String getGroup() {
        return "Timing";
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
            TIME("Observed time");

            private final String label;

            XAxisMode(String label) {
                this.label = label;
            }
        }

        private final Result result;
        private final LinearFit linearFit;
        private TwoSegmentFit twoSegmentFit;
        private final QuadraticFit quadraticFit;
        private final YIntervalSeries ocSeries = new YIntervalSeries("O-C");
        private final YIntervalRenderer ocRenderer = createOcRenderer();
        private final XYLineAndShapeRenderer fitRenderer = createFitRenderer();
        private final ChartPanel chartPanel;
        private final JRadioButton cycleAxisButton;
        private final JRadioButton timeAxisButton;
        private final JLabel fitSummaryLabel;
        private final JTextField breakCycleField;

        OCAnalysisResultDialog(String seriesName, Result result,
                LinearFit linearFit, QuadraticFit quadraticFit) {
            super(org.aavso.tools.vstar.ui.mediator.DocumentManager
                    .findActiveWindow(), "O-C: " + seriesName,
                    ModalityType.MODELESS);
            this.result = result;
            this.linearFit = linearFit;
            this.twoSegmentFit = null;
            this.quadraticFit = quadraticFit;
            fitSummaryLabel = new JLabel(buildFitSummaryHtml());
            fitSummaryLabel.setVerticalAlignment(SwingConstants.TOP);
            breakCycleField = new JTextField(6);
            breakCycleField.setBorder(BorderFactory.createTitledBorder(
                    "Break cycle (optional)"));
            breakCycleField.setToolTipText(
                    "Cycle number where the O-C trend appears to change "
                            + "(Foster ch. 13). Leave blank for a single "
                            + "linear fit only.");

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
            XYPlot plot = chart.getXYPlot();
            plot.setRenderer(0, ocRenderer);
            plot.setRenderer(1, fitRenderer);
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

        private JPanel createFitPane() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel twoSegmentPane = new JPanel();
            twoSegmentPane.setLayout(new BoxLayout(twoSegmentPane,
                    BoxLayout.LINE_AXIS));
            twoSegmentPane.setBorder(BorderFactory.createTitledBorder(
                    "Two-segment fit (optional)"));
            twoSegmentPane.add(breakCycleField);
            twoSegmentPane.add(Box.createHorizontalStrut(8));
            JButton applyTwoSegmentButton = new JButton("Apply");
            applyTwoSegmentButton.setToolTipText(
                    "Fit separate lines before and after the break cycle");
            applyTwoSegmentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyTwoSegmentFit();
                }
            });
            twoSegmentPane.add(applyTwoSegmentButton);
            twoSegmentPane.add(Box.createHorizontalStrut(8));
            int minCycle = minCycle(result.points);
            int maxCycle = maxCycle(result.points);
            JLabel hintLabel = new JLabel(String.format(
                    "Cycles %d–%d; need ≥2 points each side of break",
                    minCycle, maxCycle));
            twoSegmentPane.add(hintLabel);
            panel.add(twoSegmentPane);

            JScrollPane pane = new JScrollPane(fitSummaryLabel);
            pane.setPreferredSize(new Dimension(640, 220));
            pane.getVerticalScrollBar().setUnitIncrement(16);
            panel.add(pane);
            return panel;
        }

        private void applyTwoSegmentFit() {
            String text = breakCycleField.getText().trim();
            if (text.isEmpty()) {
                twoSegmentFit = null;
                fitSummaryLabel.setText(buildFitSummaryHtml());
                refreshChart();
                return;
            }
            int breakCycle;
            try {
                breakCycle = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                MessageBox.showErrorDialog("O-C",
                        "Enter a whole-number cycle for the break, or leave "
                                + "the field blank.");
                return;
            }
            TwoSegmentFit fit = OCAnalysisLib.fitTwoSegment(result.points,
                    breakCycle);
            if (fit == null) {
                MessageBox.showErrorDialog("O-C",
                        "Could not fit two segments at cycle " + breakCycle
                                + ". Need at least four O-C points total and "
                                + "at least two on each side of the break.");
                return;
            }
            twoSegmentFit = fit;
            fitSummaryLabel.setText(buildFitSummaryHtml());
            refreshChart();
        }

        private static int minCycle(List<Point> points) {
            int min = Integer.MAX_VALUE;
            for (Point p : points) {
                if (p.cycle < min) {
                    min = p.cycle;
                }
            }
            return min;
        }

        private static int maxCycle(List<Point> points) {
            int max = Integer.MIN_VALUE;
            for (Point p : points) {
                if (p.cycle > max) {
                    max = p.cycle;
                }
            }
            return max;
        }

        private String buildFitSummaryHtml() {
            StringBuilder buf = new StringBuilder();
            appendHtmlBodyStart(buf);
            if (linearFit != null) {
                appendHtmlSection(buf, "Linear fit (O-C vs cycle)");
                appendLinearFitDetails(buf, linearFit, result.parameters.period);
            } else {
                appendHtmlParagraph(buf, "Not enough points for a linear fit.");
            }
            if (twoSegmentFit != null) {
                appendHtmlSection(buf, "Two-segment fit (break at cycle "
                        + twoSegmentFit.breakCycle + ")");
                appendHtmlSubsection(buf, "First segment");
                appendLinearFitDetails(buf, twoSegmentFit.firstSegment,
                        result.parameters.period);
                appendHtmlSubsection(buf, "Second segment");
                appendLinearFitDetails(buf, twoSegmentFit.secondSegment,
                        result.parameters.period);
                if (Math.abs(twoSegmentFit.firstSegment.slope
                        - twoSegmentFit.secondSegment.slope) > 0) {
                    appendHtmlParagraph(buf,
                            "Different slopes suggest a period change near "
                                    + "cycle " + twoSegmentFit.breakCycle
                                    + " (Foster, ch. 13).");
                } else {
                    appendHtmlParagraph(buf,
                            "Parallel segments suggest an epoch jump with "
                                    + "unchanged period (Foster, ch. 13).");
                }
            }
            if (quadraticFit != null) {
                appendHtmlSection(buf, "Quadratic fit (O-C vs cycle)");
                appendQuadraticFitDetails(buf, quadraticFit);
            }
            appendHtmlSection(buf, "Notes");
            appendHtmlParagraph(buf, OCAnalysisLib.getPeriodScatterWarning());
            appendHtmlParagraph(buf,
                    "A horizontal O-C trend suggests an epoch offset; a "
                            + "linear slope suggests a period correction "
                            + "(Foster, ch. 13).");
            appendHtmlBodyEnd(buf);
            return buf.toString();
        }

        private static void appendHtmlBodyStart(StringBuilder buf) {
            buf.append("<html><body style='width:620px;font-family:sans-serif;"
                    + "font-size:small'>");
        }

        private static void appendHtmlBodyEnd(StringBuilder buf) {
            buf.append("</body></html>");
        }

        private static void appendHtmlSection(StringBuilder buf, String title) {
            buf.append("<p style='margin-top:10px;margin-bottom:2px'><b>")
                    .append(escapeHtml(title)).append("</b></p>");
        }

        private static void appendHtmlSubsection(StringBuilder buf,
                String title) {
            buf.append("<p style='margin-top:4px;margin-bottom:2px;"
                    + "margin-left:12px;font-style:italic'>")
                    .append(escapeHtml(title)).append("</p>");
        }

        private static void appendHtmlParagraph(StringBuilder buf, String text) {
            buf.append("<p style='margin-top:2px;margin-bottom:2px;"
                    + "margin-left:12px'>").append(escapeHtml(text))
                    .append("</p>");
        }

        private static void appendHtmlDetail(StringBuilder buf, String label,
                String value) {
            buf.append("<p style='margin-top:1px;margin-bottom:1px;"
                    + "margin-left:24px'>")
                    .append("<b>").append(escapeHtml(label)).append(":</b> ")
                    .append(escapeHtml(value)).append("</p>");
        }

        private static void appendLinearFitDetails(StringBuilder buf,
                LinearFit fit, double modelPeriod) {
            String slope = formatDays(fit.slope);
            if (Math.abs(fit.slope) > 0) {
                appendHtmlDetail(buf, "Slope", slope + " d/cycle");
                appendHtmlDetail(buf, "Corrected period",
                        formatDays(modelPeriod + fit.slope) + " d (ΔP ≈ "
                                + slope + " d)");
            } else {
                appendHtmlDetail(buf, "Slope",
                        slope + " d/cycle (period matches the ephemeris)");
            }
            appendHtmlDetail(buf, "Intercept / epoch correction",
                    formatDays(fit.intercept) + " d");
            appendHtmlDetail(buf, "RMS", formatDays(fit.rms) + " d");
        }

        private static void appendQuadraticFitDetails(StringBuilder buf,
                QuadraticFit fit) {
            double deltaPPerCycle = 2.0 * fit.quadratic;
            appendHtmlDetail(buf, "Epoch correction",
                    formatDays(fit.constant) + " d");
            appendHtmlDetail(buf, "Linear coefficient",
                    formatDays(fit.linear) + " d/cycle");
            appendHtmlDetail(buf, "Starting period correction",
                    formatDays(fit.linear - fit.quadratic) + " d");
            appendHtmlDetail(buf, "Quadratic coefficient",
                    formatDays(fit.quadratic) + " d/cycle²");
            appendHtmlDetail(buf, "ΔP per cycle",
                    formatDays(deltaPPerCycle)
                            + " d (evolving period, Foster §13.4)");
            appendHtmlDetail(buf, "RMS", formatDays(fit.rms) + " d");
        }

        private static String formatDays(double days) {
            return NumericPrecisionPrefs.formatOther(days);
        }

        private static String escapeHtml(String text) {
            if (text == null) {
                return "";
            }
            return text.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;");
        }

        private String buildSummaryHtml() {
            StringBuilder buf = new StringBuilder();
            appendHtmlBodyStart(buf);
            buf.append("<table cellpadding='2' cellspacing='0' "
                    + "style='margin-left:4px'>");
            appendSummaryRow(buf, "Ephemeris",
                    "period = " + formatDays(result.parameters.period)
                            + " d, epoch = "
                            + NumericPrecisionPrefs.formatTime(
                                    result.parameters.epoch));
            appendSummaryRow(buf, "Data",
                    result.points.size() + " O-C points");
            if (linearFit != null) {
                appendSummaryRow(buf, "Linear fit",
                        "slope = " + formatDays(linearFit.slope)
                                + " d/cycle, intercept = "
                                + formatDays(linearFit.intercept) + " d, RMS = "
                                + formatDays(linearFit.rms) + " d");
            }
            buf.append("</table>");
            appendHtmlBodyEnd(buf);
            return buf.toString();
        }

        private static void appendSummaryRow(StringBuilder buf, String label,
                String value) {
            buf.append("<tr><td valign='top' style='padding-right:12px'>")
                    .append("<b>").append(escapeHtml(label)).append("</b>")
                    .append("</td><td valign='top'>")
                    .append(escapeHtml(value)).append("</td></tr>");
        }

        private JPanel createSummaryPane() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(BorderFactory.createTitledBorder("Summary"));

            JLabel summaryLabel = new JLabel(buildSummaryHtml());
            summaryLabel.setVerticalAlignment(SwingConstants.TOP);
            panel.add(summaryLabel);
            return panel;
        }

        private JPanel createButtonPane(ActionListener dismissListener) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            JButton exportButton = new JButton("Export CSV...");
            exportButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exportCsv();
                }
            });
            panel.add(exportButton);
            panel.add(Box.createHorizontalStrut(10));
            JButton dismissButton = new JButton(
                    LocaleProps.get("DISMISS_BUTTON"));
            dismissButton.addActionListener(dismissListener);
            panel.add(dismissButton);
            return panel;
        }

        private void exportCsv() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export O-C results");
            chooser.setSelectedFile(new File("oc_analysis.csv"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(file);
                OCAnalysisLib.writeCsv(result, writer, linearFit, twoSegmentFit,
                        quadraticFit);
            } catch (IOException ex) {
                MessageBox.showErrorDialog("O-C", ex.getMessage());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
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
            plot.getDomainAxis().setLabel(mode.label);

            if (linearFit != null || twoSegmentFit != null) {
                plot.setDataset(1, buildFitSeries(mode, linearFit,
                        twoSegmentFit));
            } else if (plot.getDatasetCount() > 1) {
                plot.setDataset(1, null);
            }
        }

        private static YIntervalRenderer createOcRenderer() {
            YIntervalRenderer renderer = new YIntervalRenderer();
            renderer.setSeriesPaint(0, Color.BLUE);
            renderer.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
            return renderer;
        }

        private static XYLineAndShapeRenderer createFitRenderer() {
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(
                    true, false);
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, new Color(255, 128, 0));
            renderer.setDefaultShapesVisible(false);
            return renderer;
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

        private static final String[] COLUMNS = { "Cycle", "O (time)",
                "C (time)", "O-C (days)", "σ(O-C)", "Obs in cycle" };

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
