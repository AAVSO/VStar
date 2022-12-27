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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * Creates a LaTeX document (plot, table) from currently visible observations.
 * 
 * TODO:<br/>
 * - table with "..." row if too many observations?<br>
 * - multiple series
 */
public class LaTeXObservationSink extends ObservationSinkPluginBase {

	@Override
	public String getDisplayName() {
		return "LaTeX";
	}

	@Override
	public String getDescription() {
		return "Saves observations as a LaTex document";
	}

	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs, String delimiter) throws IOException {
		addHeader(writer);
		addPlot(writer, obs);
		addTable(writer, obs);
		addFooter(writer);
		writer.close();
	}

	// Helpers

	private void addHeader(PrintWriter writer) {
		writer.println("\\documentclass{article}\n");
		writer.println("\\title{VStar \\LaTeX}");
		writer.println("\\author{David Benn}\n");
		writer.println("\\usepackage{booktabs}");
		writer.println("\\usepackage{pgfplots}");
		writer.println("\\pgfplotsset{width=15cm,compat=1.9}");
		writer.println("\\begin{document}\n");
		writer.println("\\maketitle\n");
	}

	private void addFooter(PrintWriter writer) {
		writer.println("\\end{document}");
	}

	private void addTable(PrintWriter writer, List<ValidObservation> obs) {
		if (obs.size() <= 40) {
			writer.println("\\section{Table}");
			writer.println("\\begin{table}[h!]");
			writer.println("\\begin{tabular}{rrr}"); // 3 columns, right justified
			writer.println("\\toprule");
			AnalysisType mode = Mediator.getInstance().getAnalysisType();
			String timeLabel;
			if (mode == AnalysisType.RAW_DATA) {
				timeLabel = obs.get(0).getJDflavour().label;
			} else {
				timeLabel = "Phase";
			}
			writer.printf("%s & Mag & Error \\\\\n", timeLabel);
			writer.println("\\midrule");
			for (ValidObservation ob : obs) {
				if (!ob.isExcluded()) {
					if (mode == AnalysisType.RAW_DATA) {
						writer.print(NumericPrecisionPrefs.formatTime(ob.getJD()));
					} else {
						writer.print(NumericPrecisionPrefs.formatTime(ob.getStandardPhase()));
					}
					writer.print(" & ");

					String modifier;
					switch (ob.getMagnitude().getMagModifier()) {
					case FAINTER_THAN:
						modifier = "<";
						break;
					case BRIGHTER_THAN:
						modifier = ">";
						break;
					case NO_DELTA:
					default:
						modifier = "";
						break;
					}
					writer.print(modifier);

					Magnitude mag = ob.getMagnitude();

					String magStr = NumericPrecisionPrefs.formatMag(mag.getMagValue());
					writer.print(magStr);

					writer.print(" & ");

					String errStr = NumericPrecisionPrefs.formatMag(mag.getUncertainty());
					writer.print(errStr);

					writer.println(" \\\\");
				}
			}
			writer.println("\\bottomrule");
			writer.println("\\end{tabular}");
			writer.printf("\\caption{%s}\n", obs.get(0).getName());
			writer.println("\\label{table:1}");
			writer.println("\\end{table}");
		}
		writer.println();
	}

	private void addPlot(PrintWriter writer, List<ValidObservation> obs) {
		if (obs.size() != 0) {
			AnalysisType mode = Mediator.getInstance().getAnalysisType();
			String name = obs.get(0).getName();
			writer.println("\\section{Plot}");
			writer.println("\\begin{tikzpicture}");
			writer.println("\\begin{axis}[");
			if (mode == AnalysisType.RAW_DATA) {
				writer.printf("  title={%s Light Curve},\n", name);
				writer.printf("  xlabel={%s},\n", obs.get(0).getJDflavour().label);
			} else {
				writer.printf("  title={%s Phase Plot},\n", name);
				writer.println("  xlabel=Phase,");
			}
			writer.println("  ylabel={Magnitude},");
			writer.println("  y dir=reverse,");
			writer.println("  legend pos=north east,");
			writer.println("  xmajorgrids=true,");
			writer.println("  ymajorgrids=true,");
			writer.println("  grid style=dashed,");
			writer.println("]");
			writer.println("\\addplot [color=blue, only marks,mark=o,]");
			writer.println("plot [error bars/.cd, y dir = both, y explicit]");
			writer.println("table[x index=0, y index=1, y error index=2]  {");
			if (mode == AnalysisType.RAW_DATA) {
				addObsToPlot(writer, obs, mode, false);
			} else {
				addObsToPlot(writer, obs, mode, false);
				addObsToPlot(writer, obs, mode, true);
			}
			writer.println("};");
			writer.printf("\\legend{%s}\n", name);
			writer.println("\\end{axis}");
			writer.println("\\end{tikzpicture}");
		}
		writer.println();
	}

	private void addObsToPlot(PrintWriter writer, List<ValidObservation> obs, AnalysisType mode, boolean secondPass) {
		for (ValidObservation ob : obs) {
			if (!ob.isExcluded()) {
				if (mode == AnalysisType.RAW_DATA) {
					writer.print(NumericPrecisionPrefs.formatTime(ob.getJD()));
				} else {
					double phase;
					if (!secondPass) {
						phase = ob.getPreviousCyclePhase();
					} else {
						phase = ob.getStandardPhase();
					}
					writer.print(NumericPrecisionPrefs.formatTime(phase));
				}
				writer.print(" ");

				Magnitude mag = ob.getMagnitude();

				String magStr = NumericPrecisionPrefs.formatMag(mag.getMagValue());
				writer.print(magStr);

				writer.print(" ");

				String errStr = NumericPrecisionPrefs.formatMag(mag.getUncertainty());
				writer.print(errStr);

				writer.println(" \\\\");
			}
		}
	}
}
