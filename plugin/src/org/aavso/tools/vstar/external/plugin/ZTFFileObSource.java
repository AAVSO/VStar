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

/**
 * See https://www.cosmos.esa.int/web/gaia-users/archive/programmatic-access#Sect_1_ss1.1
 * 
 * An example of the data file can be retrieved with the following URL:
 * https://gea.esac.esa.int/data-server/data?RETRIEVAL_TYPE=epoch_photometry&FORMAT=CSV&ID=1951343009975999744&RELEASE=Gaia+DR3
 * 
 */
package org.aavso.tools.vstar.external.plugin;

import org.aavso.tools.vstar.external.lib.ZTFObSourceBase;
import org.aavso.tools.vstar.plugin.InputType;

public class ZTFFileObSource extends ZTFObSourceBase {
	
	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "ZTF Photometry Format reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from ZTF Photometry File ...";
	}

}
