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

/**
 * A listener interface genericised on the class of object
 * that will be sent by the notifier. This is a simpler,
 * more flexible, and more type-safe form of the Observer 
 * pattern than the one provided via the standard Java
 * Observer/Observable framework. A good candidate for T 
 * is an enum.
 */
public interface Listener<T> {

	/**
	 * The method that is called back by the notifier whose
	 * activities this listener is interested in.
	 * 
	 * @param info The type-safe information.
	 */
	public abstract void update(T info);
}
