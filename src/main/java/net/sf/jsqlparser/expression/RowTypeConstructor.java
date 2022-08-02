/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.ArrayList;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

public class RowTypeConstructor extends ASTNodeAccessImpl implements Expression {
    private ExpressionList exprList;
    private ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();
    private String name = null;

    public RowTypeConstructor() {
    }
    
    public ArrayList<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }
    
    public boolean addColumnDefinition(ColumnDefinition columnDefinition) {
        return columnDefinitions.add(columnDefinition);
    }

    public void addExpression( Expression expr) {
        if (exprList == null ) {
            exprList = new ExpressionList();
        }
        exprList.addExpressions(expr);
    }
    public ExpressionList getExprList() {
        return exprList;
    }

    public void setExprList(ExpressionList exprList) {
        this.exprList = exprList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        if (columnDefinitions.size()>0) {
            StringBuilder builder = new StringBuilder(name != null ? name : "");
            builder.append("(");
            int i = 0;
            for (ColumnDefinition columnDefinition:columnDefinitions) {
                builder.append(i>0 ? ", " : "").append(columnDefinition.toString());
                i++;
            }
            builder.append(")");
            return builder.toString();
        }
        return (name != null ? name : "") + exprList.toString();
    }

    public RowTypeConstructor withExprList(ExpressionList exprList) {
        this.setExprList(exprList);
        return this;
    }

    public RowTypeConstructor withName(String name) {
        this.setName(name);
        return this;
    }
}
