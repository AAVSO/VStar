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
package org.aavso.tools.vstar.ui.resources;

/*
 * A model object for the stars that make up the list of available stars in 
 * the DB. This is to ensure that the information needed to process that list 
 * is maintained in one place.
 */
public class Star {
	private String name;
	private String identifier;

	private void Star(){
		
	}

	public void setName(String n){
		this.name = n;
	}

	public void setIdentifier(String id){
		this.identifier = id;
	}

	public String getName(){
		return this.name;
	}

	public String getIdentifier(){
		return this.identifier;
	}
	
}
