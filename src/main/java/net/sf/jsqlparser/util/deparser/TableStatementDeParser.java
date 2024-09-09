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
import net.sf.jsqlparser.statement.select.*;

/**
 * @author jxnu-liguobin
 */
public class TableStatementDeParser extends AbstractDeParser<TableStatement>
        implements SelectVisitor<StringBuilder> {

    private final ExpressionVisitor<StringBuilder> expressionVisitor;

    public TableStatementDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(TableStatement tableStatement) {
        tableStatement.accept(this, null);
    }

    public void deparse(Offset offset) {
        buffer.append(" OFFSET ");
        offset.getOffset().accept(expressionVisitor, null);
        if (offset.getOffsetParam() != null) {
            buffer.append(" ").append(offset.getOffsetParam());
        }

    }

    @Override
    public <S> StringBuilder visit(ParenthesedSelect parenthesedSelect, S context) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(PlainSelect plainSelect, S context) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SetOperationList setOperationList, S context) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(WithItem<?> withItem, S context) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Values values, S context) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LateralSubSelect lateralSubSelect, S context) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TableStatement tableStatement, S context) {
        buffer.append("TABLE ");
        buffer.append(tableStatement.getTable());
        if (tableStatement.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer)
                    .deParse(tableStatement.getOrderByElements());
        }

        if (tableStatement.getLimit() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(tableStatement.getLimit());
        }
        if (tableStatement.getOffset() != null) {
            deparse(tableStatement.getOffset());
        }

        // TODO UNION

        tableStatement.appendTo(buffer, tableStatement.getAlias(), tableStatement.getPivot(),
            tableStatement.getUnPivot());

        return buffer;
    }
}
