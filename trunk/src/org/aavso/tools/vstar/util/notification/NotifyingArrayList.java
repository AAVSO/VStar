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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class extends ArrayList<E> to notify listeners with a message of type
 * ListChangeMessage. What we are saying here is that we want to be able to have
 * a drop-in replacement for an ArrayList<E> that will also provide us with a
 * generic optional message notification capability. Listeners are notified of
 * any change to the list.
 */
public class NotifyingArrayList<E> extends NotifyingList<E> {

	// TODO: do we really want to notify for *all* additions or have the
	// option of doing bulk notifications via a constructor boolean?
	// => add a setter for this and make if (changed) into if (changed && notifyImmediately)
	//    and also add a fireUpdate() or fireNotify() that sends a generic CHANGED message?
	
	// TODO: write UTs!

	private ArrayList<E> list;

	public boolean add(E e) {
		boolean changed = list.add(e);
		if (changed) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.ADDED_ONE, this));
		}
		return changed;
	}

	public void add(int index, E element) {
		list.add(index, element);
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.ADDED_ONE, this, index));
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean changed = list.addAll(c);
		if (changed) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.ADDED_MANY, this));
		}
		return changed;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		boolean changed = list.addAll(c);
		if (changed) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.ADDED_MANY, this, index));
		}
		return changed;
	}

	public void clear() {
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.CLEARED, this));
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public E get(int index) {
		return list.get(index);
	}

	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<E> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}

	public boolean remove(Object o) {
		boolean changed = list.remove(o);
		if (changed) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.REMOVED, this, o));
		}
		return changed;
	}

	public E remove(int index) {
		E element = list.remove(index);
		if (element != null) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.REMOVED, this, index));
		}
		return element;
	}

	public boolean removeAll(Collection<?> c) {
		boolean changed = list.removeAll(c);
		if (changed) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.REMOVED_MANY, this));
		}
		return changed;
	}

	public boolean retainAll(Collection<?> c) {
		boolean changed = list.retainAll(c);
		if (changed) {
			this.notifier.notifyListeners(new ListChangeMessage<E>(
					ListChangeType.REMOVED_MANY, this));
		}
		return changed;
	}

	public E set(int index, E element) {
		E old = list.set(index, element);
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.SET, this, index));
		return old;
	}

	public int size() {
		return list.size();
	}

	public List<E> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
}
