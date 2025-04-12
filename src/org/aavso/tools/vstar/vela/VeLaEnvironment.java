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
package org.aavso.tools.vstar.vela;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A generic symbol binding and lookup environment implementation,
 * the base class for all VeLa environments.
 */
public class VeLaEnvironment<T> {

    // Named operand cache
    protected Map<String, T> cache;

    // Constant binding map
    protected Set<String> constants;

    /**
     * Construct an environment with an empty name and constant set.
     */
    public VeLaEnvironment() {
        cache = new HashMap<String, T>();
        constants = new HashSet<String>();
    }

    /**
     * Construct an environment with a specified empty name and constant set.
     */
    public VeLaEnvironment(Map<String, T> map, Set<String> isBoundConstants) {
        cache = map;
        constants = isBoundConstants;
    }

    /**
     * Is this environment mutable (can be modified)?
     *
     * @return true if mutable, false if not
     */
    public boolean isMutable() {
        // default to not being mutable
        return false;
    }

    /**
     * Lookup the named symbol in the environment.
     * 
     * @param name The symbol's name.
     * @return An optional Operand instance if it exists.
     */
    public Optional<T> lookup(String name) {
        return Optional.ofNullable(cache.get(name.toUpperCase()));
    }

    /**
     * Does this environment contain the named binding?
     * 
     * @param name The name of the binding to lookup.
     * @return Whether the name is bound in this environment.
     * @deprecated less useful now, given the use of Optional values
     */
    public boolean hasBinding(String name) {
        return cache.containsKey(name);
    }

    /**
     * Bind a value to a name.<br/>
     * It is an invariant that a constant binding cannot be overridden.
     * 
     * @param name       The name to which to bind the value.
     * @param value      The value to be bound.
     * @param isConstant Is this a constant binding?
     */
    public void bind(String name, T value, boolean isConstant) {
        boolean isBoundConstant = false;
        if (!constants.contains(name)) {
            if (!isConstant) {
                // Variable binding.
                cache.put(name, value);
            } else if (isConstant && !cache.containsKey(name)) {
                // Bind name to constant value.
                cache.put(name, value);
                constants.add(name);
            } else {
                // We are trying to bind a constant to a name in the presence
                // of a variable binding.
                isBoundConstant = true;
            }
        } else {
            isBoundConstant = true;
        }

        if (isBoundConstant) {
            throw new VeLaEvalError("'" + name + "' is a constant binding in this environment.");
        }
    }

    /**
     * Add all symbol bindings from another scope to this one.
     *
     * @param other The scope to be added to this one.
     */
    public void addAll(VeLaEnvironment<T> other) {
        cache.putAll(other.cache);
        constants.addAll(other.constants);
    }

    /**
     * Is this VeLa environment empty?
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (String name : cache.keySet()) {
            buf.append(" ");
            buf.append(name);
            buf.append(" = ");
            buf.append(cache.get(name));
            buf.append("\n");
        }

        return buf.toString();
    }
}
