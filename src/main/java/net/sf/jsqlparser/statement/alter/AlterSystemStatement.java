/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.List;
import java.util.Objects;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 * @see <a href="https://docs.oracle.com/cd/B12037_01/server.101/b10759/statements_2013.htm">ALTER
 *      SESSION</a>
 */

public class AlterSystemStatement implements Statement {
    private final AlterSystemOperation operation;
    private final List<String> parameters;

    public AlterSystemStatement(AlterSystemOperation operation, List<String> parameters) {
        this.operation =
                Objects.requireNonNull(operation, "The ALTER SYSTEM Operation must not be Null");
        this.parameters = Objects.requireNonNull(parameters,
                "The PARAMETERS List must not be null although it can be empty.");
    }

    private static void appendParameters(StringBuilder builder, List<String> parameters) {
        for (String s : parameters) {
            builder.append(" ").append(s);
        }
    }

    public AlterSystemOperation getOperation() {
        return operation;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER SYSTEM ").append(operation);
        appendParameters(builder, parameters);
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

}
