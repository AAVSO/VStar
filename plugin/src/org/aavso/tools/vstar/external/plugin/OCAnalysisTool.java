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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.Collections;
import java.util.List;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoDataset;
import org.aavso.tools.vstar.external.lib.OCAnalysisDemoData.DemoScenario;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib;
import org.aavso.tools.vstar.external.lib.OCAnalysisLib.EditableTimingsModel;
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
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.ChartPropertiesPrefs;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

/**
 * O-C (observed minus computed) analysis tool for times of light-curve extrema.
 *
 * <p>
 * See AAVSO <em>Variable Star Astronomy</em>, chapter 13 (Grant Foster):
 * {@link OCAnalysisLib#VSA_CHAPTER13_PDF_URL}.
 * </p>
 */
public class OCAnalysisTool extends GeneralToolPluginBase {

    private static final String DOC_NAME = "OCAnalysis.md";

    /**
     * Domain markers currently decorating the raw light curve for O-C timings.
     * Tracked so a re-run or load-new-star can remove them without clearing
     * unrelated plot decorations. O markers are index-aligned with the
     * editable timings list.
     */
    private static final List<ValueMarker> ocOMarkers = new ArrayList<ValueMarker>();
    private static final List<ValueMarker> ocCMarkers = new ArrayList<ValueMarker>();
    private static boolean newStarClearListenerRegistered = false;
    /** Active editor dialog (at most one) so new-star can shut down place mode. */
    private static OCAnalysisResultDialog activeResultDialog;

    private static final String EPHEMERIS_PHASE = "Phase plot";
    private static final String EPHEMERIS_PHASE_TOOLTIP =
            "Period and epoch from the active phase plot. Set via Phase Plot or "
                    + "Period Analysis → New Phase Plot.";
    private static final String EPHEMERIS_STAR = "Star metadata";
    private static final String EPHEMERIS_STAR_TOOLTIP =
            "Period and epoch from the loaded star record (e.g. VSX / AID "
                    + "catalogue fields).";
    private static final String EPHEMERIS_MANUAL = "Manual entry";
    private static final String EPHEMERIS_MANUAL_TOOLTIP =
            "Enter period and epoch yourself.";

    private static final String DATA_OBSERVATIONS = "From observations";
    private static final String DATA_IMPORTED = "Imported timings file";
    private static final String DATA_EDIT_TIMINGS = "Edit timings on light curve";

    /** Pixel radius for O-marker hit-testing. */
    private static final int MARKER_HIT_PIXELS = 8;
    /** Default max |ΔJD| for snap-to-observation (days). */
    private static final double DEFAULT_SNAP_MAX_DAYS = 0.05;

    private File lastImportFile;

