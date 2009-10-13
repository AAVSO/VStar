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
package org.aavso.tools.vstar.util.notification;

import java.util.List;

/**
 * A message to be sent to listeners of notifying list classes.
 */
public class ListChangeMessage<E> {
	public static int NONE = -1;
	
	private ListChangeType type;
	private List<E> source;
	private int index;
	private Object object;

	/**
	 * Construct a list change message with type, source, and element index.
	 * 
	 * @param type List change message type.
	 * @param source Source list of change.
	 * @param index Index of element in changed source list.
	 * @param object The object that was removed, added, or set (may be null).
	 */
	public ListChangeMessage(ListChangeType type, List<E> source, int index, Object object) {
		this.type = type;
		this.source = source;
		this.index = index;
		this.object = object;
	}

	/**
	 * Construct a list change message with type, source, and element index.
	 * 
	 * @param type List change message type.
	 * @param source Source list of change.
	 * @param object The object that was removed, added, or set (may be null).
	 */
	public ListChangeMessage(ListChangeType type, List<E> source, Object object) {
		this(type, source, NONE, object);
	}

	/**
	 * Construct a list change message with type, source, and element index.
	 * 
	 * @param type List change message type.
	 * @param source Source list of change.
	 * @param index Index of element in changed source list.
	 */
	public ListChangeMessage(ListChangeType type, List<E> source, int index) {
		this(type, source, index, null);
	}

	/**
	 * Construct a list change message with type, source, and element index.
	 * 
	 * @param type List change message type.
	 * @param source Source list of change.
	 */
	public ListChangeMessage(ListChangeType type, List<E> source) {
		this(type, source, NONE, null);
	}

	/**
	 * @return the type
	 */
	public ListChangeType getType() {
		return type;
	}

	/**
	 * @return the source
	 */
	public List<E> getSource() {
		return source;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}
}
