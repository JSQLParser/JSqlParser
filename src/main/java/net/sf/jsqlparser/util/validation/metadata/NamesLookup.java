/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.metadata;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A strategy for transformation of database-names before lookup in
 * database-catalog-metadata
 */
public enum NamesLookup implements UnaryOperator<String> {
    UPPERCASE(String::toUpperCase), LOWERCASE(String::toLowerCase), NO_TRANSFORMATION(UnaryOperator.identity());

    private Function<String, String> strategy;

    private NamesLookup(UnaryOperator<String> strategy) {
        this.strategy = strategy;
    }

    @Override
    public String apply(String name) {
        return name == null ? null : strategy.apply(name);
    }
}