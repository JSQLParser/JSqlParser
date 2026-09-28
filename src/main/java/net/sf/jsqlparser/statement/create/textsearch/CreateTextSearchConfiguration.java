/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.textsearch;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class CreateTextSearchConfiguration implements Statement {
    public enum SourceKind {
        PARSER, COPY
    }

    private String name;
    private SourceKind sourceKind;
    private String sourceName;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public SourceKind getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(SourceKind value) {
        sourceKind = value;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String value) {
        sourceName = value;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        return sql.append("CREATE TEXT SEARCH CONFIGURATION ").append(name)
                .append(" (").append(sourceKind).append(" = ").append(sourceName).append(')');
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
