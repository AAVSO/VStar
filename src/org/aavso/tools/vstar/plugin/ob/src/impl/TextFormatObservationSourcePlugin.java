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
package org.aavso.tools.vstar.plugin.ob.src.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.text.ObservationSourceAnalyser;
import org.aavso.tools.vstar.input.text.TextFormatObservationReader;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This intrinsic plug-in reads a variable star data file format containing
 * lines of text or comma separated fields, and yields a collection of
 * observations for one star. In particular, it supports AAVSO Download and the
 * so-called "simple format".
 */
public class TextFormatObservationSourcePlugin extends
		ObservationSourcePluginBase {

	private ObservationSourceAnalyser analyser;

	@Override
	public String getDisplayName() {
		return LocaleProps.get("FILE_MENU_NEW_STAR_FROM_FILE");
	}

	@Override
	public String getDescription() {
		return "Observation source for AAVSO download and simple formats";
	}

	@Override
	public String getDocName() {
		return Help.getAAVSOtextFormatHelpPage();
	}

	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	@Override
	public NewStarType getNewStarType() {
		return analyser.getNewStarType();
	}

	@Override
	public String getGroup() {
		return "Internal";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {

		AbstractObservationRetriever retriever = null;

		byte[] allBytes = null;
		List<byte[]> byteArrayList = new ArrayList<byte[]>();
		int total = 0;
		try {
			InputStream stream = getInputStreams().get(0);

			BufferedReader streamReader = new BufferedReader(
					new InputStreamReader(stream));

			// Obtain bytes from stream in order to re-use in analyser and
			// reader.
			String line;
			while ((line = streamReader.readLine()) != null) {

				byte[] bytes = line.getBytes();
				byteArrayList.add(bytes);
				total += bytes.length + 1;
			}

			int i = 0;
			allBytes = new byte[total];
			for (byte[] bytes : byteArrayList) {
				for (byte b : bytes) {
					allBytes[i++] = b;
				}
				allBytes[i++] = '\n';
			}

			// Analyse the observation file and create an observation retriever.
			analyser = new ObservationSourceAnalyser(new LineNumberReader(
					new InputStreamReader(new ByteArrayInputStream(allBytes))),
					getInputName());
			analyser.analyse();

			retriever = new TextFormatObservationReader(new LineNumberReader(
					new InputStreamReader(new ByteArrayInputStream(allBytes))),
					analyser, getVelaFilterStr());

		} catch (Exception e) {
			// TODO: move analyser creation into reader class so we can handle
			// this more efficiently, passing top-level stream not reader...or perhaps
			// pass lines, handling exception there
		}

		return retriever;
	}
}
