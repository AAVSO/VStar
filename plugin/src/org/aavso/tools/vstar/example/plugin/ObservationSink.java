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
package org.aavso.tools.vstar.example.plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;

/**
 * An observation sink plugin that saves observations as simple XML files
 * consisting of time, magnitude, uncertainty, and band.
 */
public class ObservationSink extends ObservationSinkPluginBase {

	@Override
	public String getDisplayName() {
		return "Simple XML File";
	}

	@Override
	public String getDescription() {
		return "Simple XML observation file format";
	}

	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs,
			String delimiter) throws IOException {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		for (ValidObservation ob : obs) {
			writer.print("<observation ");
			writer.printf("time=%f magnitude=%f error=%f band=%s/>\n",
					ob.getJD(), ob.getMag(),
					ob.getMagnitude().getUncertainty(), ob.getBand());
		}
	}
}
