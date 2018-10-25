/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string) a
 * {@link net.sf.jsqlparser.statement.replace.Replace}
 */
public class ReplaceDeParser implements ItemsListVisitor {

    protected StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;
    private SelectVisitor selectVisitor;

    public ReplaceDeParser() {
    }

    /**
     * @param expressionVisitor a {@link ExpressionVisitor} to de-parse expressions. It has to share
     * the same<br>
     * StringBuilder (buffer parameter) as this object in order to work
     * @param selectVisitor a {@link SelectVisitor} to de-parse
     * {@link net.sf.jsqlparser.statement.select.Select}s. It has to share the same<br>
     * StringBuilder (buffer parameter) as this object in order to work
     * @param buffer the buffer that will be filled with the select
     */
    public ReplaceDeParser(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer) {
        this.buffer = buffer;
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(Replace replace) {
        buffer.append("REPLACE ");
        if (replace.isUseIntoTables()) {
            buffer.append("INTO ");
        }
        buffer.append(replace.getTable().getFullyQualifiedName());
        if (replace.getItemsList() != null) {
            if (replace.getColumns() != null) {
                buffer.append(" (");
                for (int i = 0; i < replace.getColumns().size(); i++) {
                    Column column = replace.getColumns().get(i);
                    buffer.append(column.getFullyQualifiedName());
                    if (i < replace.getColumns().size() - 1) {
                        buffer.append(", ");
                    }
                }
                buffer.append(") ");
            } else {
                buffer.append(" ");
            }

        } else {
            buffer.append(" SET ");
            for (int i = 0; i < replace.getColumns().size(); i++) {
                Column column = replace.getColumns().get(i);
                buffer.append(column.getFullyQualifiedName()).append("=");

                Expression expression = replace.getExpressions().get(i);
                expression.accept(expressionVisitor);
                if (i < replace.getColumns().size() - 1) {
                    buffer.append(", ");
                }

            }
        }

        if (replace.getItemsList() != null) {
            // REPLACE mytab SELECT * FROM mytab2
            // or VALUES ('as', ?, 565)
            replace.getItemsList().accept(this);
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append("VALUES (");
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = iter.next();
            expression.accept(expressionVisitor);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(selectVisitor);
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }

    public void setSelectVisitor(SelectVisitor visitor) {
        selectVisitor = visitor;
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append("VALUES ");
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            buffer.append("(");
            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
                Expression expression = iter.next();
                expression.accept(expressionVisitor);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
    }
}
