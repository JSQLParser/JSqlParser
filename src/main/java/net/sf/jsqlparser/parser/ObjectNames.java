/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.util.List;

public class ObjectNames {

    private final List<String> names;
    private final List<String> delimiters;

    public ObjectNames(List<String> names, List<String> delimiters) {
        this.names = names;
        this.delimiters = delimiters;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getDelimiters() {
        return delimiters;
    }
}
