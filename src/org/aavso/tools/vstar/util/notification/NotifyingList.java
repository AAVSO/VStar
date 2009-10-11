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
 * This is the abstract base class for all lists with elements of type E 
 * that will notify listeners with a message of type ListChangeMessage. What 
 * we are saying here is that we want to be able to have a drop-in replacement 
 * for any old List<E> that will also provide us with a generic optional 
 * message notification capability. This class gets the generic message 
 * notification capability for free but requires realization as a concrete list 
 * type. This is acceptable so long as the cost of the interface mechanism isn't
 * too high. Then again, we have that problem with any List<E>. Since subclasses 
 * are unlikely to invent their own lists, instead realising the interface
 * by aggregating an ArrayList<E> or LinkedList<E> member, there will be a
 * double indirection for all array operations. The question that hasn't 
 * been answered yet is: what is the notification policy? In other words,
 * we know what will be notified (ListChangeMessage) but not when or why.
 * The expected behavior of any subclass is that listeners will be notified 
 * whenever its underlying list changes.
 */
public abstract class NotifyingList<E> implements List<E> {
	
	protected Notifier<ListChangeMessage<E>> notifier;
}
