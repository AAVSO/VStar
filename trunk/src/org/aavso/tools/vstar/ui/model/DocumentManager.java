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
package org.aavso.tools.vstar.ui.model;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class manages documents, in particular, the data models
 * that underpin the table and chart views. 
 * 
 * This is a Singleton since only one document manager per
 * application instance needs to exist.
 */
public class DocumentManager {

	private Map<String, AbstractTableModel> tableModelMap;
	private Map<String, AbstractXYDataset> plotModelMap;
	
	/**
	 * Associate a name with a table model.
	 * 
	 * @param name The name to be associated with the model (e.g. document/file name). 
	 * @param model The model to be associated with the name.
	 */
	public void addTableModel(String name, AbstractTableModel model) {
		tableModelMap.put(name, model);
	}
	
	/**
	 * Associate a name with a plot model.
	 * 
	 * @param name The name to be associated with the model (e.g. document/file name). 
	 * @param model The model to be associated with the name.
	 */
	public void addPlotModel(String name, AbstractXYDataset model) {
		plotModelMap.put(name, model);
	}

	/**
	 * Get a table model by name.
	 * 
	 * @param name The name of the model
	 * @return The model
	 */
	public AbstractTableModel getTableModel(String name) {
		return tableModelMap.get(name);
	}
	
	/**
	 * Get a plot model by name.
	 * 
	 * @param name The name of the model
	 * @return The model
	 */
	public AbstractXYDataset getPlotModel(String name) {
		return plotModelMap.get(name);
	}
	
	// TODO: getters for all models or all model names?
	
	// Singleton member, constructor, and getter.
	
	private static DocumentManager docMgr = new DocumentManager();

	/**
	 * Private constructor.
	 */
	private DocumentManager() {
		tableModelMap = new TreeMap<String, AbstractTableModel>();
		plotModelMap = new TreeMap<String, AbstractXYDataset>();
	}

	/**
	 * Return the Singleton instance.
	 */
	public static DocumentManager getInstance() {
		return docMgr;
	}
}
