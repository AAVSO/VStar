package org.aavso.tools.vstar.data;

import org.aavso.tools.vstar.data.visitor.ObservationVisitor;
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

/**
 * The interface for all observation classes. 
 */
public interface Observation {

	/**
	 * Accept an observation visitor.
	 * 
	 * @param v A concrete observation visitor.
	 */
	public abstract void accept(ObservationVisitor v);
}
