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

    private ExpressionVisitor<StringBuilder> expressionVisitor;

    public DeclareStatementDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deParse(DeclareStatement declare) {
        builder.append("DECLARE ");

        if (declare.getUserVariable() != null) {
            declare.getUserVariable().accept(expressionVisitor, null);
        }

        if (declare.getType() == DeclareType.AS) {
            builder.append(" AS ");
            builder.append(declare.getTypeName());
            return;
        }

        if (declare.getType() == DeclareType.TABLE) {
            builder.append(" TABLE (");
            for (int i = 0; i < declare.getColumnDefinitions().size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(declare.getColumnDefinitions().get(i).toString());
            }
            builder.append(")");
        } else {
            if (declare.getTypeDefinitions() != null) {
                for (int i = 0; i < declare.getTypeDefinitions().size(); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    DeclareStatement.TypeDefExpr type = declare.getTypeDefinitions().get(i);
                    if (type.userVariable != null) {
                        type.userVariable.accept(expressionVisitor, null);
                        builder.append(" ");
                    }
                    builder.append(type.colDataType.toString());
                    if (type.defaultExpr != null) {
                        builder.append(" = ");
                        type.defaultExpr.accept(expressionVisitor, null);
                    }
                }
            }
        }
    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }
}
