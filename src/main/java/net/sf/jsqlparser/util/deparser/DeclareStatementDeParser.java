/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DeclareType;

public class DeclareStatementDeParser extends AbstractDeParser<DeclareStatement> {

    private ExpressionVisitor expressionVisitor;

    public DeclareStatementDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(DeclareStatement declare) {
        buffer.append("DECLARE ");

        if (declare.getUserVariable() != null) {
            declare.getUserVariable().accept(expressionVisitor);
        }

        if (declare.getType() == DeclareType.AS) {
            buffer.append(" AS ");
            buffer.append(declare.getTypeName());
            return;
        }

        if (declare.getType() == DeclareType.TABLE) {
            buffer.append(" TABLE (");
            for (int i = 0; i < declare.getColumnDefinitions().size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(declare.getColumnDefinitions().get(i).toString());
            }
            buffer.append(")");
        } else {
            if (declare.getTypeDefinitions() != null) {
                for (int i = 0; i < declare.getTypeDefinitions().size(); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    DeclareStatement.TypeDefExpr type = declare.getTypeDefinitions().get(i);
                    if (type.userVariable != null) {
                        type.userVariable.accept(expressionVisitor);
                        buffer.append(" ");
                    }
                    buffer.append(type.colDataType.toString());
                    if (type.defaultExpr != null) {
                        buffer.append(" = ");
                        type.defaultExpr.accept(expressionVisitor);
                    }
                }
            }
        }
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }
}
