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

import java.util.ArrayList;
import java.util.List;

/**
 * A notifier class genericised on the class of object
 * that will be sent to Listeners. This is a simpler,
 * more flexible, and more type-safe form of the Observer 
 * pattern than the one provided via the standard Java
 * Observer/Observable framework. A good candidate for T 
 * is an enum. Notice that both notifier and listener must
 * share the same type T. 
 */
public class Notifier<T> {

	// The list of objects with an interest in the 
	// notifier's activities.
	private List<Listener<T>> listeners;

	/**
	 * Constructor.
	 */
	public Notifier() {
		this.listeners = new ArrayList<Listener<T>>();
	}
	
	/**
	 * Add a listener with no immediate notification message.
	 * 
	 * @param listener The listener to add.
	 */
	public void addListener(Listener<T> listener) {
		this.addListener(listener, null);
	}
	
	/**
	 * Add a listener, and specify whether to notify it immediately.
	 * 
	 * TODO: not sure we really need this one!
	 * 
	 * @param listener The listener to add.
	 * @param immediateMessage The initial notification message, or null.
	 */
	public void addListener(Listener<T> listener, T message) {
		listeners.add(listener);
		
		if (message != null) {
			this.notifyListeners(message);
		}
	}

	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeListener(Listener<T> listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all iListeners of an activity update.
	 * 
	 * @param message The message to pass to each listener.
	 */
	public void notifyListeners(T message) {
		for (Listener<T> listener : listeners) {
			listener.update(message);
		}
	}
}
