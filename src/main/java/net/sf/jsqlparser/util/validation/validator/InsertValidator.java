/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class InsertValidator extends AbstractValidator<Insert> implements ItemsListVisitor {


    @Override
    public void validate(Insert insert) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.insert);
        }
        //        buffer.append("INSERT ");
        //        if (insert.getModifierPriority() != null) {
        //            buffer.append(insert.getModifierPriority()).append(" ");
        //        }
        //        if (insert.isModifierIgnore()) {
        //            buffer.append("IGNORE ");
        //        }
        //        buffer.append("INTO ");
        //
        //        buffer.append(insert.getTable().toString());
        //
        //        if (insert.getColumns() != null) {
        //            buffer.append(" (");
        //            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext();) {
        //                Column column = iter.next();
        //                buffer.append(column.getColumnName());
        //                if (iter.hasNext()) {
        //                    buffer.append(", ");
        //                }
        //            }
        //            buffer.append(")");
        //        }
        //
        //        if (insert.getItemsList() != null) {
        //            insert.getItemsList().accept(this);
        //        }
        //
        //        if (insert.getSelect() != null) {
        //            buffer.append(" ");
        //            if (insert.isUseSelectBrackets()) {
        //                buffer.append("(");
        //            }
        //            if (insert.getSelect().getWithItemsList() != null) {
        //                buffer.append("WITH ");
        //                for (WithItem with : insert.getSelect().getWithItemsList()) {
        //                    with.accept(selectVisitor);
        //                }
        //                buffer.append(" ");
        //            }
        //            insert.getSelect().getSelectBody().accept(selectVisitor);
        //            if (insert.isUseSelectBrackets()) {
        //                buffer.append(")");
        //            }
        //        }
        //
        //        if (insert.isUseSet()) {
        //            buffer.append(" SET ");
        //            for (int i = 0; i < insert.getSetColumns().size(); i++) {
        //                Column column = insert.getSetColumns().get(i);
        //                column.accept(expressionVisitor);
        //
        //                buffer.append(" = ");
        //
        //                Expression expression = insert.getSetExpressionList().get(i);
        //                expression.accept(expressionVisitor);
        //                if (i < insert.getSetColumns().size() - 1) {
        //                    buffer.append(", ");
        //                }
        //            }
        //        }
        //
        //        if (insert.isUseDuplicate()) {
        //            buffer.append(" ON DUPLICATE KEY UPDATE ");
        //            for (int i = 0; i < insert.getDuplicateUpdateColumns().size(); i++) {
        //                Column column = insert.getDuplicateUpdateColumns().get(i);
        //                buffer.append(column.getFullyQualifiedName()).append(" = ");
        //
        //                Expression expression = insert.getDuplicateUpdateExpressionList().get(i);
        //                expression.accept(expressionVisitor);
        //                if (i < insert.getDuplicateUpdateColumns().size() - 1) {
        //                    buffer.append(", ");
        //                }
        //            }
        //        }
        //
        //        if (insert.isReturningAllColumns()) {
        //            buffer.append(" RETURNING *");
        //        } else if (insert.getReturningExpressionList() != null) {
        //            buffer.append(" RETURNING ");
        //            for (Iterator<SelectExpressionItem> iter = insert.getReturningExpressionList().iterator(); iter
        //                    .hasNext();) {
        //                buffer.append(iter.next().toString());
        //                if (iter.hasNext()) {
        //                    buffer.append(", ");
        //                }
        //            }
        //        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        //        buffer.append(" VALUES (");
        //        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
        //            Expression expression = iter.next();
        //            expression.accept(expressionVisitor);
        //            if (iter.hasNext()) {
        //                buffer.append(", ");
        //            }
        //        }
        //        buffer.append(")");
    }

    @Override
    public void visit(NamedExpressionList NamedExpressionList) {
        // not used in a top-level insert statement
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        //        buffer.append(" VALUES ");
        //        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
        //            buffer.append("(");
        //            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
        //                Expression expression = iter.next();
        //                expression.accept(expressionVisitor);
        //                if (iter.hasNext()) {
        //                    buffer.append(", ");
        //                }
        //            }
        //            buffer.append(")");
        //            if (it.hasNext()) {
        //                buffer.append(", ");
        //            }
        //        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(getValidator(SelectValidator.class));
    }

}
