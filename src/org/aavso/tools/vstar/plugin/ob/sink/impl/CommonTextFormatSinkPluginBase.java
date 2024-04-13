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
package org.aavso.tools.vstar.plugin.ob.sink.impl;

import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.util.help.Help;

/**
 * Common base class for text format sink plugins.
 */
public abstract class CommonTextFormatSinkPluginBase extends ObservationSinkPluginBase {
	protected final static Map<String, String> DELIMS;
	protected final static Map<String, String> SUFFIXES;

	static {
		DELIMS = new TreeMap<String, String>();
		DELIMS.put("Comma", ",");
		DELIMS.put("Space", " ");
		DELIMS.put("Tab", "\t");

		SUFFIXES = new TreeMap<String, String>();
		SUFFIXES.put("Comma", "csv");
		SUFFIXES.put("Space", "txt");
		SUFFIXES.put("Tab", "tsv");
	}


	@Override
	public Map<String, String> getDelimiterNameValuePairs() {
		return DELIMS;
	}

	@Override
	public Map<String, String> getDelimiterSuffixValuePairs() {
		return SUFFIXES;
	}
	
	@Override
	public String getDocName() {
		return Help.getAAVSOtextFormatSinkHelpPage();
	}

}
