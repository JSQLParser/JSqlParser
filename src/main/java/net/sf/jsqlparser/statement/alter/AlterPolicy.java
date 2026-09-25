/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.policy.PolicyOptions;

public class AlterPolicy implements Statement {
    private String policyName;
    private net.sf.jsqlparser.schema.Table table;
    private String newName;
    private PolicyOptions options = new PolicyOptions();


    public String getPolicyName() {
        return policyName;
    }

    public AlterPolicy setPolicyName(String policyName) {
        this.policyName = policyName;
        return this;
    }

    public net.sf.jsqlparser.schema.Table getTable() {
        return table;
    }

    public AlterPolicy setTable(net.sf.jsqlparser.schema.Table table) {
        this.table = table;
        return this;
    }


    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
        if (newName != null) {
            options = new PolicyOptions();
        }
    }

    public PolicyOptions getOptions() {
        return options;
    }

    public void setOptions(PolicyOptions options) {
        this.options = java.util.Objects.requireNonNull(options);
        newName = null;
    }

    public StringBuilder appendTo(StringBuilder sql,
            java.util.function.Consumer<net.sf.jsqlparser.expression.Expression> printer) {
        sql.append("ALTER POLICY ").append(policyName).append(" ON ").append(table);
        if (newName != null) {
            sql.append(" RENAME TO ").append(newName);
        } else {
            options.appendTo(sql, printer);
        }
        return sql;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        return appendTo(b, b::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
