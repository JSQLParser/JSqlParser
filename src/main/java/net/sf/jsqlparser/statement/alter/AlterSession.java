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

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * @author are
 * @see <a href="https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_2012.htm">ALTER
 *      SESSION</a>
 */
public class AlterSession implements Statement {
    private AlterSessionOperation operation;
    private List<String> parameters;

    public AlterSession(AlterSessionOperation operation, List<String> parameters) {
        this.operation = operation;
        this.parameters = parameters;
    }

    private static void appendParameters(StringBuilder builder, List<String> parameters) {
        for (String s : parameters) {
            builder.append(" ").append(s);
        }
    }

    public AlterSessionOperation getOperation() {
        return operation;
    }

    public void setOperation(AlterSessionOperation operation) {
        this.operation = operation;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.CyclomaticComplexity"})
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ALTER SESSION ");
        switch (operation) {
            case ADVISE_COMMIT:
                builder.append("ADVISE COMMIT");
                break;
            case ADVISE_ROLLBACK:
                builder.append("ADVISE ROLLBACK");
                break;
            case ADVISE_NOTHING:
                builder.append("ADVISE NOTHING");
                break;
            case CLOSE_DATABASE_LINK:
                builder.append("CLOSE DATABASE LINK ");
                appendParameters(builder, parameters);
                break;
            case ENABLE_COMMIT_IN_PROCEDURE:
                builder.append("ENABLE COMMIT IN PROCEDURE");
                break;
            case DISABLE_COMMIT_IN_PROCEDURE:
                builder.append("DISABLE COMMIT IN PROCEDURE");
                break;
            case ENABLE_GUARD:
                builder.append("ENABLE GUARD");
                break;
            case DISABLE_GUARD:
                builder.append("DISABLE GUARD");
                break;

            case ENABLE_PARALLEL_DML:
                builder.append("ENABLE PARALLEL DML");
                appendParameters(builder, parameters);
                break;

            case DISABLE_PARALLEL_DML:
                builder.append("DISABLE PARALLEL DML");
                appendParameters(builder, parameters);
                break;

            case FORCE_PARALLEL_DML:
                builder.append("FORCE PARALLEL DML");
                appendParameters(builder, parameters);
                break;

            case ENABLE_PARALLEL_DDL:
                builder.append("ENABLE PARALLEL DDL");
                appendParameters(builder, parameters);
                break;

            case DISABLE_PARALLEL_DDL:
                builder.append("DISABLE PARALLEL DDL");
                break;

            case FORCE_PARALLEL_DDL:
                builder.append("FORCE PARALLEL DDL");
                appendParameters(builder, parameters);
                break;

            case ENABLE_PARALLEL_QUERY:
                builder.append("ENABLE PARALLEL QUERY");
                appendParameters(builder, parameters);
                break;

            case DISABLE_PARALLEL_QUERY:
                builder.append("DISABLE PARALLEL QUERY");
                break;

            case FORCE_PARALLEL_QUERY:
                builder.append("FORCE PARALLEL QUERY");
                appendParameters(builder, parameters);
                break;

            case ENABLE_RESUMABLE:
                builder.append("ENABLE RESUMABLE");
                appendParameters(builder, parameters);
                break;

            case DISABLE_RESUMABLE:
                builder.append("DISABLE RESUMABLE");
                break;

            case SET:
                builder.append("SET");
                appendParameters(builder, parameters);
                break;
            default:
                // not going to happen

        }
        return builder.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
