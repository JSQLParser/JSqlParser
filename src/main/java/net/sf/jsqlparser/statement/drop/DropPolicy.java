/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class DropPolicy implements Statement {
    private String policyName;
    private net.sf.jsqlparser.schema.Table table;
    private boolean ifExists = false;
    private Behavior behavior;

    public String getPolicyName() {
        return policyName;
    }

    public DropPolicy setPolicyName(String policyName) {
        this.policyName = policyName;
        return this;
    }

    public net.sf.jsqlparser.schema.Table getTable() {
        return table;
    }

    public DropPolicy setTable(net.sf.jsqlparser.schema.Table table) {
        this.table = table;
        return this;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public DropPolicy setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
        return this;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public DropPolicy setBehavior(Behavior behavior) {
        this.behavior = behavior;
        return this;
    }

    public enum Behavior {
        CASCADE, RESTRICT
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("DROP POLICY ");
        if (ifExists) {
            sql.append("IF EXISTS ");
        }
        sql.append(policyName).append(" ON ").append(table);
        if (behavior != null) {
            sql.append(' ').append(behavior);
        }
        return sql;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
