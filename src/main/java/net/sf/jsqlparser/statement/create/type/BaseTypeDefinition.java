/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.type;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.statement.DefinitionElement;

/**
 * Definition of a PostgreSQL base type, {@code CREATE TYPE name (INPUT = fn, OUTPUT = fn, ...)}.
 * PostgreSQL only warns about a name it does not know, so the names are not restricted.
 */
public class BaseTypeDefinition implements TypeDefinition {
    private List<DefinitionElement> elements = new ArrayList<>();

    public BaseTypeDefinition() {
        // empty constructor
    }

    public BaseTypeDefinition(List<DefinitionElement> elements) {
        this.elements = elements;
    }

    public List<DefinitionElement> getElements() {
        return elements;
    }

    public void setElements(List<DefinitionElement> elements) {
        this.elements = elements;
    }

    /**
     * Finds an element by its name, ignoring case.
     *
     * @param name the name, such as {@code INPUT}
     * @return the first element of that name, or {@code null}
     */
    public DefinitionElement getElement(String name) {
        return elements.stream().filter(element -> name.equalsIgnoreCase(element.getName()))
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return DefinitionElement.toString(elements);
    }
}
