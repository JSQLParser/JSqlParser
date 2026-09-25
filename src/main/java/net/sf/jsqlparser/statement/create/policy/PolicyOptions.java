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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/** Roles and predicates shared by PostgreSQL CREATE and ALTER POLICY. */
public class PolicyOptions implements Serializable {
    private List<String> roles = new ArrayList<>();
    private Expression usingExpression;
    private Expression withCheckExpression;

    public List<String> getRoles() {
        return roles;
    }

    public PolicyOptions setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public Expression getUsingExpression() {
        return usingExpression;
    }

    public PolicyOptions setUsingExpression(Expression usingExpression) {
        this.usingExpression = usingExpression;
        return this;
    }

    public Expression getWithCheckExpression() {
        return withCheckExpression;
    }

    public PolicyOptions setWithCheckExpression(Expression withCheckExpression) {
        this.withCheckExpression = withCheckExpression;
        return this;
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        if (usingExpression != null) {
            visitor.accept(usingExpression);
        }
        if (withCheckExpression != null) {
            visitor.accept(withCheckExpression);
        }
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> printer) {
        if (roles != null && !roles.isEmpty()) {
            sql.append(" TO ").append(String.join(", ", roles));
        }
        if (usingExpression != null) {
            sql.append(" USING (");
            printer.accept(usingExpression);
            sql.append(')');
        }
        if (withCheckExpression != null) {
            sql.append(" WITH CHECK (");
            printer.accept(withCheckExpression);
            sql.append(')');
        }
    }
}
