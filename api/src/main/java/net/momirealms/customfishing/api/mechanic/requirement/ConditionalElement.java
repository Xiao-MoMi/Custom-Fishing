/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.mechanic.requirement;

import java.util.Map;

/**
 * Represents a conditional element that can hold an element, its sub-elements, and associated requirements.
 *
 * @param <E> the type of the main element
 * @param <T> the type of the requirement parameter
 */
public class ConditionalElement<E, T> {

    private final E element;
    private final Map<String, ConditionalElement<E, T>> subElements;
    private final Requirement<T>[] requirements;

    /**
     * Constructs a new ConditionalElement.
     *
     * @param element      the main element of this conditional element
     * @param subElements  a map of sub-elements identified by a string key
     * @param requirements an array of requirements associated with this element
     */
    public ConditionalElement(E element, Map<String, ConditionalElement<E, T>> subElements, Requirement<T>[] requirements) {
        this.element = element;
        this.subElements = subElements;
        this.requirements = requirements;
    }

    /**
     * Retrieves the main element.
     *
     * @return the main element of type {@link E}
     */
    public E getElement() {
        return element;
    }

    /**
     * Retrieves the requirements associated with this element.
     *
     * @return an array of {@link Requirement} of type {@link T}
     */
    public Requirement<T>[] getRequirements() {
        return requirements;
    }

    /**
     * Retrieves the sub-elements associated with this element.
     *
     * @return a map of sub-elements identified by a string key
     */
    public Map<String, ConditionalElement<E, T>> getSubElements() {
        return subElements;
    }
}
