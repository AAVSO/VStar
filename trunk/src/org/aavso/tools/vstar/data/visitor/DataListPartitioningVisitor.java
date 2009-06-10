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
package org.aavso.tools.vstar.data.visitor;

import javax.swing.DefaultListModel;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This class chooses which data list an observation belongs in and adds it to
 * the corresponding list model.
 */
public class DataListPartitioningVisitor extends ObservationVisitor {

	// Model for valid observations.
	private DefaultListModel dataListModel;

	// Model for invalid observations.
	private DefaultListModel dataErrorListModel;

	/**
	 * Constructor
	 * 
	 * @param dataListModel
	 * @param dataErrorListModel
	 */
	public DataListPartitioningVisitor(DefaultListModel dataListModel,
			DefaultListModel dataErrorListModel) {
		super();
		this.dataErrorListModel = dataErrorListModel;
		this.dataListModel = dataListModel;
	}

	@Override
	public void visit(InvalidObservation ob) {
		this.dataErrorListModel.addElement(ob);
	}

	@Override
	public void visit(ValidObservation ob) {
		this.dataListModel.addElement(ob);
	}
}
