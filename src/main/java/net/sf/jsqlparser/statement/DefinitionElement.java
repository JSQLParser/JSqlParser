/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One element of a PostgreSQL definition list, {@code name [= value]}, as in
 * {@code CREATE TYPE name (INPUT = fn, PASSEDBYVALUE)}, {@code ALTER TYPE name SET (SEND = NONE)},
 * {@code CREATE OPERATOR} or {@code CREATE AGGREGATE}. The name keeps its spelling and quoting.
 */
public class DefinitionElement implements Serializable {
    private String name;
    private DefinitionArgument argument;

    public DefinitionElement() {
        // empty constructor
    }

    public DefinitionElement(String name, DefinitionArgument argument) {
        this.name = name;
        this.argument = argument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value after the equals sign.
     *
     * @return the argument, or {@code null} for an element without a value
     */
    public DefinitionArgument getArgument() {
        return argument;
    }

    public void setArgument(DefinitionArgument argument) {
        this.argument = argument;
    }

    /**
     * Renders a definition list in parentheses.
     *
     * @param elements the elements
     * @return the list, such as {@code (INPUT = fn, PASSEDBYVALUE)}
     */
    public static String toString(List<DefinitionElement> elements) {
        return elements.stream().map(Object::toString).collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Finds an element of a definition list by its name, ignoring case.
     *
     * @param elements the elements
     * @param name the name, such as {@code INPUT}
     * @return the first element of that name, or {@code null}
     */
    public static DefinitionElement find(List<DefinitionElement> elements, String name) {
        return elements.stream().filter(element -> name.equalsIgnoreCase(element.getName()))
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return argument == null ? name : name + " = " + argument;
    }
}