    @Override
    public void invoke() {
        EphemerisDefaults defaults = resolveEphemerisDefaults();
        IModel selectedModel = currentModel();

        List<String> dataSources = Arrays.asList(DATA_OBSERVATIONS,
                DATA_IMPORTED, DATA_EDIT_TIMINGS);
        SelectableTextField dataSourceField = new SelectableTextField(
                "Data source", dataSources, DATA_OBSERVATIONS);

        List<String> ephemerisSources = Arrays.asList(EPHEMERIS_PHASE,
                EPHEMERIS_STAR, EPHEMERIS_MANUAL);
        SelectableTextField ephemerisSourceField = new SelectableTextField(
                "Ephemeris source", ephemerisSources, defaults.sourceLabel);
        installEphemerisSourceTooltips(ephemerisSourceField);

        DoubleField periodField = new DoubleField("Period (days)", 0.0, null,
                defaults.period > 0 ? defaults.period : null);
        DoubleField epochField = new DoubleField("Epoch", null, null,
                defaults.epoch != 0 ? defaults.epoch : null);

        ephemerisSourceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyEphemerisSource(ephemerisSourceField.getValue(),
                        periodField, epochField);
                updateEphemerisSourceFieldTooltip(ephemerisSourceField);
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
        timingField.getUIComponent().setToolTipText(
                "Parabolic suits smooth peaks. Kwee–van Woerden is a "
                        + "single-eclipse ToM algorithm: needs well-covered "
                        + "eclipses and period/epoch good enough that roughly "
                        + "one event falls per cycle bin; Recommended min obs "
                        + "per cycle for KvW is ≥ "
                        + OCAnalysisLib.KVW_MIN_POINTS + ".");

        IntegerField meanPercentField = new IntegerField(
                "Extreme N% (mean timing method)", 1, 100, 10);
        IntegerField minObsField = new IntegerField(
                "Minimum observations per cycle", 1, null, 3);
        minObsField.getUIComponent().setToolTipText(
                "Cycles with fewer observations are skipped. For "
                        + "Kwee–van Woerden use at least "
                        + OCAnalysisLib.KVW_MIN_POINTS
                        + " (library floor after eclipse windowing).");
        IntegerField kvwNfoldField = new IntegerField(
                "KvW folds (3, 5, or 7)", 3, 7, 5);
        kvwNfoldField.getUIComponent().setToolTipText(
                "Kwee–van Woerden only: more folds (5–7) improve precision on "
                        + "well-sampled eclipses.");

        dataSourceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyDataSourceFields(dataSourceField.getValue(),
                        ephemerisSourceField, timingField, meanPercentField,
                        minObsField, kvwNfoldField, periodField, epochField,
                        eventField);
            }
        });
        timingField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyTimingMethodFields(dataSourceField.getValue(),
                        timingField, meanPercentField, kvwNfoldField);
            }
        });
        applyDataSourceFields(dataSourceField.getValue(), ephemerisSourceField,
                timingField, meanPercentField, minObsField, kvwNfoldField,
                periodField, epochField, eventField);

        List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
        fields.add(dataSourceField);
        fields.add(ephemerisSourceField);
        fields.add(periodField);
        fields.add(epochField);
        fields.add(eventField);
        fields.add(timingField);
        fields.add(meanPercentField);
        fields.add(kvwNfoldField);
        fields.add(minObsField);

        ParameterDialog paramDlg = new ParameterDialog(getDisplayName(), fields,
                dataSourceField);
        if (paramDlg.isCancelled()) {
            return;
        }

        boolean fromImportedTimings = DATA_IMPORTED
                .equals(dataSourceField.getValue());
        boolean editTimingsOnly = DATA_EDIT_TIMINGS
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
        Integer kvwNfold;

        if (fromImportedTimings || editTimingsOnly) {
            period = periodField.getValue();
            epoch = epochField.getValue();
            eventType = eventTypeFromLabel(eventField.getValue());
            timingMethod = TimingMethod.PARABOLIC;
            meanPercent = 10;
            minObs = 1;
            kvwNfold = 5;
            if (fromImportedTimings && importLines != null) {
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
            kvwNfold = kvwNfoldField.getValue();
            if (kvwNfold == null) {
                kvwNfold = 5;
            }
            if (kvwNfold != 3 && kvwNfold != 5 && kvwNfold != 7) {
                kvwNfold = 5;
            }
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

        if (eventType == null) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "One or more parameters are invalid.");
            return;
        }
        if (!fromImportedTimings && !editTimingsOnly
                && (timingMethod == null || meanPercent == null
                        || minObs == null)) {
            MessageBox.showErrorDialog(getDisplayName(),
                    "One or more parameters are invalid.");
            return;
        }

        String resultLabel;
        List<ValidObservation> obsForSnap = Collections.emptyList();
        SeriesType selectedSeries = null;
        if (fromImportedTimings) {
            resultLabel = resolveStarLabel();
            // Snaps work if user later loads data; no series prompt on import.
        } else if (editTimingsOnly) {
            resultLabel = resolveStarLabel() + " (edit timings)";
            List<ValidObservation> picked = tryPickSeriesForSnap();
            if (picked != null && !picked.isEmpty()) {
                obsForSnap = picked;
                resultLabel = resolveStarLabel();
            }
        } else {
            ISeriesInfoProvider seriesInfo = currentSeriesInfo();
            if (seriesInfo == null) {
                MessageBox.showErrorDialog(getDisplayName(),
                        "No observations are loaded. Choose imported timings, "
                                + "edit timings, or load a light curve first.");
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
            selectedSeries = series;
            obsForSnap = seriesInfo.getObservations(series);
            if (obsForSnap == null || obsForSnap.isEmpty()) {
                MessageBox.showErrorDialog(getDisplayName(),
                        "The selected series has no observations.");
                return;
            }
            resultLabel = series.getDescription();
        }

        Result result;
        Parameters params;
        try {
            if (editTimingsOnly) {
                params = new Parameters(period, epoch, eventType,
                        TimingMethod.PARABOLIC, 10, 1, null, 5);
                result = OCAnalysisLib.analyzeImported(
                        Collections.<ImportedTiming>emptyList(), params);
            } else if (fromImportedTimings) {
                List<ImportedTiming> timings = OCAnalysisLib
                        .parseImportedTimings(importLines, epoch, period);
                params = new Parameters(period, epoch, eventType,
                        TimingMethod.PARABOLIC, meanPercent, 1, null, 5);
                result = OCAnalysisLib.analyzeImported(timings, params);
                if (lastImportFile != null) {
                    resultLabel = resultLabel + " (" + lastImportFile.getName()
                            + ")";
                }
            } else {
                IModel model = timingMethod == TimingMethod.FROM_MODEL
                        ? selectedModel : null;
                params = new Parameters(period, epoch, eventType, timingMethod,
                        meanPercent, minObs, model, kvwNfold);
                result = OCAnalysisLib.analyze(obsForSnap, params);
            }
        } catch (IllegalArgumentException ex) {
            MessageBox.showErrorDialog(getDisplayName(), ex.getMessage());
            return;
        } catch (IOException ex) {
            MessageBox.showErrorDialog(getDisplayName(), ex.getMessage());
            return;
        }

        if (result.points.isEmpty() && !editTimingsOnly) {
            String message;
            if (fromImportedTimings) {
                message = "No O-C points could be computed from the imported "
                        + "timings file. Check the file format and ephemeris.";
            } else if (timingMethod == TimingMethod.KWEE_VAN_WOERDEN) {
                message = "No O-C points from Kwee–van Woerden. Need ≥"
                        + OCAnalysisLib.KVW_MIN_POINTS
                        + " in-eclipse points per cycle, a sensible "
                        + "period/epoch, and Event = Minimum for eclipsing "
                        + "binaries. Place timings manually via "
                        + DATA_EDIT_TIMINGS + " or try Parabolic.";
            } else {
                message = "No O-C points could be computed. Try lowering the minimum "
                        + "observations per cycle, check the ephemeris, or use "
                        + DATA_EDIT_TIMINGS + ".";
            }
            MessageBox.showErrorDialog(getDisplayName(), message);
            return;
        }

        EditableTimingsModel timingsModel = EditableTimingsModel
                .fromResult(result);
        LinearFit linearFit = OCAnalysisLib.fitLinear(result.points);
        QuadraticFit quadraticFit = OCAnalysisLib.fitQuadratic(result.points);

        boolean markersShown = publishTimingDomainMarkers(result);

        Color seriesColor = selectedSeries != null
                ? SeriesType.getColorFromSeries(selectedSeries)
                : Color.BLUE;
        new OCAnalysisResultDialog(resultLabel, result, timingsModel, linearFit,
                quadraticFit, seriesColor, markersShown, obsForSnap,
                editTimingsOnly);
    }

    /**
     * Optional series for snap-to-observation. Empty if cancel or no data.
     */
    private static List<ValidObservation> tryPickSeriesForSnap() {
        ISeriesInfoProvider seriesInfo = currentSeriesInfo();
        if (seriesInfo == null) {
            return Collections.emptyList();
        }
        ObservationAndMeanPlotModel plotModel = Mediator.getInstance()
                .getObservationPlotModel(AnalysisType.RAW_DATA);
        if (plotModel == null) {
            return Collections.emptyList();
        }
        SingleSeriesSelectionDialog seriesDlg = new SingleSeriesSelectionDialog(
                plotModel);
        if (seriesDlg.isCancelled()) {
            return Collections.emptyList();
        }
        List<ValidObservation> obs = seriesInfo
                .getObservations(seriesDlg.getSeries());
        if (obs == null) {
            return Collections.emptyList();
        }
        return obs;
    }

    /**
     * Draw full-height vertical domain markers for O (and lighter dashed
     * markers for C) on the raw-data light curve. Replaces prior O-C markers.
     *
     * @return true if at least one plot was decorated
     */
    private static boolean publishTimingDomainMarkers(Result result) {
        ensureOcTimingNewStarClearListener();
        ObservationAndMeanPlotPane pane = rawPlotPane();
        if (pane == null || pane.getChartPanel() == null
                || pane.getChartPanel().getChart() == null) {
            return false;
        }
        XYPlot plot = pane.getChartPanel().getChart().getXYPlot();
        clearOcTimingMarkers(plot);

        Color oColor = new Color(255, 100, 0, 200);
        Color cColor = new Color(80, 80, 180, 140);
        Color oSelected = new Color(255, 0, 0, 230);
        BasicStroke oStroke = new BasicStroke(1.4f);
        BasicStroke oSelectedStroke = new BasicStroke(2.2f);
        BasicStroke cStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[] { 4f, 4f }, 0f);
        boolean labelCycles = result.points.size() <= 30;
        int selected = activeResultDialog != null
                ? activeResultDialog.getSelectedTimingIndex() : -1;

        for (int i = 0; i < result.points.size(); i++) {
            Point p = result.points.get(i);
            ValueMarker oMark = new ValueMarker(p.observedTime);
            boolean isSel = i == selected;
            oMark.setPaint(isSel ? oSelected : oColor);
            oMark.setStroke(isSel ? oSelectedStroke : oStroke);
            if (labelCycles) {
                oMark.setLabel("O" + p.cycle);
            }
            plot.addDomainMarker(oMark, Layer.FOREGROUND);
            ocOMarkers.add(oMark);

            ValueMarker cMark = new ValueMarker(p.computedTime);
            cMark.setPaint(cColor);
            cMark.setStroke(cStroke);
            if (labelCycles) {
                cMark.setLabel("C" + p.cycle);
            }
            plot.addDomainMarker(cMark, Layer.BACKGROUND);
            ocCMarkers.add(cMark);
        }
        return !result.points.isEmpty();
    }

    private static ObservationAndMeanPlotPane rawPlotPane() {
        return Mediator.getInstance().getPlotPane(AnalysisType.RAW_DATA);
    }

    private static ChartPanel rawChartPanel() {
        ObservationAndMeanPlotPane pane = rawPlotPane();
        return pane != null ? pane.getChartPanel() : null;
    }

    private static void clearOcTimingMarkers(XYPlot plot) {
        if (plot != null) {
            for (ValueMarker m : ocOMarkers) {
                plot.removeDomainMarker(m, Layer.FOREGROUND);
            }
            for (ValueMarker m : ocCMarkers) {
                plot.removeDomainMarker(m, Layer.BACKGROUND);
            }
        }
        ocOMarkers.clear();
        ocCMarkers.clear();
    }

    private static void clearOcTimingMarkersFromRawPlot() {
        ObservationAndMeanPlotPane pane = rawPlotPane();
        if (pane != null && pane.getChartPanel() != null
                && pane.getChartPanel().getChart() != null) {
            clearOcTimingMarkers(
                    pane.getChartPanel().getChart().getXYPlot());
        } else {
            ocOMarkers.clear();
            ocCMarkers.clear();
        }
    }

    private static void ensureOcTimingNewStarClearListener() {
        if (newStarClearListenerRegistered) {
            return;
        }
        newStarClearListenerRegistered = true;
        Mediator.getInstance().getNewStarNotifier()
                .addListener(new Listener<NewStarMessage>() {
                    @Override
                    public void update(NewStarMessage info) {
                        if (activeResultDialog != null) {
                            activeResultDialog.onNewStar();
                        }
                        clearOcTimingMarkersFromRawPlot();
                    }

                    @Override
                    public boolean canBeRemoved() {
                        return false;
                    }
                });
    }

    /**
     * Convert a ChartPanel screen location to domain (JD) value, or null.
     */
    static Double domainValueAtScreenX(ChartPanel chartPanel, int screenX,
            int screenY) {
        if (chartPanel == null || chartPanel.getChart() == null) {
            return null;
        }
        XYPlot plot = chartPanel.getChart().getXYPlot();
        PlotRenderingInfo plotInfo = chartPanel.getChartRenderingInfo()
                .getPlotInfo();
        if (plotInfo == null) {
            return null;
        }
        Rectangle2D dataArea = plotInfo.getDataArea();
        if (dataArea == null) {
            return null;
        }
        Point2D p = chartPanel.translateScreenToJava2D(
                new java.awt.Point(screenX, screenY));
        if (!dataArea.contains(p)) {
            // Still resolve domain from x if inside horizontal span of area.
            if (p.getX() < dataArea.getMinX() || p.getX() > dataArea.getMaxX()) {
                return null;
            }
        }
        ValueAxis domainAxis = plot.getDomainAxis();
        return domainAxis.java2DToValue(p.getX(), dataArea,
                plot.getDomainAxisEdge());
    }

    /**
     * Pixel distance of screen x to a domain marker value.
     */
    static double pixelDistanceToDomainValue(ChartPanel chartPanel,
            double domainValue, int screenX, int screenY) {
        if (chartPanel == null || chartPanel.getChart() == null) {
            return Double.MAX_VALUE;
        }
        XYPlot plot = chartPanel.getChart().getXYPlot();
        PlotRenderingInfo plotInfo = chartPanel.getChartRenderingInfo()
                .getPlotInfo();
        if (plotInfo == null) {
            return Double.MAX_VALUE;
        }
        Rectangle2D dataArea = plotInfo.getDataArea();
        ValueAxis domainAxis = plot.getDomainAxis();
        double java2DX = domainAxis.valueToJava2D(domainValue, dataArea,
                plot.getDomainAxisEdge());
        Point2D screenOfMarker = chartPanel.translateJava2DToScreen(
                new Point2D.Double(java2DX, dataArea.getCenterY()));
        return Math.abs(screenOfMarker.getX() - screenX);
    }

    static int hitTestOMarkerIndex(ChartPanel chartPanel, int screenX,
            int screenY) {
        int best = -1;
        double bestDist = MARKER_HIT_PIXELS + 0.5;
        for (int i = 0; i < ocOMarkers.size(); i++) {
            double d = pixelDistanceToDomainValue(chartPanel,
                    ocOMarkers.get(i).getValue(), screenX, screenY);
            if (d <= MARKER_HIT_PIXELS && d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    private static void applyDataSourceFields(String dataSource,
            SelectableTextField ephemerisSourceField,
            SelectableTextField timingField, IntegerField meanPercentField,
            IntegerField minObsField, IntegerField kvwNfoldField,
            DoubleField periodField, DoubleField epochField,
            SelectableTextField eventField) {
        boolean fromObs = DATA_OBSERVATIONS.equals(dataSource);
        boolean fromImport = DATA_IMPORTED.equals(dataSource);
        boolean editTimings = DATA_EDIT_TIMINGS.equals(dataSource);

        setFieldEnabled(ephemerisSourceField, fromObs || editTimings);
        setFieldEnabled(timingField, fromObs);
        applyTimingMethodFields(dataSource, timingField, meanPercentField,
                kvwNfoldField);
        setFieldEnabled(minObsField, fromObs);
        setFieldEnabled(eventField, fromObs || fromImport || editTimings);
        setFieldEnabled(periodField, fromObs || fromImport || editTimings);
        setFieldEnabled(epochField, fromObs || fromImport || editTimings);
    }

    private static void applyTimingMethodFields(String dataSource,
            SelectableTextField timingField, IntegerField meanPercentField,
            IntegerField kvwNfoldField) {
        boolean fromObs = DATA_OBSERVATIONS.equals(dataSource);
        boolean meanExtreme = TimingMethod.MEAN_OF_EXTREME.getLabel()
                .equals(timingField.getValue());
        boolean kvw = TimingMethod.KWEE_VAN_WOERDEN.getLabel()
                .equals(timingField.getValue());
        setFieldEnabled(meanPercentField, fromObs && meanExtreme);
        setFieldEnabled(kvwNfoldField, fromObs && kvw);
    }

    private static void setFieldEnabled(ITextComponent<?> field,
            boolean enabled) {
        field.getUIComponent().setEnabled(enabled);
    }

    private static void tightenFieldHeight(JComponent comp) {
        Dimension pref = comp.getPreferredSize();
        int height = Math.min(pref.height, 24);
        comp.setPreferredSize(new Dimension(pref.width, height));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    /**
     * Match chart background, gridlines, fonts, and padding to the main
     * light-curve plot preferences.
     */
    private static void applyChartProperties(JFreeChart chart) {
        StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme
                .createJFreeTheme();
        try {
            Font font = ChartPropertiesPrefs.getChartExtraLargeFont();
            if (font != null) {
                chartTheme.setExtraLargeFont(font);
            }
            font = ChartPropertiesPrefs.getChartLargeFont();
            if (font != null) {
                chartTheme.setLargeFont(font);
            }
            font = ChartPropertiesPrefs.getChartRegularFont();
            if (font != null) {
                chartTheme.setRegularFont(font);
            }
            font = ChartPropertiesPrefs.getChartSmallFont();
            if (font != null) {
                chartTheme.setSmallFont(font);
            }
            chartTheme.apply(chart);
        } catch (Exception e) {
            // ignore
        }

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(ChartPropertiesPrefs.getChartBackgroundColor());
        plot.setDomainGridlinePaint(ChartPropertiesPrefs.getChartGridlinesColor());
        plot.setRangeGridlinePaint(ChartPropertiesPrefs.getChartGridlinesColor());
        chart.setPadding(new RectangleInsets(0, 0, 0, 30));
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
            boolean editTimings = DATA_EDIT_TIMINGS
                    .equals(dataSourceField.getValue());
            for (ITextComponent<?> field : fields) {
                if (imported && isOptionalForImportFile(field.getName())) {
                    continue;
                }
                if (editTimings && isDisabledForEditTimings(field.getName())) {
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

        private static boolean isDisabledForEditTimings(String fieldName) {
            return "Timing method".equals(fieldName)
                    || "Extreme N% (mean timing method)".equals(fieldName)
                    || "KvW folds (3, 5, or 7)".equals(fieldName)
                    || "Minimum observations per cycle".equals(fieldName);
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

    private static String ephemerisSourceTooltip(String source) {
        if (EPHEMERIS_PHASE.equals(source)) {
            return EPHEMERIS_PHASE_TOOLTIP;
        }
        if (EPHEMERIS_STAR.equals(source)) {
            return EPHEMERIS_STAR_TOOLTIP;
        }
        if (EPHEMERIS_MANUAL.equals(source)) {
            return EPHEMERIS_MANUAL_TOOLTIP;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void installEphemerisSourceTooltips(
            SelectableTextField field) {
        JComboBox<String> combo = (JComboBox<String>) field.getUIComponent();
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                if (value instanceof String) {
                    setToolTipText(ephemerisSourceTooltip((String) value));
                }
                return c;
            }
        });
        // Popup list is not always tip-enabled (notably Aqua); register it.
        combo.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                Object popup = combo.getUI().getAccessibleChild(combo, 0);
                if (popup instanceof ComboPopup) {
                    ToolTipManager.sharedInstance().registerComponent(
                            ((ComboPopup) popup).getList());
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        updateEphemerisSourceFieldTooltip(field);
    }

    private static void updateEphemerisSourceFieldTooltip(
            SelectableTextField field) {
        field.getUIComponent().setToolTipText(
                ephemerisSourceTooltip(field.getValue()));
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

    /**
     * Smoke test: generate Foster clock 2 demo light curves, measure O-C, and
     * confirm a flat offset near +0.0035 d with near-zero slope (epoch wrong,
     * period OK). Avoids UI dialogs; detailed cases live in unit tests.
     */
    @Override
    public Boolean test() {
        boolean ok = true;
        setTestMode(true);
        try {
            DemoDataset data = OCAnalysisDemoData
                    .generate(DemoScenario.EPOCH_OFFSET);
            Parameters params = new Parameters(data.modelPeriod,
                    data.modelEpoch, EventType.MAXIMUM, TimingMethod.PARABOLIC,
                    10, 3);
            Result result = OCAnalysisLib.analyze(data.observations, params);
            ok &= result.points.size() >= 5;
            // Foster Table 13.2 clock 2: constant O-C ≈ +0.0035 d (epoch
            // offset, period OK). Tolerance 0.05 matches OCAnalysisLibTest —
            // parabolic timing on synthetic bumps is not exact table times.
            for (Point p : result.points) {
                ok &= Math.abs(p.oc - 0.0035) < 0.05;
            }
            // Flat trend: slope must be ≈ 0 (not clock 3's ~0.0021 d/cycle).
            LinearFit fit = OCAnalysisLib.fitLinear(result.points);
            ok &= fit != null && Math.abs(fit.slope) < 1e-3;
        } catch (Throwable t) {
            ok = false;
        } finally {
            setTestMode(false);
        }
        return ok;
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

        private enum FitDisplayMode {
            LINEAR("Linear"),
            QUADRATIC("Quadratic"),
            TWO_SEGMENT("Two-segment");

            private final String label;

            FitDisplayMode(String label) {
                this.label = label;
            }
        }

        private Result result;
        private LinearFit linearFit;
        private TwoSegmentFit twoSegmentFit;
        private QuadraticFit quadraticFit;
        private final EditableTimingsModel timingsModel;
        private final Color seriesColor;
        private boolean timingMarkersShown;
        private final List<ValidObservation> obsForSnap;
        private final boolean startInPlaceMode;

        private final YIntervalSeries ocSeries = new YIntervalSeries("O-C");
        private final YIntervalRenderer ocRenderer;
        private final XYLineAndShapeRenderer fitRenderer = createFitRenderer();
        private final ChartPanel chartPanel;
        private final JRadioButton cycleAxisButton;
        private final JRadioButton timeAxisButton;
        private final JRadioButton linearFitButton;
        private final JRadioButton quadraticFitButton;
        private final JRadioButton twoSegmentFitButton;
        private final JEditorPane fitSummaryPane;
        private final JLabel dialogSummaryLabel;
        private final JTextField breakCycleField;
        private final JButton applyTwoSegmentButton;
        private final JCheckBox placeModeCheck;
        private final JCheckBox snapCheck;
        private final JTable dataTable;
        private final OCTableModel tableModel;
        private int selectedTimingIndex = -1;

        private MouseAdapter plotMouseAdapter;
        private boolean placeModeActive = false;
        private boolean panZoomSaved = false;
        private boolean savedDomainZoomable;
        private boolean savedRangeZoomable;
        private boolean savedDomainPannable;
        private boolean savedRangePannable;
        private int dragIndex = -1;
        private boolean didDrag = false;
        private boolean rebuilding = false;

        OCAnalysisResultDialog(String seriesName, Result result,
                EditableTimingsModel timingsModel, LinearFit linearFit,
                QuadraticFit quadraticFit, Color seriesColor,
                boolean timingMarkersShown, List<ValidObservation> obsForSnap,
                boolean startInPlaceMode) {
            super(DocumentManager.findActiveWindow(), "O-C: " + seriesName,
                    ModalityType.MODELESS);
            this.result = result;
            this.timingsModel = timingsModel;
            this.linearFit = linearFit;
            this.twoSegmentFit = null;
            this.quadraticFit = quadraticFit;
            this.seriesColor = seriesColor;
            this.timingMarkersShown = timingMarkersShown;
            this.obsForSnap = obsForSnap != null ? obsForSnap
                    : Collections.<ValidObservation>emptyList();
            this.startInPlaceMode = startInPlaceMode;
            this.ocRenderer = createOcRenderer(seriesColor);
            this.tableModel = new OCTableModel();
            this.dataTable = new JTable(tableModel);
            dataTable.setColumnSelectionAllowed(false);
            dataTable.setRowSelectionAllowed(true);
            dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            dataTable.getSelectionModel()
                    .addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            if (e.getValueIsAdjusting() || rebuilding) {
                                return;
                            }
                            selectedTimingIndex = dataTable.getSelectedRow();
                            publishTimingDomainMarkers(OCAnalysisResultDialog.this.result);
                        }
                    });
            tableModel.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    // no-op; edits handled in setValueAt
                }
            });

            breakCycleField = new JTextField(4);
            tightenFieldHeight(breakCycleField);
            breakCycleField.setToolTipText(
                    "Cycle number where the O-C trend appears to change "
                            + "(" + OCAnalysisLib.VSA_CHAPTER13_CITE + ").");
            applyTwoSegmentButton = new JButton("Apply");
            applyTwoSegmentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyTwoSegmentFit();
                }
            });
            updateBreakCycleTooltip();

            placeModeCheck = new JCheckBox("Place O on light curve");
            placeModeCheck.setToolTipText(
                    "When checked, click the raw light curve to place a free "
                            + "JD timing. Drag an O marker to move it. "
                            + "Pan/zoom drag is disabled while this is on.");
            placeModeCheck.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    setPlaceMode(placeModeCheck.isSelected());
                }
            });
            snapCheck = new JCheckBox("Snap to nearest observation");
            snapCheck.setSelected(!this.obsForSnap.isEmpty());
            snapCheck.setEnabled(!this.obsForSnap.isEmpty());
            snapCheck.setToolTipText(
                    "When placing or dragging, snap O to a nearby observation "
                            + "JD (within about " + DEFAULT_SNAP_MAX_DAYS
                            + " d).");

            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            ActionListener dismissListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    disposeEditor();
                }
            };
            getRootPane().registerKeyboardAction(dismissListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            getRootPane().registerKeyboardAction(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeSelectedTiming();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            getRootPane().registerKeyboardAction(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeSelectedTiming();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
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

            linearFitButton = new JRadioButton(FitDisplayMode.LINEAR.label,
                    true);
            quadraticFitButton = new JRadioButton(
                    FitDisplayMode.QUADRATIC.label, false);
            twoSegmentFitButton = new JRadioButton(
                    FitDisplayMode.TWO_SEGMENT.label, false);
            ButtonGroup fitGroup = new ButtonGroup();
            fitGroup.add(linearFitButton);
            fitGroup.add(quadraticFitButton);
            fitGroup.add(twoSegmentFitButton);
            ItemListener fitListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        updateFitControls();
                        refreshFitSummary();
                        refreshChart();
                    }
                }
            };
            linearFitButton.addItemListener(fitListener);
            quadraticFitButton.addItemListener(fitListener);
            twoSegmentFitButton.addItemListener(fitListener);
            updateFitControls();

            fitSummaryPane = createFitSummaryPane();
            dialogSummaryLabel = new JLabel();
            dialogSummaryLabel.setVerticalAlignment(SwingConstants.TOP);
            refreshFitSummary();

            JFreeChart chart = ChartFactory.createScatterPlot(
                    "O-C diagram", XAxisMode.CYCLE.label, "O-C (days)",
                    new YIntervalSeriesCollection(), PlotOrientation.VERTICAL,
                    false, true, false);
            XYPlot plot = chart.getXYPlot();
            plot.setRenderer(0, ocRenderer);
            plot.setRenderer(1, fitRenderer);
            applyChartProperties(chart);
            restoreChartRendererStyles(plot);
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
            topPane.add(createEditTimingsPane());
            topPane.add(tabs);
            topPane.add(createSummaryPane());
            topPane.add(createButtonPane(dismissListener));
            contentPane.add(topPane);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    cleanupPlotInteraction();
                    if (activeResultDialog == OCAnalysisResultDialog.this) {
                        activeResultDialog = null;
                    }
                }
            });

            if (activeResultDialog != null
                    && activeResultDialog != this) {
                activeResultDialog.disposeEditor();
            }
            activeResultDialog = this;
            installPlotMouseHandlers();
            if (startInPlaceMode) {
                placeModeCheck.setSelected(true);
            }

            pack();
            setLocationRelativeTo(Mediator.getUI().getContentPane());
            setVisible(true);
        }

        int getSelectedTimingIndex() {
            return selectedTimingIndex;
        }

        void onNewStar() {
            setPlaceMode(false);
            placeModeCheck.setSelected(false);
            cleanupPlotInteraction();
        }

        private void disposeEditor() {
            setPlaceMode(false);
            cleanupPlotInteraction();
            setVisible(false);
            dispose();
        }

        private JPanel createEditTimingsPane() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            panel.setBorder(BorderFactory.createTitledBorder("Edit timings"));
            panel.add(placeModeCheck);
            panel.add(snapCheck);
            JButton removeButton = new JButton("Remove selected");
            removeButton.setToolTipText(
                    "Remove the selected row / O marker (Delete key).");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeSelectedTiming();
                }
            });
            panel.add(removeButton);
            JButton clearButton = new JButton("Clear all");
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timingsModel.isEmpty()) {
                        return;
                    }
                    int ok = JOptionPane.showConfirmDialog(
                            OCAnalysisResultDialog.this,
                            "Remove all observed timings?", "O-C",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (ok == JOptionPane.OK_OPTION) {
                        timingsModel.clear();
                        selectedTimingIndex = -1;
                        rebuildFromModel();
                    }
                }
            });
            panel.add(clearButton);
            return panel;
        }

        private void installPlotMouseHandlers() {
            ChartPanel panel = rawChartPanel();
            if (panel == null) {
                return;
            }
            plotMouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    int hit = hitTestOMarkerIndex(panel, e.getX(), e.getY());
                    if (hit >= 0) {
                        dragIndex = hit;
                        didDrag = false;
                        selectedTimingIndex = hit;
                        if (hit < dataTable.getRowCount()) {
                            dataTable.setRowSelectionInterval(hit, hit);
                        }
                        publishTimingDomainMarkers(result);
                        e.consume();
                    } else {
                        dragIndex = -1;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragIndex < 0 || !SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    Double jd = domainValueAtScreenX(panel, e.getX(), e.getY());
                    if (jd == null) {
                        return;
                    }
                    jd = maybeSnap(jd);
                    didDrag = true;
                    // Live marker move without full rebuild.
                    if (dragIndex < ocOMarkers.size()) {
                        ocOMarkers.get(dragIndex).setValue(jd);
                    }
                    // C marker moves with recomputed ephemeris cycle.
                    int cycle = OCAnalysisLib.cycleNumber(jd,
                            result.parameters.epoch, result.parameters.period);
                    double c = result.parameters.computedTime(cycle);
                    if (dragIndex < ocCMarkers.size()) {
                        ocCMarkers.get(dragIndex).setValue(c);
                    }
                    e.consume();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragIndex >= 0 && didDrag) {
                        Double jd = domainValueAtScreenX(panel, e.getX(),
                                e.getY());
                        if (jd != null) {
                            jd = maybeSnap(jd);
                            String source = snapCheck.isSelected()
                                    && obsForSnap != null
                                            ? OCAnalysisLib.TIMING_SOURCE_SNAP
                                            : OCAnalysisLib.TIMING_SOURCE_MANUAL;
                            // Prefer snap source only if JD actually matches snap.
                            if (snapCheck.isSelected()) {
                                Double snapped = OCAnalysisLib
                                        .nearestObservationJd(obsForSnap, jd,
                                                DEFAULT_SNAP_MAX_DAYS);
                                source = snapped != null
                                        ? OCAnalysisLib.TIMING_SOURCE_SNAP
                                        : OCAnalysisLib.TIMING_SOURCE_MANUAL;
                            }
                            timingsModel.setObservedTime(dragIndex, jd, source);
                            selectedTimingIndex = dragIndex;
                            rebuildFromModel();
                        }
                    } else if (placeModeActive && dragIndex < 0
                            && SwingUtilities.isLeftMouseButton(e)
                            && !didDrag) {
                        Double jd = domainValueAtScreenX(panel, e.getX(),
                                e.getY());
                        if (jd != null) {
                            String source = OCAnalysisLib.TIMING_SOURCE_MANUAL;
                            if (snapCheck.isSelected()) {
                                Double snapped = OCAnalysisLib
                                        .nearestObservationJd(obsForSnap, jd,
                                                DEFAULT_SNAP_MAX_DAYS);
                                if (snapped != null) {
                                    jd = snapped;
                                    source = OCAnalysisLib.TIMING_SOURCE_SNAP;
                                }
                            }
                            timingsModel.add(jd, source);
                            selectedTimingIndex = timingsModel.size() - 1;
                            rebuildFromModel();
                        }
                    }
                    dragIndex = -1;
                    didDrag = false;
                }
            };
            panel.addMouseListener(plotMouseAdapter);
            panel.addMouseMotionListener(plotMouseAdapter);
        }

        private Double maybeSnap(double jd) {
            if (!snapCheck.isSelected() || obsForSnap == null
                    || obsForSnap.isEmpty()) {
                return jd;
            }
            Double snapped = OCAnalysisLib.nearestObservationJd(obsForSnap, jd,
                    DEFAULT_SNAP_MAX_DAYS);
            return snapped != null ? snapped : jd;
        }

        private void setPlaceMode(boolean on) {
            placeModeActive = on;
            ChartPanel panel = rawChartPanel();
            ObservationAndMeanPlotPane pane = rawPlotPane();
            if (panel == null || pane == null) {
                return;
            }
            XYPlot plot = panel.getChart().getXYPlot();
            if (on) {
                if (!panZoomSaved) {
                    savedDomainZoomable = panel.isDomainZoomable();
                    savedRangeZoomable = panel.isRangeZoomable();
                    savedDomainPannable = plot.isDomainPannable();
                    savedRangePannable = plot.isRangePannable();
                    panZoomSaved = true;
                }
                panel.setDomainZoomable(false);
                panel.setRangeZoomable(false);
                plot.setDomainPannable(false);
                plot.setRangePannable(false);
            } else {
                restorePanZoom();
            }
        }

        private void restorePanZoom() {
            ChartPanel panel = rawChartPanel();
            if (panel == null || !panZoomSaved) {
                return;
            }
            XYPlot plot = panel.getChart().getXYPlot();
            panel.setDomainZoomable(savedDomainZoomable);
            panel.setRangeZoomable(savedRangeZoomable);
            plot.setDomainPannable(savedDomainPannable);
            plot.setRangePannable(savedRangePannable);
            panZoomSaved = false;
        }

        private void cleanupPlotInteraction() {
            setPlaceMode(false);
            ChartPanel panel = rawChartPanel();
            if (panel != null && plotMouseAdapter != null) {
                panel.removeMouseListener(plotMouseAdapter);
                panel.removeMouseMotionListener(plotMouseAdapter);
            }
            plotMouseAdapter = null;
            restorePanZoom();
        }

        private void removeSelectedTiming() {
            int row = dataTable.getSelectedRow();
            if (row < 0) {
                row = selectedTimingIndex;
            }
            if (row < 0 || row >= timingsModel.size()) {
                return;
            }
            timingsModel.remove(row);
            selectedTimingIndex = -1;
            rebuildFromModel();
        }

        private void rebuildFromModel() {
            rebuilding = true;
            try {
                result = timingsModel.toResult(result.parameters);
                linearFit = OCAnalysisLib.fitLinear(result.points);
                quadraticFit = OCAnalysisLib.fitQuadratic(result.points);
                twoSegmentFit = null;
                timingMarkersShown = publishTimingDomainMarkers(result);
                tableModel.fireTableDataChanged();
                if (selectedTimingIndex >= 0
                        && selectedTimingIndex < tableModel.getRowCount()) {
                    dataTable.setRowSelectionInterval(selectedTimingIndex,
                            selectedTimingIndex);
                } else {
                    dataTable.clearSelection();
                }
                updateBreakCycleTooltip();
                updateFitControls();
                refreshFitSummary();
                refreshChart();
            } finally {
                rebuilding = false;
            }
        }

        private void updateBreakCycleTooltip() {
            if (result.points.isEmpty()) {
                applyTwoSegmentButton.setToolTipText(
                        "Need ≥4 O-C points for a two-segment fit.");
                return;
            }
            int minCycle = minCycle(result.points);
            int maxCycle = maxCycle(result.points);
            applyTwoSegmentButton.setToolTipText(String.format(
                    "Fit separate lines before and after the break cycle. "
                            + "Cycles %d–%d; need ≥2 points each side of break.",
                    minCycle, maxCycle));
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

            JPanel fitPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            fitPane.setBorder(BorderFactory.createTitledBorder("Fit on plot"));
            fitPane.add(linearFitButton);
            fitPane.add(quadraticFitButton);
            fitPane.add(twoSegmentFitButton);
            fitPane.add(new JLabel("Break cycle:"));
            fitPane.add(breakCycleField);
            fitPane.add(applyTwoSegmentButton);
            panel.add(fitPane);
            panel.add(chartPanel);
            return panel;
        }

        private void updateFitControls() {
            linearFitButton.setEnabled(linearFit != null);
            quadraticFitButton.setEnabled(quadraticFit != null);
            twoSegmentFitButton.setEnabled(result.points.size() >= 4);

            if (quadraticFitButton.isSelected() && quadraticFit == null) {
                linearFitButton.setSelected(true);
            } else if (twoSegmentFitButton.isSelected()
                    && result.points.size() < 4) {
                linearFitButton.setSelected(true);
            } else if (linearFitButton.isSelected() && linearFit == null
                    && quadraticFit != null) {
                quadraticFitButton.setSelected(true);
            }

            boolean twoSegmentMode = twoSegmentFitButton.isSelected();
            breakCycleField.setEnabled(twoSegmentMode);
            applyTwoSegmentButton.setEnabled(twoSegmentMode);
        }

        private FitDisplayMode selectedFitDisplayMode() {
            if (twoSegmentFitButton.isSelected()) {
                return FitDisplayMode.TWO_SEGMENT;
            }
            if (quadraticFitButton.isSelected()) {
                return FitDisplayMode.QUADRATIC;
            }
            return FitDisplayMode.LINEAR;
        }

        private JScrollPane createTablePane() {
            JScrollPane pane = new JScrollPane(dataTable);
            pane.setPreferredSize(new Dimension(640, 240));
            return pane;
        }

        private JPanel createFitPane() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JScrollPane pane = new JScrollPane(fitSummaryPane);
            pane.setPreferredSize(new Dimension(640, 220));
            pane.getVerticalScrollBar().setUnitIncrement(16);
            panel.add(pane, BorderLayout.CENTER);
            return panel;
        }

        private static JEditorPane createFitSummaryPane() {
            JEditorPane pane = new JEditorPane();
            pane.setContentType("text/html");
            pane.setEditable(false);
            pane.setOpaque(false);
            pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                    Boolean.TRUE);
            pane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED
                            && e.getURL() != null) {
                        Help.openURLInWebBrowser(e.getURL().toString(), "O-C");
                    }
                }
            });
            return pane;
        }

        private void applyTwoSegmentFit() {
            String text = breakCycleField.getText().trim();
            if (text.isEmpty()) {
                twoSegmentFit = null;
                refreshFitSummary();
                updateFitControls();
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
            refreshFitSummary();
            updateFitControls();
            twoSegmentFitButton.setSelected(true);
            refreshChart();
        }

        private void refreshFitSummary() {
            fitSummaryPane.setText(buildFitSummaryHtml());
            fitSummaryPane.setCaretPosition(0);
            dialogSummaryLabel.setText(buildSummaryHtml());
        }

        private static OCAnalysisLib.OcDiagramFitMode toLibFitMode(
                FitDisplayMode mode) {
            switch (mode) {
            case TWO_SEGMENT:
                return OCAnalysisLib.OcDiagramFitMode.TWO_SEGMENT;
            case QUADRATIC:
                return OCAnalysisLib.OcDiagramFitMode.QUADRATIC;
            case LINEAR:
            default:
                return OCAnalysisLib.OcDiagramFitMode.LINEAR;
            }
        }

        private static int minCycle(List<Point> points) {
            int min = Integer.MAX_VALUE;
            for (Point p : points) {
                if (p.cycle < min) {
                    min = p.cycle;
                }
            }
            return min == Integer.MAX_VALUE ? 0 : min;
        }

        private static int maxCycle(List<Point> points) {
            int max = Integer.MIN_VALUE;
            for (Point p : points) {
                if (p.cycle > max) {
                    max = p.cycle;
                }
            }
            return max == Integer.MIN_VALUE ? 0 : max;
        }

        private String buildFitSummaryHtml() {
            StringBuilder buf = new StringBuilder();
            appendHtmlBodyStart(buf);
            appendHtmlSection(buf, "Interpretation");
            if (result.points.isEmpty()) {
                appendHtmlParagraph(buf,
                        "No O-C points yet. Enable Place O on light curve and "
                                + "click free JD (optional snap), or enter "
                                + "times in the Data table.");
            } else {
                appendHtmlParagraph(buf, OCAnalysisLib.interpretOcDiagram(
                        linearFit, quadraticFit, twoSegmentFit, result.points,
                        toLibFitMode(selectedFitDisplayMode()),
                        result.parameters.period));
            }
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
            }
            if (quadraticFit != null) {
                appendHtmlSection(buf, "Quadratic fit (O-C vs cycle)");
                appendQuadraticFitDetails(buf, quadraticFit);
            }
            appendHtmlSection(buf, "Notes");
            appendHtmlParagraph(buf, OCAnalysisLib.getPeriodScatterWarning());
            appendHtmlParagraphWithLink(buf, "O-C pattern reference: ",
                    OCAnalysisLib.VSA_CHAPTER13_CITE,
                    OCAnalysisLib.VSA_CHAPTER13_PDF_URL);
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
                    + "margin-left:12px'>").append(htmlWithLinks(text))
                    .append("</p>");
        }

        private static void appendHtmlParagraphWithLink(StringBuilder buf,
                String beforeLink, String linkText, String url) {
            buf.append("<p style='margin-top:2px;margin-bottom:2px;"
                    + "margin-left:12px'>").append(escapeHtml(beforeLink))
                    .append("<a href='").append(escapeHtml(url)).append("'>")
                    .append(escapeHtml(linkText)).append("</a></p>");
        }

        private static String htmlWithLinks(String text) {
            String escaped = escapeHtml(text);
            String url = OCAnalysisLib.VSA_CHAPTER13_PDF_URL;
            String escapedUrl = escapeHtml(url);
            if (escaped.contains(escapedUrl)) {
                escaped = escaped.replace(escapedUrl,
                        "<a href='" + escapedUrl + "'>" + escapedUrl + "</a>");
            }
            return escaped;
        }

        private static void appendHtmlDetail(StringBuilder buf, String label,
                String value) {
            buf.append("<p style='margin-top:1px;margin-bottom:1px;"
                    + "margin-left:24px'><span style='color:#444'>")
                    .append(escapeHtml(label)).append(":</span> ")
                    .append(escapeHtml(value)).append("</p>");
        }

        private static void appendLinearFitDetails(StringBuilder buf,
                LinearFit fit, double period) {
            appendHtmlDetail(buf, "Points", String.valueOf(fit.pointCount));
            appendHtmlDetail(buf, "Intercept", formatDays(fit.intercept) + " d");
            appendHtmlDetail(buf, "Slope", formatDays(fit.slope) + " d/cycle");
            appendHtmlDetail(buf, "RMS", formatDays(fit.rms) + " d");
            appendHtmlDetail(buf, "Period correction (≈ slope)",
                    formatDays(fit.slope) + " d  (model P = "
                            + formatDays(period) + " d)");
        }

        private static void appendQuadraticFitDetails(StringBuilder buf,
                QuadraticFit fit) {
            appendHtmlDetail(buf, "Points", String.valueOf(fit.pointCount));
            appendHtmlDetail(buf, "Constant", formatDays(fit.constant) + " d");
            appendHtmlDetail(buf, "Linear", formatDays(fit.linear) + " d/cycle");
            appendHtmlDetail(buf, "Quadratic",
                    formatDays(fit.quadratic) + " d/cycle²");
            appendHtmlDetail(buf, "ΔP/cycle (≈ 2·quad)",
                    formatDays(2.0 * fit.quadratic) + " d");
            appendHtmlDetail(buf, "RMS", formatDays(fit.rms) + " d");
        }

        private static String formatDays(double value) {
            return NumericPrecisionPrefs.formatOther(value);
        }

        private static String escapeHtml(String text) {
            if (text == null) {
                return "";
            }
            return text.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;");
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
                    result.points.size() + " O-C points (editable)");
            FitDisplayMode mode = selectedFitDisplayMode();
            switch (mode) {
            case QUADRATIC:
                if (quadraticFit != null) {
                    appendSummaryRow(buf, "Quadratic fit",
                            "ΔP/cycle ≈ "
                                    + formatDays(2.0 * quadraticFit.quadratic)
                                    + " d, RMS = "
                                    + formatDays(quadraticFit.rms) + " d");
                }
                break;
            case TWO_SEGMENT:
                if (twoSegmentFit != null) {
                    appendSummaryRow(buf, "Two-segment fit",
                            "break at cycle " + twoSegmentFit.breakCycle
                                    + "; slopes "
                                    + formatDays(twoSegmentFit.firstSegment.slope)
                                    + " / "
                                    + formatDays(
                                            twoSegmentFit.secondSegment.slope)
                                    + " d/cycle");
                } else {
                    appendSummaryRow(buf, "Two-segment fit",
                            "enter break cycle and Apply");
                }
                break;
            case LINEAR:
            default:
                if (linearFit != null) {
                    appendSummaryRow(buf, "Linear fit",
                            "slope = " + formatDays(linearFit.slope)
                                    + " d/cycle, intercept = "
                                    + formatDays(linearFit.intercept)
                                    + " d, RMS = "
                                    + formatDays(linearFit.rms) + " d");
                }
                break;
            }
            if (result.parameters.timingMethod == TimingMethod.KWEE_VAN_WOERDEN) {
                appendSummaryRow(buf, "KvW (initial)",
                        result.cyclesTimed + " timed, "
                                + result.cyclesSkipped()
                                + " skipped (of " + result.cyclesExamined
                                + " cycles examined)");
            }
            if (timingMarkersShown || !result.points.isEmpty()) {
                appendSummaryRow(buf, "Light curve",
                        "Vertical O (solid) and C (dashed); place/drag when "
                                + "Place mode is on");
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
            panel.add(dialogSummaryLabel);
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

            XYSeriesCollection fitSeries = buildFitSeries(mode,
                    selectedFitDisplayMode());
            if (fitSeries != null && fitSeries.getSeriesCount() > 0) {
                plot.setDataset(1, fitSeries);
            } else if (plot.getDatasetCount() > 1) {
                plot.setDataset(1, null);
            }
        }

        private static YIntervalRenderer createOcRenderer(Color seriesColor) {
            YIntervalRenderer renderer = new YIntervalRenderer();
            renderer.setSeriesPaint(0, seriesColor);
            renderer.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
            return renderer;
        }

        private void restoreChartRendererStyles(XYPlot plot) {
            YIntervalRenderer oc = (YIntervalRenderer) plot.getRenderer(0);
            if (oc != null) {
                oc.setSeriesPaint(0, seriesColor);
                oc.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
            }
            XYLineAndShapeRenderer fit = (XYLineAndShapeRenderer) plot
                    .getRenderer(1);
            if (fit != null) {
                fit.setSeriesPaint(0, Color.RED);
                fit.setSeriesPaint(1, new Color(255, 128, 0));
                fit.setDefaultShapesVisible(false);
            }
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
                FitDisplayMode fitMode) {
            XYSeriesCollection collection = new XYSeriesCollection();
            switch (fitMode) {
            case TWO_SEGMENT:
                if (twoSegmentFit != null) {
                    collection.addSeries(buildSegmentSeries("First segment",
                            twoSegmentFit.firstSegment, result.points, mode,
                            true, twoSegmentFit.breakCycle));
                    collection.addSeries(buildSegmentSeries("Second segment",
                            twoSegmentFit.secondSegment, result.points, mode,
                            false, twoSegmentFit.breakCycle));
                }
                break;
            case QUADRATIC:
                if (quadraticFit != null) {
                    collection.addSeries(buildQuadraticSeries("Quadratic fit",
                            quadraticFit, result.points, mode));
                }
                break;
            case LINEAR:
            default:
                if (linearFit != null) {
                    collection.addSeries(buildFullSeries("Linear fit", linearFit,
                            result.points, mode));
                }
                break;
            }
            return collection;
        }

        private XYSeries buildQuadraticSeries(String name, QuadraticFit fit,
                List<Point> points, XAxisMode mode) {
            XYSeries series = new XYSeries(name);
            if (points.isEmpty()) {
                return series;
            }
            int minC = minCycle(points);
            int maxC = maxCycle(points);
            int range = maxC - minC;
            int steps = Math.min(Math.max(2, range + 1), 50);
            for (int i = 0; i < steps; i++) {
                int cycle = range == 0 ? minC
                        : minC + (int) Math.round(i * range
                                / (double) (steps - 1));
                addQuadraticFitPoint(series, fit, cycle, points, mode);
            }
            return series;
        }

        private void addQuadraticFitPoint(XYSeries series, QuadraticFit fit,
                int cycle, List<Point> points, XAxisMode mode) {
            double x = mode == XAxisMode.CYCLE ? cycle
                    : observedTimeForCycle(cycle, points);
            series.add(x, fit.evaluate(cycle));
        }

        private static double observedTimeForCycle(int cycle, List<Point> points) {
            Point below = null;
            Point above = null;
            for (Point p : points) {
                if (p.cycle <= cycle) {
                    below = p;
                }
                if (p.cycle >= cycle && above == null) {
                    above = p;
                }
            }
            if (below != null && below.cycle == cycle) {
                return below.observedTime;
            }
            if (above != null && above.cycle == cycle) {
                return above.observedTime;
            }
            if (below != null && above != null && below != above) {
                double fraction = (cycle - below.cycle)
                        / (double) (above.cycle - below.cycle);
                return below.observedTime + fraction
                        * (above.observedTime - below.observedTime);
            }
            if (below != null) {
                return below.observedTime;
            }
            if (above != null) {
                return above.observedTime;
            }
            return cycle;
        }

        private XYSeries buildFullSeries(String name, LinearFit fit,
                List<Point> points, XAxisMode mode) {
            XYSeries series = new XYSeries(name);
            if (points.isEmpty()) {
                return series;
            }
            int minC = points.get(0).cycle;
            int maxC = points.get(points.size() - 1).cycle;
            addFitPoint(series, fit, minC, points.get(0), mode);
            addFitPoint(series, fit, maxC, points.get(points.size() - 1),
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

        private class OCTableModel extends AbstractTableModel {

            private final String[] COLUMNS = { "Cycle", "O (time)", "C (time)",
                    "O-C (days)", "σ(O-C)", "Obs in cycle", "QC" };

            @Override
            public int getColumnCount() {
                return COLUMNS.length;
            }

            @Override
            public int getRowCount() {
                return result.points.size();
            }

            @Override
            public String getColumnName(int col) {
                return COLUMNS[col];
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (row < 0 || row >= result.points.size()) {
                    return null;
                }
                Point p = result.points.get(row);
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
                            : NumericPrecisionPrefs
                                    .formatOther(p.ocUncertainty);
                case 5:
                    return p.obsInCycle;
                case 6:
                    return p.qc != null ? p.qc.summaryText() : "";
                default:
                    return null;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col != 1 || row < 0 || row >= timingsModel.size()) {
                    return;
                }
                try {
                    double jd = Double.parseDouble(value.toString().trim());
                    timingsModel.setObservedTime(row, jd,
                            OCAnalysisLib.TIMING_SOURCE_MANUAL);
                    selectedTimingIndex = row;
                    rebuildFromModel();
                } catch (NumberFormatException ex) {
                    MessageBox.showErrorDialog("O-C",
                            "Enter a numeric time (JD) for O.");
                }
            }
        }
    }
}
