/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code ALTER AGGREGATE name (arguments)} with one of {@code RENAME TO new_name},
 * {@code OWNER TO role} and {@code SET SCHEMA schema}.
 */
public class AlterAggregate extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        RENAME, OWNER, SET_SCHEMA
    }

    private RoutineReference aggregate;
    private Action action;
    private String newName;

    /**
     * The name and the argument signature, read as in DROP AGGREGATE.
     *
     * @return the aggregate
     */
    public RoutineReference getAggregate() {
        return aggregate;
    }

    public void setAggregate(RoutineReference aggregate) {
        this.aggregate = aggregate;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * The new name of {@code RENAME TO}, the role of {@code OWNER TO} or the schema of
     * {@code SET SCHEMA}.
     *
     * @return the name, or {@code null} for another action
     */
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER AGGREGATE ").append(aggregate);
        switch (action) {
            case RENAME:
                return builder.append(" RENAME TO ").append(newName);
            case OWNER:
                return builder.append(" OWNER TO ").append(newName);
            case SET_SCHEMA:
                return builder.append(" SET SCHEMA ").append(newName);
            default:
                throw new IllegalStateException("Unknown aggregate alteration: " + action);
        }
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
