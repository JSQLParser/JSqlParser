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

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.operator.OperatorClassItem;

/**
 * PostgreSQL {@code ALTER OPERATOR FAMILY name USING index_method} with one of
 * {@code ADD member [, ...]}, {@code DROP member [, ...]}, {@code RENAME TO new_name},
 * {@code OWNER TO role} and {@code SET SCHEMA schema}. Names retain SQL identifier quoting.
 */
public class AlterOperatorFamily extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        ADD, DROP, RENAME, OWNER, SET_SCHEMA
    }

    private String name;
    private String accessMethod;
    private Action action;
    private String newName;
    private List<OperatorClassItem> items = new ArrayList<>();

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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * The members that {@code ADD} adds or {@code DROP} removes. A member to remove has a kind, a
     * number and operand types only.
     *
     * @return the members in source order, empty for another action
     */
    public List<OperatorClassItem> getItems() {
        return items;
    }

    public void setItems(List<OperatorClassItem> items) {
        this.items = items;
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
        builder.append("ALTER OPERATOR FAMILY ").append(name).append(" USING ")
                .append(accessMethod);
        switch (action) {
            case ADD:
                return builder.append(" ADD ").append(OperatorClassItem.toString(items));
            case DROP:
                return builder.append(" DROP ").append(OperatorClassItem.toString(items));
            case RENAME:
                return builder.append(" RENAME TO ").append(newName);
            case OWNER:
                return builder.append(" OWNER TO ").append(newName);
            case SET_SCHEMA:
                return builder.append(" SET SCHEMA ").append(newName);
            default:
                throw new IllegalStateException("Unknown operator family alteration: " + action);
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
