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

import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class ValuesStatementDeParser extends AbstractDeParser<ValuesStatement> {

    private final ItemsListVisitor expressionVisitor;

    public ValuesStatementDeParser(ItemsListVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(ValuesStatement values) {
        buffer.append("VALUES ");
        values.getExpressions().accept(expressionVisitor);
    }
}
