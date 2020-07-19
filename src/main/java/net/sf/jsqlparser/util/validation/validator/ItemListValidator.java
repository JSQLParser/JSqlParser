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

import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ItemListValidator extends AbstractValidator<ItemsList> implements ItemsListVisitor {

    @Override
    public void visit(SubSelect subSelect) {
        SelectValidator v = getValidator(SelectValidator.class);
        if (subSelect.getWithItemsList() != null) {
            subSelect.getWithItemsList().forEach(withItem -> withItem.accept(v));
        }
        subSelect.getSelectBody().accept(v);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        validateOptionalExpressions(expressionList.getExpressions());
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        validateOptionalExpressions(namedExpressionList.getExpressions());
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        multiExprList.getExpressionLists().forEach(l -> l.accept(this));
    }

    @Override
    public void validate(ItemsList statement) {
        statement.accept(this);
    }

}
