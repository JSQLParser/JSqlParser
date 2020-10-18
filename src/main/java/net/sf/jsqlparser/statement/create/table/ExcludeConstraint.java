/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

public class ExcludeConstraint extends Index {

    private Expression expression;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        StringBuilder exclusionStatement = new StringBuilder("EXCLUDE WHERE ");
        exclusionStatement.append("(");
        exclusionStatement.append(expression);
        exclusionStatement.append(")");
        return exclusionStatement.toString();
    }

    public ExcludeConstraint withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

    @Override
    public ExcludeConstraint withName(List<String> name) {
        return (ExcludeConstraint) super.withName(name);
    }

    @Override
    public ExcludeConstraint withType(String type) {
        return (ExcludeConstraint) super.withType(type);
    }

    @Override
    public ExcludeConstraint withUsing(String using) {
        return (ExcludeConstraint) super.withUsing(using);
    }

    @Override
    public ExcludeConstraint withColumnsNames(List<String> list) {
        return (ExcludeConstraint) super.withColumnsNames(list);
    }

    @Override
    public ExcludeConstraint withColumns(List<ColumnParams> columns) {
        return (ExcludeConstraint) super.withColumns(columns);
    }

    @Override
    public ExcludeConstraint addColumns(ColumnParams... functionDeclarationParts) {
        return (ExcludeConstraint) super.addColumns(functionDeclarationParts);
    }

    @Override
    public ExcludeConstraint addColumns(Collection<? extends ColumnParams> functionDeclarationParts) {
        return (ExcludeConstraint) super.addColumns(functionDeclarationParts);
    }

    @Override
    public ExcludeConstraint withIndexSpec(List<String> idxSpec) {
        return (ExcludeConstraint) super.withIndexSpec(idxSpec);
    }

    @Override
    public ExcludeConstraint withName(String name) {
        return (ExcludeConstraint) super.withName(name);
    }

}
