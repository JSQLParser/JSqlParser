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

import java.util.List;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.select.*;

/** Shared source rendering for DML and the statements nested in WITH items. */
final class DmlDeParserSupport {
    private final ExpressionVisitor<StringBuilder> expressions;
    private final SelectDeParser selects;
    private final StringBuilder builder;

    DmlDeParserSupport(ExpressionVisitor<StringBuilder> expressions, SelectDeParser selects,
            StringBuilder builder) {
        this.expressions = expressions;
        this.builder = builder;
        SelectDeParser selected = selects;
        if (selected == null && expressions instanceof ExpressionDeParser) {
            SelectVisitor<StringBuilder> visitor =
                    ((ExpressionDeParser) expressions).getSelectVisitor();
            if (visitor instanceof SelectDeParser) {
                selected = (SelectDeParser) visitor;
            }
        }
        this.selects = selected == null ? new SelectDeParser(expressions, builder) : selected;
        this.selects.setBuilder(builder);
        this.selects.setExpressionVisitor(expressions);
    }

    <S> void deparseWithItems(List<WithItem<?>> items, S context) {
        if (items != null && !items.isEmpty()) {
            builder.append("WITH ");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                selects.visit(items.get(i), context);
            }
            builder.append(' ');
        }
    }

    void deparseJoins(List<Join> joins) {
        if (joins != null) {
            for (Join join : joins) {
                selects.deparseJoin(join);
            }
        }
    }

    void deparseFrom(FromItem item, List<Join> joins) {
        if (item != null) {
            builder.append(" FROM ");
            item.accept(selects, null);
            deparseJoins(joins);
        }
    }

    void deparseUsing(List<FromItem> items) {
        if (items != null && !items.isEmpty()) {
            builder.append(" USING ");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                items.get(i).accept(selects, null);
            }
        }
    }

    <S> StringBuilder deparseWithItem(WithItem<?> item, S context) {
        if (item.getWithFunctionDeclaration() != null) {
            return builder.append(item.getWithFunctionDeclaration());
        }
        if (item.isRecursive()) {
            builder.append("RECURSIVE ");
        }
        builder.append(item.getAlias().getName());
        if (item.getWithItemList() != null) {
            builder.append(' ')
                    .append(PlainSelect.getStringList(item.getWithItemList(), true, true));
        }
        builder.append(" AS ");
        if (item.isMaterialized()) {
            builder.append(item.isUsingNot() ? "NOT MATERIALIZED " : "MATERIALIZED ");
        }
        item.getParenthesedStatement().accept(new StatementVisitorAdapter<StringBuilder>() {
            @Override
            public <T> StringBuilder visit(Select select, T nestedContext) {
                return select.accept((SelectVisitor<StringBuilder>) selects, nestedContext);
            }

            @Override
            public <T> StringBuilder visit(ParenthesedInsert insert, T nestedContext) {
                deparseWithItems(insert.getWithItemsList(), nestedContext);
                builder.append('(');
                new InsertDeParser(expressions, selects, builder).deParse(insert.getInsert());
                return builder.append(')');
            }

            @Override
            public <T> StringBuilder visit(ParenthesedUpdate update, T nestedContext) {
                deparseWithItems(update.getWithItemsList(), nestedContext);
                builder.append('(');
                new UpdateDeParser(expressions, selects, builder).deParse(update.getUpdate());
                return builder.append(')');
            }

            @Override
            public <T> StringBuilder visit(ParenthesedDelete delete, T nestedContext) {
                deparseWithItems(delete.getWithItemsList(), nestedContext);
                builder.append('(');
                new DeleteDeParser(expressions, selects, builder).deParse(delete.getDelete());
                return builder.append(')');
            }
        }, context);
        return item.appendRecursiveClausesTo(builder,
                expression -> expression.accept(expressions, context));
    }
}
