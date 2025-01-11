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
        merge.getTable().accept(selectDeParser, null);

        builder.append(" USING ");
        merge.getFromItem().accept(selectDeParser, null);

        builder.append(" ON ");
        merge.getOnCondition().accept(expressionDeParser, null);

        List<MergeOperation> operations = merge.getOperations();
        if (operations != null && !operations.isEmpty()) {
            operations.forEach(operation -> operation.accept(this, null));
        }

        if (merge.getOutputClause() != null) {
            merge.getOutputClause().appendTo(builder);
        }
    }

    @Override
    public <S> StringBuilder visit(MergeDelete mergeDelete, S context) {
        builder.append(" WHEN MATCHED");
        if (mergeDelete.getAndPredicate() != null) {
            builder.append(" AND ");
            mergeDelete.getAndPredicate().accept(expressionDeParser, context);
        }
        builder.append(" THEN DELETE");
        return builder;
    }

    public void visit(MergeDelete mergeDelete) {
        visit(mergeDelete, null);
    }

    @Override
    public <S> StringBuilder visit(MergeUpdate mergeUpdate, S context) {
        builder.append(" WHEN MATCHED");
        if (mergeUpdate.getAndPredicate() != null) {
            builder.append(" AND ");
            mergeUpdate.getAndPredicate().accept(expressionDeParser, context);
        }
        builder.append(" THEN UPDATE SET ");
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
        builder.append(" WHEN NOT MATCHED");
        if (mergeInsert.getAndPredicate() != null) {
            builder.append(" AND ");
            mergeInsert.getAndPredicate().accept(expressionDeParser, context);
        }
        builder.append(" THEN INSERT ");
        if (mergeInsert.getColumns() != null) {
            mergeInsert.getColumns().accept(expressionDeParser, context);
        }
        builder.append(" VALUES ");
        mergeInsert.getValues().accept(expressionDeParser, context);

        if (mergeInsert.getWhereCondition() != null) {
            builder.append(" WHERE ");
            mergeInsert.getWhereCondition().accept(expressionDeParser, context);
        }

        return builder;
    }

    public void visit(MergeInsert mergeInsert) {
        visit(mergeInsert, null);
    }

    public ExpressionDeParser getExpressionDeParser() {
        return expressionDeParser;
    }

    public SelectDeParser getSelectDeParser() {
        return selectDeParser;
    }
}
