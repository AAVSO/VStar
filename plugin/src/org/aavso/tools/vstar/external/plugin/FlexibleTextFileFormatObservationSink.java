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

package org.aavso.tools.vstar.external.plugin;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.plugin.ob.sink.impl.CommonTextFormatSinkPluginBase;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

public class FlexibleTextFileFormatObservationSink extends CommonTextFormatSinkPluginBase {

	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs, String delimiter) throws IOException {
		saveObsToFileInFlexTextFormat(writer, obs, delimiter);
	}

	@Override
	public String getDescription() {
		return "Flexible Text Format File Sink";
	}
	
	@Override
	public String getDisplayName() {
		return "Flexible Text Format v1.2";
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "FlexibleTextFileFormat Plug-In.pdf";
	}

	private void saveObsToFileInFlexTextFormat(PrintWriter writer, List<ValidObservation> obs, String delimiter)
			throws IOException {

		Mediator mediator = Mediator.getInstance();		
		AnalysisType analysisType = mediator.getAnalysisType();		
		
		boolean includeJD = mediator.getViewMode() == ViewModeType.LIST_OBS_MODE || analysisType == AnalysisType.RAW_DATA;
		if (!includeJD) {
			throw new IOException("Please select Raw Data to save this view");
		}
		
		// Flexible Text Format header
		writer.write("## VStar Flexible Text Format 1.2\n\n");
		String delimiterKey = delimiter;
		for(Map.Entry<String,String> entry : DELIMS.entrySet()) {
		  String value = entry.getValue();
		  if (delimiter.equals(value)) {
			  delimiterKey = entry.getKey();
			  break;
		  }
		}		
		
		writer.write("#FIELDS=Time,Magnitude,Uncertainty,Band,ObsCode,Notes,Validation,Name\n");
		writer.write("#DELIM=" + delimiterKey + "\n\n");
		
		JDflavour jDflavour = null;
		boolean mixedJD = false;
		
		List<SeriesType> seriesList = new ArrayList<SeriesType>();
		
		for (ValidObservation ob : obs) {
			if (jDflavour == null)
				jDflavour = ob.getJDflavour();
			else if (jDflavour != ob.getJDflavour()) {
				mixedJD = true;
				//break;
			}
			
			SeriesType seriesType = ob.getSeries();
			if (!seriesList.contains(seriesType))
				seriesList.add(seriesType);
		}
		
		if (mixedJD) jDflavour = null;
		
		if (jDflavour != null && jDflavour != JDflavour.UNKNOWN)
			writer.write("#DATE=" + jDflavour.toString() + "\n\n");
		else
			writer.write("## Julian Date variant cannot be determined or it is mixed\n\n");
		
		writer.write("## Series types (including standard ones)\n");
		
		for (SeriesType seriesType : seriesList) {
			String seriesShortName = seriesType.getShortName();
			String seriesDescription = seriesType.getDescription();
			Color seriesColor = seriesType.getColor();
			if (seriesShortName.indexOf(',') == -1 && seriesDescription.indexOf(',') == -1) {
				String colorHex = String.format("#%02X%02X%02X", seriesColor.getRed(), seriesColor.getGreen(), seriesColor.getBlue());
				writer.write("#DEFINESERIES=" + seriesDescription + "," + seriesShortName + "," + colorHex + "\n");
			} else {
				writer.write("## Cannot save a series definition: 'description' and 'shortName' cannot contain ','\n");
				writer.write("##   Description: " + seriesDescription + "\n");
				writer.write("##   ShortName  : " + seriesShortName + "\n");
			}
		}

		writer.write("\n");
		
		//ChartPanel panel = mediator.getPlotPane(mediator.getAnalysisType()).getChartPanel();
		// Always take the titles of the RAW_DATA pane.
		ChartPanel panel = mediator.getPlotPane(AnalysisType.RAW_DATA).getChartPanel();
		JFreeChart chart = panel.getChart();
		XYPlot plot = chart.getXYPlot();
		
		String titleX = plot.getDomainAxis().getLabel();
		String titleY = plot.getRangeAxis().getLabel();
		String title = chart.getTitle().getText();
		String titlePrefix = LocaleProps.get("LIGHT_CURVE") + " " + LocaleProps.get("FOR") + " ";
		if (title.length() > titlePrefix.length() && titlePrefix.equals(title.substring(0, titlePrefix.length())))
			title = title.substring(titlePrefix.length(), title.length());
		else
			title = "^" + title;

		if (title != null) writer.write("#NAME=" + title + "\n");
		if (titleX != null) writer.write("#TITLEX=" + titleX + "\n");
		if (titleY != null) writer.write("#TITLEY=" + titleY + "\n");
		
		writer.write("\n");
		
//		if (plot.getRangeAxis().isInverted())
//			writer.write("#RANGEINVERTED=N\n");
//		else
//			writer.write("#RANGEINVERTED=Y\n");
//
//		writer.write("\n");

		// copied from DownloadFormatObservationSinkPlugin with modifications
		for (ValidObservation ob : obs) {
			// Exclude excluded observations from the output like in DownloadFormatObservationSinkPlugin
			if (!ob.isExcluded()) {
				writer.write(observationToString(ob, delimiter));
			}
			mediator.getProgressNotifier().notifyListeners(ProgressInfo.INCREMENT_PROGRESS);
		}
	}
	
	private String observationToString(ValidObservation ob, String delimiter) {
		StringBuffer buf = new StringBuffer();
		
		buf.append(ob.getDateInfo().getJulianDay());
		buf.append(delimiter);
		
		buf.append(ob.getMagnitude().isFainterThan() ? "<" : "");
		buf.append(ob.getMagnitude().getMagValue());
		buf.append(delimiter);
		
		double uncertainty = ob.getMagnitude().getUncertainty();
		if (uncertainty > 0.0) {
			buf.append(uncertainty);
		}
		buf.append(delimiter);
		
		buf.append(quoteForCSVifNeeded(ob.getBand().getShortName(), delimiter));
		buf.append(delimiter);
		
		if (!isNullOrEmpty(ob.getObsCode())) {
			buf.append(quoteForCSVifNeeded(ob.getObsCode(), delimiter));
		}
		buf.append(delimiter);
		
		if (ob.getComments() != null) {
			buf.append(quoteForCSVifNeeded(ob.getComments(), delimiter));
		}
		buf.append(delimiter);
		
		if (ob.getValidationType() != null) {
			buf.append(ob.getValidationType().getValflag());
		}
		buf.append(delimiter);
		
		buf.append(quoteForCSVifNeeded(!isNullOrEmpty(ob.getName()) ? ob.getName() : "Unknown", delimiter));
		//buf.append(delimiter);
		
		buf.append("\n");
		return buf.toString();
	}
	
	private String quoteForCSV(String field) {
		return "\"" + field.replace("\"", "\"\"") + "\"";
	}

	private String quoteForCSVifNeeded(String field, String delimiter) {
		return field.contains(delimiter) ? quoteForCSV(field) : field;
	}
	
	private boolean isNullOrEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

}
