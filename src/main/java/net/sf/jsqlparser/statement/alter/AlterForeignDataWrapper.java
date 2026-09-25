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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.foreign.ForeignDataStatement;
import net.sf.jsqlparser.statement.create.fdw.ForeignDataWrapperFunctions;

public class AlterForeignDataWrapper extends ForeignDataStatement {
    public enum Action {
        OPTIONS, OWNER, RENAME
    }

    private String name;
    private Action action = Action.OPTIONS;
    private String newName;
    private String owner;
    private ForeignDataWrapperFunctions functions = new ForeignDataWrapperFunctions();

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action value) {
        action = value;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String value) {
        newName = value;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String value) {
        owner = value;
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
        sql.append("ALTER FOREIGN DATA WRAPPER ").append(name);
        switch (action) {
            case RENAME:
                sql.append(" RENAME TO ").append(newName);
                break;
            case OWNER:
                sql.append(" OWNER TO ").append(owner);
                break;
            case OPTIONS:
                functions.appendTo(sql);
                appendOptionsTo(sql, printer);
                break;
            default:
                throw new IllegalStateException("Unknown foreign-data wrapper action: " + action);
        }
        return sql;
    }

    @Override
    public void visitExpressions(Consumer<Expression> visitor) {
        if (action == Action.OPTIONS) {
            super.visitExpressions(visitor);
        }
    }
}
