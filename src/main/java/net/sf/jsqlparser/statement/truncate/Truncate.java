/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.truncate;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.schema.TableReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Truncate implements Statement {
    public enum IdentityOption {
        RESTART, CONTINUE
    }
    public enum DropBehavior {
        CASCADE, RESTRICT
    }

    private boolean tableToken;
    private boolean only;
    private final List<TableReference> targets = new ArrayList<>();
    private IdentityOption identityOption;
    private DropBehavior dropBehavior;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public List<TableReference> getTargets() {
        return targets;
    }

    public void setTargets(List<TableReference> references) {
        List<TableReference> replacement = new ArrayList<>(references);
        targets.clear();
        targets.addAll(replacement);
    }

    /** Preserves the legacy parser's singular accessor for the last target. */
    public Table getTable() {
        return targets.isEmpty() ? null : targets.get(targets.size() - 1).getTable();
    }

    public void setTable(Table table) {
        if (targets.isEmpty()) {
            TableReference target = new TableReference(table);
            target.setOnly(only);
            targets.add(target);
        } else {
            targets.get(targets.size() - 1).setTable(table);
        }
    }

    /** Mutable table projection; replacing a table preserves its target's inheritance scope. */
    public List<Table> getTables() {
        return new AbstractList<Table>() {
            @Override
            public Table get(int index) {
                return targets.get(index).getTable();
            }

            @Override
            public int size() {
                return targets.size();
            }

            @Override
            public Table set(int index, Table table) {
                Table previous = get(index);
                targets.get(index).setTable(table);
                return previous;
            }

            @Override
            public void add(int index, Table table) {
                targets.add(index, new TableReference(table));
            }

            @Override
            public Table remove(int index) {
                return targets.remove(index).getTable();
            }
        };
    }

    public void setTables(List<Table> tables) {
        List<Table> replacement = tables == null ? new ArrayList<>() : new ArrayList<>(tables);
        boolean firstOnly = isOnly();
        targets.clear();
        replacement.forEach(table -> targets.add(new TableReference(table)));
        setOnly(firstOnly);
    }

    public IdentityOption getIdentityOption() {
        return identityOption;
    }

    public void setIdentityOption(IdentityOption option) {
        identityOption = option;
    }

    public DropBehavior getDropBehavior() {
        return dropBehavior;
    }

    public void setDropBehavior(DropBehavior behavior) {
        dropBehavior = behavior;
    }

    public boolean getCascade() {
        return dropBehavior == DropBehavior.CASCADE;
    }

    public void setCascade(boolean cascade) {
        dropBehavior = cascade ? DropBehavior.CASCADE : null;
    }

    public boolean isTableToken() {
        return tableToken;
    }

    public void setTableToken(boolean tableToken) {
        this.tableToken = tableToken;
    }

    /** Legacy ONLY accessor describes the first target. */
    public boolean isOnly() {
        return targets.isEmpty() ? only : targets.get(0).isOnly();
    }

    public void setOnly(boolean only) {
        this.only = only;
        if (!targets.isEmpty()) {
            targets.get(0).setOnly(only);
        }
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(tableToken ? "TRUNCATE TABLE " : "TRUNCATE ");
        for (int i = 0; i < targets.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            targets.get(i).appendTo(builder);
        }
        if (identityOption != null) {
            builder.append(' ').append(identityOption).append(" IDENTITY");
        }
        if (dropBehavior != null) {
            builder.append(' ').append(dropBehavior);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public Truncate withTableToken(boolean value) {
        setTableToken(value);
        return this;
    }

    public Truncate withTable(Table value) {
        setTable(value);
        return this;
    }

    public Truncate withTables(List<Table> value) {
        setTables(value);
        return this;
    }

    public Truncate withCascade(boolean value) {
        setCascade(value);
        return this;
    }

    public Truncate withOnly(boolean value) {
        setOnly(value);
        return this;
    }
}
