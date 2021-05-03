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

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A notifier class genericised on the class of object that will be sent to
 * Listeners. This is a simpler, more flexible, and more type-safe form of the
 * Observer pattern than the one provided via the standard Java
 * Observer/Observable framework. A good candidate for T is an enum. Notice that
 * both notifier and listener must share the same type T.
 */
public class Notifier<T> {

	// The list of objects with an interest in the
	// notifier's activities.
	private CopyOnWriteArrayList<Listener<T>> listeners;

	// The list of messages for this notifier's listeners.
	private CopyOnWriteArrayList<T> messages;
	
	/**
	 * Constructor
	 */
	public Notifier() {
		this.listeners = new CopyOnWriteArrayList<Listener<T>>();
		this.messages = new CopyOnWriteArrayList<T>();
	}

	/**
	 * Add a listener with no immediate notification message.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addListener(Listener<T> listener) {
		this.addListener(listener, false);
	}

	/**
	 * Add a listener, and specify whether to notify it immediately.
	 * 
	 * @param listener
	 *            The listener to add.
	 * @param immediateMessages
	 *            Send all messages so far to this new listener.
	 */
	public void addListener(Listener<T> listener, boolean immediateMessages) {
		listeners.addIfAbsent(listener);

		if (immediateMessages) {
			for (T message : messages) {
				listener.update(message);
			}
		}
	}

	/**
	 * Remove a listener, if it says it is willing to be removed.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeListenerIfWilling(Listener<T> listener) {
		if (listener.canBeRemoved()) {
			listeners.remove(listener);
		}
	}

	/**
	 * Remove all listeners that are willing to be removed.
	 * Also, clear the message list.
	 */
	public void cleanup() {
		messages.clear();
		
		for (Listener<T> listener : listeners) {
			removeListenerIfWilling(listener);
		}
	}
	
	/**
	 * Notify all listeners of an activity update and collect the message
	 * for future replay, in particular for new listeners.
	 * 
	 * @param message
	 *            The message to pass to each listener.
	 */
	public void notifyListeners(T message) {
		messages.add(message);
		
		for (Listener<T> listener : listeners) {
			listener.update(message);
		}
	}
}
