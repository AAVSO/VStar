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
package org.aavso.tools.vstar.ui.mediator.message;

import org.aavso.tools.vstar.util.model.IModel;

/**
 * This message is sent when a model is created.
 */
public class ModelCreationMessage extends MessageBase {

	private IModel model;
	
	/**
	 * Constructor
	 * 
	 * @param source The source of the message.
	 * @param model The model that has been created.
	 */
	public ModelCreationMessage(Object source, IModel model) {
		super(source);
		this.model = model;
	}

	/**
	 * @return the model
	 */
	public IModel getModel() {
		return model;
	}
}
