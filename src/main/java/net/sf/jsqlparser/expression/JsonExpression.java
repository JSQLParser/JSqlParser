/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

/**
 *
 * @author toben
 */
public class JsonExpression extends ASTNodeAccessImpl implements Expression {

    private Column column;

    private List<String> idents = new ArrayList<String>();
    private List<String> operators = new ArrayList<String>();

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

//    public List<String> getIdents() {
//        return idents;
//    }
//
//    public void setIdents(List<String> idents) {
//        this.idents = idents;
//        operators = new ArrayList<String>();
//        for (String ident : idents) {
//            operators.add("->");
//        }
//    }
//    
//    public void addIdent(String ident) {
//        addIdent(ident, "->");
//    }
    public void addIdent(String ident, String operator) {
        idents.add(ident);
        operators.add(operator);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(column.toString());
        for (int i = 0; i < idents.size(); i++) {
            b.append(operators.get(i)).append(idents.get(i));
        }
        return b.toString();
    }
}
