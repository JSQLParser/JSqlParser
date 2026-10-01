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
import net.sf.jsqlparser.statement.select.Values;

public class ValuesStatementDeParser extends AbstractDeParser<Values> {

    private final ExpressionVisitor<StringBuilder> expressionVisitor;
    private final SelectDeParser selectDeParser;

    public ValuesStatementDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        this(expressionVisitor, null, buffer);
    }

    public ValuesStatementDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            SelectDeParser selectDeParser, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.selectDeParser = selectDeParser;
    }

    @Override
    public void deParse(Values values) {
        deParse(values, null);
    }

    public <S> void deParse(Values values, S context) {
        new DmlDeParserSupport(expressionVisitor, selectDeParser, builder)
                .deparseWithItems(values.getWithItemsList(), context);
        builder.append("VALUES ");
        values.getExpressions().accept(expressionVisitor, context);
        if (values.getAlias() != null) {
            builder.append(" ").append(values.getAlias());
        }
    }
}
