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
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.upsert.Upsert;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class UpsertDeParser extends AbstractDeParser<Upsert> {

    private ExpressionDeParser expressionVisitor;
    private SelectDeParser selectVisitor;

    public UpsertDeParser(ExpressionDeParser expressionVisitor, SelectDeParser selectVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.expressionVisitor.setSelectVisitor(selectVisitor);
        this.selectVisitor = selectVisitor;
        this.selectVisitor.setExpressionVisitor(expressionVisitor);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(Upsert upsert) {
        switch (upsert.getUpsertType()) {
            case REPLACE:
            case REPLACE_SET:
                buffer.append("REPLACE ");
                break;
            case INSERT_OR_ABORT:
                buffer.append("INSERT OR ABORT ");
                break;
            case INSERT_OR_FAIL:
                buffer.append("INSERT OR FAIL ");
                break;
            case INSERT_OR_IGNORE:
                buffer.append("INSERT OR IGNORE ");
                break;
            case INSERT_OR_REPLACE:
                buffer.append("INSERT OR REPLACE ");
                break;
            case INSERT_OR_ROLLBACK:
                buffer.append("INSERT OR ROLLBACK ");
                break;
            case UPSERT:
            default:
                buffer.append("UPSERT ");
        }

        if (upsert.isUsingInto()) {
            buffer.append("INTO ");
        }
        buffer.append(upsert.getTable().getFullyQualifiedName());

        if (upsert.getUpdateSets() != null) {
            buffer.append(" SET ");
            deparseUpdateSets(upsert.getUpdateSets(), buffer, expressionVisitor);
        } else {
            if (upsert.getColumns() != null) {
                upsert.getColumns().accept(expressionVisitor, null);
            }

            if (upsert.getExpressions() != null) {
                upsert.getExpressions().accept(expressionVisitor, null);
            }

            if (upsert.getSelect() != null) {
                buffer.append(" ");
                upsert.getSelect().accept(selectVisitor, null);
            }

            if (upsert.getDuplicateUpdateSets() != null) {
                buffer.append(" ON DUPLICATE KEY UPDATE ");
                deparseUpdateSets(upsert.getDuplicateUpdateSets(), buffer, expressionVisitor);
            }
        }
    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public SelectVisitor<StringBuilder> getSelectVisitor() {
        return selectVisitor;
    }

    public void setExpressionVisitor(ExpressionDeParser visitor) {
        expressionVisitor = visitor;
    }

    public void setSelectVisitor(SelectDeParser visitor) {
        selectVisitor = visitor;
    }

}
