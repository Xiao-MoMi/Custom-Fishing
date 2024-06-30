package net.momirealms.customfishing.api.mechanic.requirement;

import java.util.Map;

public class ConditionalElement<E, T> {

    private final E element;
    private final Map<String, ConditionalElement<E, T>> subElements;
    private final Requirement<T>[] requirements;

    public ConditionalElement(E element, Map<String, ConditionalElement<E, T>> subElements, Requirement<T>[] requirements) {
        this.element = element;
        this.subElements = subElements;
        this.requirements = requirements;
    }

    public E getElement() {
        return element;
    }

    public Requirement<T>[] getRequirements() {
        return requirements;
    }

    public Map<String, ConditionalElement<E, T>> getSubElements() {
        return subElements;
    }
}
