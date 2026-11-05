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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;

public class InsertDeParser extends AbstractDeParser<Insert> {

    private ExpressionVisitor<StringBuilder> expressionVisitor;
    private SelectVisitor<StringBuilder> selectVisitor;

    public InsertDeParser() {
        super(new StringBuilder());
    }

    public InsertDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            SelectVisitor<StringBuilder> selectVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
    }

    /**
     * Renders the statement with {@link Insert#appendTo}, the clause layout shared with
     * {@link Insert#toString()}, visiting its WITH items, queries, expressions and RETURNING items.
     */
    @Override
    public void deParse(Insert insert) {
        Consumer<WithItem<?>> withItems = selectVisitor == null ? builder::append
                : withItem -> withItem.accept(selectVisitor, null);
        Consumer<Select> selects = selectVisitor == null ? builder::append
                : select -> select.accept(selectVisitor, null);
        Consumer<Expression> expressions = expressionVisitor == null ? builder::append
                : expression -> expression.accept(expressionVisitor, null);
        Consumer<SelectItem<?>> selectItems = builder::append;
        if (expressionVisitor != null && insert.getReturningClause() != null) {
            SelectDeParser returningItems = new SelectDeParser(expressionVisitor, builder);
            selectItems = item -> item.accept(returningItems, null);
        }
        insert.appendTo(builder, withItems, selects, expressions, selectItems);
    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }

    public SelectVisitor<StringBuilder> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<StringBuilder> visitor) {
        selectVisitor = visitor;
    }
}
