/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.fdw;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.foreign.ForeignDataStatement;

public class CreateForeignDataWrapper extends ForeignDataStatement {
    private String name;
    private ForeignDataWrapperFunctions functions = new ForeignDataWrapperFunctions();

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public ForeignDataWrapperFunctions getFunctions() {
        return functions;
    }

    public void setFunctions(ForeignDataWrapperFunctions value) {
        functions = value;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("CREATE FOREIGN DATA WRAPPER ").append(name);
        functions.appendTo(sql);
        appendOptionsTo(sql, printer);
        return sql;
    }
}
