/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.values;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitor;

import java.util.ArrayList;
import java.util.Collection;

public class ValuesStatement extends SelectBody implements Statement {

    private ItemsList expressions;

    public ValuesStatement() {
        // empty constructor
    }

    public ValuesStatement(ItemsList expressions) {
        this.expressions = expressions;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public ItemsList getExpressions() {
        return expressions;
    }

    public void setExpressions(ItemsList expressions) {
        this.expressions = expressions;
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("VALUES ");
        builder.append(expressions.toString());
        return builder;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public ValuesStatement withExpressions(ItemsList expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public ValuesStatement addExpressions(Expression... addExpressions) {
        if (expressions != null && expressions instanceof ExpressionList) {
            ((ExpressionList) expressions).addExpressions(addExpressions);
            return this;
        } else {
            return this.withExpressions(new ExpressionList(addExpressions));
        }
    }

    public ValuesStatement addExpressions(Collection<? extends Expression> addExpressions) {
        if (expressions != null && expressions instanceof ExpressionList) {
            ((ExpressionList) expressions).addExpressions(addExpressions);
            return this;
        } else {
            return this.withExpressions(new ExpressionList(new ArrayList<>(addExpressions)));
        }
    }
}
