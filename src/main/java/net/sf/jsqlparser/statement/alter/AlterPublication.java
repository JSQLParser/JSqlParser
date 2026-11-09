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
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.publication.PublicationTarget;
import net.sf.jsqlparser.statement.create.publication.PublicationOption;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class AlterPublication extends ASTNodeAccessImpl implements Statement {
    private String name;
    private Action action;
    private List<PublicationTarget> targets = new ArrayList<>();
    private List<PublicationOption> options = new ArrayList<>();
    private String newName;

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

    public List<PublicationTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<PublicationTarget> targets) {
        this.targets = targets;
    }

    public List<PublicationOption> getOptions() {
        return options;
    }

    public void setOptions(List<PublicationOption> options) {
        this.options = options;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public enum Action {
        ADD, SET, DROP, SET_OPTIONS, OWNER, RENAME
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressions) {
        sql.append("ALTER PUBLICATION ").append(name).append(' ');
        switch (action) {
            case OWNER:
                sql.append("OWNER TO ").append(newName);
                break;
            case RENAME:
                sql.append("RENAME TO ").append(newName);
                break;
            case SET_OPTIONS:
                sql.append("SET (");
                for (int i = 0; i < options.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    options.get(i).appendTo(sql, expressions);
                }
                sql.append(')');
                break;
            case ADD:
            case SET:
            case DROP:
                sql.append(action).append(' ');
                for (int i = 0; i < targets.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    targets.get(i).appendTo(sql, expressions);
                }
                break;
            default:
                throw new IllegalStateException("Unknown publication alteration: " + action);
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}
