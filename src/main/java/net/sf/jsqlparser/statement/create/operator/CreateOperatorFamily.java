/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.operator;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code CREATE OPERATOR FAMILY name USING index_method}. Names retain SQL identifier
 * quoting.
 */
public class CreateOperatorFamily extends ASTNodeAccessImpl implements Statement {
    private String name;
    private String accessMethod;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The index access method after {@code USING}, such as {@code btree}.
     *
     * @return the access method
     */
    public String getAccessMethod() {
        return accessMethod;
    }

    public void setAccessMethod(String accessMethod) {
        this.accessMethod = accessMethod;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("CREATE OPERATOR FAMILY ").append(name).append(" USING ")
                .append(accessMethod);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
