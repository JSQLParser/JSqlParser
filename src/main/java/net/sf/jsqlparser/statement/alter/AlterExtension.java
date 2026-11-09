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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.extension.ExtensionObject;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class AlterExtension extends ASTNodeAccessImpl implements Statement {
    private String name;
    private Action action;
    private Expression version;
    private String schema;
    private ExtensionObject member;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Expression getVersion() {
        return version;
    }

    public void setVersion(Expression version) {
        this.version = version;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public ExtensionObject getMember() {
        return member;
    }

    public void setMember(ExtensionObject member) {
        this.member = member;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public enum Action {
        UPDATE, SET_SCHEMA, ADD, DROP
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("ALTER EXTENSION ").append(name).append(' ');
        switch (action) {
            case UPDATE:
                sql.append("UPDATE");
                if (version != null) {
                    sql.append(" TO ").append(version);
                }
                break;
            case SET_SCHEMA:
                sql.append("SET SCHEMA ").append(schema);
                break;
            case ADD:
            case DROP:
                sql.append(action).append(' ').append(member);
                break;
            default:
                throw new IllegalStateException("Unknown extension alteration: " + action);
        }
        return sql.toString();
    }
}
