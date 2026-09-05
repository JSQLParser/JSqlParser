/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.policy;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL CREATE POLICY statement for Row Level Security (RLS).
 *
 * Syntax: CREATE POLICY name ON table_name [ AS { PERMISSIVE | RESTRICTIVE } ] [ FOR { ALL | SELECT
 * | INSERT | UPDATE | DELETE } ] [ TO { role_name | PUBLIC | CURRENT_ROLE | CURRENT_USER |
 * SESSION_USER } [, ...] ] [ USING ( using_expression ) ] [ WITH CHECK ( check_expression ) ]
 */
public class CreatePolicy implements Statement {

    private String policyName;
    private Table table;
    private PolicyMode policyMode;
    private PolicyCommand policyCommand;
    private List<String> roles = new ArrayList<>();
    private Expression usingExpression;
    private Expression withCheckExpression;

    public String getPolicyName() {
        return policyName;
    }

    public CreatePolicy setPolicyName(String policyName) {
        this.policyName = policyName;
        return this;
    }

    public Table getTable() {
        return table;
    }

    public CreatePolicy setTable(Table table) {
        this.table = table;
        return this;
    }

    /**
     * Returns the explicitly specified policy mode, or {@code null} when the {@code AS} clause was
     * omitted.
     *
     * @return the explicitly specified policy mode
     */
    public PolicyMode getPolicyMode() {
        return policyMode;
    }

    public CreatePolicy setPolicyMode(PolicyMode policyMode) {
        this.policyMode = policyMode;
        return this;
    }

    /**
     * Returns the effective PostgreSQL policy mode, including the default for an omitted {@code AS}
     * clause.
     *
     * @return the explicit policy mode, or {@link PolicyMode#PERMISSIVE}
     */
    public PolicyMode getEffectivePolicyMode() {
        return policyMode != null ? policyMode : PolicyMode.PERMISSIVE;
    }

    /**
     * Returns the explicitly specified command as a string for backwards compatibility.
     *
     * @return the explicitly specified command, or {@code null} when the {@code FOR} clause was
     *         omitted
     */
    public String getCommand() {
        return policyCommand != null ? policyCommand.name() : null;
    }

    public CreatePolicy setCommand(String command) {
        this.policyCommand = command != null ? PolicyCommand.from(command) : null;
        return this;
    }

    /**
     * Returns the explicitly specified command, or {@code null} when the {@code FOR} clause was
     * omitted.
     *
     * @return the explicitly specified command
     */
    public PolicyCommand getPolicyCommand() {
        return policyCommand;
    }

    public CreatePolicy setPolicyCommand(PolicyCommand policyCommand) {
        this.policyCommand = policyCommand;
        return this;
    }

    /**
     * Returns the effective PostgreSQL command, including the default for an omitted {@code FOR}
     * clause.
     *
     * @return the explicit command, or {@link PolicyCommand#ALL}
     */
    public PolicyCommand getEffectivePolicyCommand() {
        return policyCommand != null ? policyCommand : PolicyCommand.ALL;
    }

    public List<String> getRoles() {
        return roles;
    }

    public CreatePolicy setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public CreatePolicy addRole(String role) {
        this.roles.add(role);
        return this;
    }

    public Expression getUsingExpression() {
        return usingExpression;
    }

    public CreatePolicy setUsingExpression(Expression usingExpression) {
        this.usingExpression = usingExpression;
        return this;
    }

    public Expression getWithCheckExpression() {
        return withCheckExpression;
    }

    public CreatePolicy setWithCheckExpression(Expression withCheckExpression) {
        this.withCheckExpression = withCheckExpression;
        return this;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CREATE POLICY ");
        builder.append(policyName);
        builder.append(" ON ");
        builder.append(table.toString());

        if (policyMode != null) {
            builder.append(" AS ").append(policyMode);
        }

        if (policyCommand != null) {
            builder.append(" FOR ").append(policyCommand);
        }

        if (roles != null && !roles.isEmpty()) {
            builder.append(" TO ");
            for (int i = 0; i < roles.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(roles.get(i));
            }
        }

        if (usingExpression != null) {
            builder.append(" USING (").append(usingExpression.toString()).append(")");
        }

        if (withCheckExpression != null) {
            builder.append(" WITH CHECK (").append(withCheckExpression.toString()).append(")");
        }

        return builder.toString();
    }
}
