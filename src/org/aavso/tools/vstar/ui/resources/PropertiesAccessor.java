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

import java.net.URL;

import java.io.*;
import java.util.*;
import org.aavso.tools.vstar.ui.model.list.Star;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * The purpose of this class is to provide access to non-class properties such as
 * the star list
 */
public class PropertiesAccessor {

	/**
	  * TODO: Pull the accessing of the URL out to a class that does that in the constructor
	  */
	//build some constants here for the starlists
	public static Star [] getNorthernStarList(/*constant indicating starlist*/){
		//based on constant passed in use the resource accessor to get the appropriate properties file path
		Star tenstarlist[] = null;
		String list[];

		try {

			URL url = PropertiesAccessor.class.getResource("/etc/TenStarList.properties");
			Properties props = new Properties();
			props.load(url.openStream());

			list = props.getProperty("tenstarnorthlist").split(",");

			tenstarlist = new Star[list.length/2];

			for(int i = 0, j = 0; i < list.length; j++, i+=2){
				Star S = new Star();
				S.setName(list[i]);
				S.setIdentifier(list[i+1]);
				tenstarlist[j] = S;
			}   
			//Process star list and make a hashmap or whatever the drop down needs

		}catch(IOException e){ 
			e.printStackTrace();
		}   

		return tenstarlist;

	}

	public static String getNorthernStarTitle(/*constant indicating starlist*/){
		//based on constant passed in use the resource accessor to get the appropriate properties file path
		String value = null;

		try {

			URL url = PropertiesAccessor.class.getResource("/etc/TenStarList.properties");
			Properties props = new Properties();
			props.load(url.openStream());

			value = props.getProperty("tenstartitle");

		}catch(IOException e){ 
			e.printStackTrace();
		}   

		return value;

	}

}
