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

	private ArrayList<E> list;

	public boolean add(E e) {
		boolean result = list.add(e);
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.ADDED_ONE, this));
		return result;
	}

	public void add(int index, E element) {
		list.add(index, element);
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.ADDED_ONE, this, index));
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean result = list.addAll(c);
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.ADDED_MANY, this));
		return result;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = list.addAll(c);
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.ADDED_MANY, this, index));
		return result;
	}

	public void clear() {
		this.notifier.notifyListeners(new ListChangeMessage<E>(
				ListChangeType.CLEARED, this));
	}

	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public E get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public E remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public E set(int index, E element) {
		// TODO Auto-generated method stub
		return null;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
}
