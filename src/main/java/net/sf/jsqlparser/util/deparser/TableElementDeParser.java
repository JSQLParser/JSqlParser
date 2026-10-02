/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.TableElement;

/** Deparses table elements while preserving expression visitor customization. */
public class TableElementDeParser extends AbstractDeParser<TableElement> {
    private final ExpressionVisitor<StringBuilder> expressionVisitor;

    public TableElementDeParser(StringBuilder builder,
            ExpressionVisitor<StringBuilder> expressionVisitor) {
        super(builder);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(TableElement element) {
        if (element instanceof Index) {
            ((Index) element).appendTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
        } else if (element instanceof NamedConstraint) {
            ((NamedConstraint) element).appendTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
        } else if (element instanceof ColumnDefinition) {
            ((ColumnDefinition) element).appendTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
        } else {
            builder.append(element);
        }
    }

}
