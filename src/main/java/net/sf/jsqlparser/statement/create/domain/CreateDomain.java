/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class CreateDomain extends ASTNodeAccessImpl implements Statement {
    private String name;
    private boolean useAs;
    private ColDataType dataType;
    private String collation;
    private Expression defaultExpression;
    private List<DomainConstraint> constraints = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseAs() {
        return useAs;
    }

    public void setUseAs(boolean useAs) {
        this.useAs = useAs;
    }

    public ColDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColDataType dataType) {
        this.dataType = dataType;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public Expression getDefaultExpression() {
        return defaultExpression;
    }

    public void setDefaultExpression(Expression defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    public List<DomainConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<DomainConstraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressions) {
        sql.append("CREATE DOMAIN ").append(name).append(useAs ? " AS " : " ").append(dataType);
        if (collation != null) {
            sql.append(" COLLATE ").append(collation);
        }
        if (defaultExpression != null) {
            sql.append(" DEFAULT ");
            expressions.accept(defaultExpression);
        }
        for (DomainConstraint constraint : constraints) {
            sql.append(' ');
            constraint.appendTo(sql, expressions);
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}
