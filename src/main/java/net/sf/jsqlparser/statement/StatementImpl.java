/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public abstract class StatementImpl implements Statement {

    public boolean isQuery() {
        return false;
    }

    public boolean isDML() {
        return false;
    }

    public boolean isDDL() {
        return false;
    }

    public boolean isBlock() {
        return false;
    }

    public boolean isUnparsed() {
        return false;
    }

    public abstract StatementType getStatementType();

    public abstract StringBuilder appendTo(final StringBuilder builder);

    @Override
    public final String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
