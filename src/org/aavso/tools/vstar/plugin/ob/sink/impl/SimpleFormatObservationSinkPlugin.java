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
package org.aavso.tools.vstar.plugin.ob.sink.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * An observation sink plugin for simple text format file saves.
 */
public class SimpleFormatObservationSinkPlugin extends CommonTextFormatSinkPluginBase {

	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs, String delimiter) throws IOException {
		saveObsToFileInSimpleFormat(writer, obs, delimiter);
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("SIMPLE_FORMAT_FILE");
	}

	@Override
	public String getDescription() {
		return "Save as simple text format file.";
	}

	/**
	 * Write observations in simple format to specified output stream. Also updates
	 * the progress bar upon each observation written.
	 * 
	 * @param writer    The specified print writer.
	 * @param obs       A list of observations.
	 * @param delimiter The field delimiter to use; may be null.
	 */
	private void saveObsToFileInSimpleFormat(PrintWriter writer, List<ValidObservation> obs, String delimiter)
			throws IOException {
		for (ValidObservation ob : obs) {
			boolean includeJD = Mediator.getInstance().getViewMode() == ViewModeType.LIST_OBS_MODE
					|| Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA;
			// Exclude excluded observations from the output file C.Kotnik
			// 2018-12-16
			if (!ob.isExcluded()) {
				writer.write(ob.toSimpleFormatString(delimiter, includeJD));
			}

			Mediator.getInstance().getProgressNotifier().notifyListeners(ProgressInfo.INCREMENT_PROGRESS);
		}
	}

    @Override
    public Boolean test() {
        boolean success = true;

        List<ValidObservation> obs = new ArrayList<ValidObservation>();

        String obsCode = "FOOBAR";

        ValidObservation ob1 = new ValidObservation();
        ob1.setDateInfo(new DateInfo(2459645.1234));
        ob1.setMagnitude(new Magnitude(5, 0.001));
        ob1.setObsCode(obsCode);
        obs.add(ob1);

        ValidObservation ob2 = new ValidObservation();
        ob2.setDateInfo(new DateInfo(2459645.2345));
        ob2.setMagnitude(new Magnitude(5.1, 0.002));
        ob2.setObsCode(obsCode);
        obs.add(ob2);
        
        PrintWriter writer = null;
        StringWriter strWriter = null;
        
        try {
            strWriter = new StringWriter();
            writer = new PrintWriter(strWriter);
            save(writer, obs, ",");
        } catch (Exception e) {
            success = false;
        } finally {
            // see ObsListFileSaveTask.doInBackground()
            if (strWriter != null && writer != null) {
                writer.flush();
            }
        }

        if (success) {
            StringBuffer actual = strWriter.getBuffer();

            StringBuffer expected = new StringBuffer();
            expected.append("2459645.1234,5.0,0.001,FOOBAR,\n");
            expected.append("2459645.2345,5.1,0.002,FOOBAR,\n");

            success = actual.toString().equals(expected.toString());
        }

        return success;
    }
}