/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.merge.*;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.Iterator;
import java.util.List;

public class MergeDeParser extends AbstractDeParser<Merge>
        implements MergeOperationVisitor<StringBuilder> {
    private final ExpressionDeParser expressionDeParser;

    private final SelectDeParser selectDeParser;

    public MergeDeParser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser,
            StringBuilder buffer) {
        super(buffer);
        this.expressionDeParser = expressionDeParser;
        this.selectDeParser = selectDeParser;
    }

    @Override
    public void deParse(Merge merge) {
        List<WithItem<?>> withItemsList = merge.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept(selectDeParser, null);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        builder.append("MERGE ");
        if (merge.getOracleHint() != null) {
            builder.append(merge.getOracleHint()).append(" ");
        }
        builder.append("INTO ");
        if (merge.getTableReference().hasInheritanceScope()) {
            merge.getTableReference().appendWithAliasTo(builder);
        } else {
            merge.getTable().accept(selectDeParser, null);
        }

        builder.append(" USING ");
        merge.getFromItem().accept(selectDeParser, null);

        builder.append(" ON ");
        merge.getOnCondition().accept(expressionDeParser, null);

        List<MergeOperation> operations = merge.getOperations();
        if (operations != null && !operations.isEmpty()) {
            operations.forEach(operation -> operation.accept(this, null));
        }

        if (merge.getReturningClause() != null) {
            merge.getReturningClause().appendTo(builder,
                    item -> item.accept(selectDeParser, null));
        }

        if (merge.getOutputClause() != null) {
            merge.getOutputClause().appendTo(builder);
        }

        if (merge.getOption() != null) {
            if (merge.getOutputClause() != null) {
                builder.setLength(builder.length() - 1);
            }
            builder.append(merge.getOption());
        }
    }

    @Override
    public <S> StringBuilder visit(MergeDelete mergeDelete, S context) {
        deparseWhen(mergeDelete.getWhen(), mergeDelete.getAndPredicate(), context)
                .append("DELETE");
        return builder;
    }

    public void visit(MergeDelete mergeDelete) {
        visit(mergeDelete, null);
    }

    @Override
    public <S> StringBuilder visit(MergeUpdate mergeUpdate, S context) {
        deparseWhen(mergeUpdate.getWhen(), mergeUpdate.getAndPredicate(), context)
                .append("UPDATE SET ");
        deparseUpdateSets(mergeUpdate.getUpdateSets(), builder, expressionDeParser);

        if (mergeUpdate.getWhereCondition() != null) {
            builder.append(" WHERE ");
            mergeUpdate.getWhereCondition().accept(expressionDeParser, context);
        }

        if (mergeUpdate.getDeleteWhereCondition() != null) {
            builder.append(" DELETE WHERE ");
            mergeUpdate.getDeleteWhereCondition().accept(expressionDeParser, context);
        }

        return builder;
    }

    public void visit(MergeUpdate mergeUpdate) {
        visit(mergeUpdate, null);
    }

    @Override
    public <S> StringBuilder visit(MergeInsert mergeInsert, S context) {
        deparseWhen(mergeInsert.getWhen(), mergeInsert.getAndPredicate(), context);
        mergeInsert.appendInsertTo(builder,
                expression -> expression.accept(expressionDeParser, context));

        if (mergeInsert.getWhereCondition() != null) {
            builder.append(" WHERE ");
            mergeInsert.getWhereCondition().accept(expressionDeParser, context);
        }

        return builder;
    }

    public void visit(MergeInsert mergeInsert) {
        visit(mergeInsert, null);
    }

    @Override
    public <S> StringBuilder visit(MergeDoNothing mergeDoNothing, S context) {
        return deparseWhen(mergeDoNothing.getWhen(), mergeDoNothing.getAndPredicate(), context)
                .append("DO NOTHING");
    }

    private <S> StringBuilder deparseWhen(MergeWhen when, Expression andPredicate, S context) {
        return when.appendTo(builder, andPredicate,
                predicate -> predicate.accept(expressionDeParser, context));
    }

    public ExpressionDeParser getExpressionDeParser() {
        return expressionDeParser;
    }

    public SelectDeParser getSelectDeParser() {
        return selectDeParser;
    }
}
