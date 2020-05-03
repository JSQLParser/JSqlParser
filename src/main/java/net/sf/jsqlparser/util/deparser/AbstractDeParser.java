/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

/**
 * A base for a Statement DeParser
 * @param <S> the type of statement this DeParser supports
 */
abstract class AbstractDeParser<S> {
    protected StringBuilder buffer;

    protected AbstractDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    /**
     * DeParses the given statement into the buffer
     * @param statement the statement to deparse
     */
    abstract void deParse(S statement);
}
