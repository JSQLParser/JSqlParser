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
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;

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

    public void visit(Offset offset) {
        buffer.append(" OFFSET ");
        offset.getOffset().accept(expressionVisitor, null);
        if (offset.getOffsetParam() != null) {
            buffer.append(" ").append(offset.getOffsetParam());
        }

    }

    @Override
    public <S> StringBuilder visit(ParenthesedSelect parenthesedSelect, S parameters) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(PlainSelect plainSelect, S parameters) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SetOperationList setOperationList, S parameters) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(WithItem withItem, S parameters) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Values values, S parameters) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LateralSubSelect lateralSubSelect, S parameters) {

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TableStatement tableStatement, S parameters) {
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
            visit(tableStatement.getOffset());
        }

        // TODO UNION
        return buffer;
    }
}
