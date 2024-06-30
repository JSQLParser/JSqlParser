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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.GroupByVisitor;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class GroupByValidator<Void> extends AbstractValidator<GroupByElement>
        implements GroupByVisitor<Void> {

    @Override
    public void validate(GroupByElement groupBy) {
        groupBy.accept(this, null);
    }

    @Override
    public <S> Void visit(GroupByElement groupBy, S context) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.selectGroupBy);
            if (isNotEmpty(groupBy.getGroupingSets())) {
                validateFeature(c, Feature.selectGroupByGroupingSets);
            }
        }

        validateOptionalExpressions(groupBy.getGroupByExpressions());

        if (isNotEmpty(groupBy.getGroupingSets())) {
            for (Object o : groupBy.getGroupingSets()) {
                if (o instanceof Expression) {
                    validateOptionalExpression((Expression) o);
                } else if (o instanceof ExpressionList) {
                    validateOptionalExpressions(((ExpressionList) o).getExpressions());
                }
            }
        }
        return null;
    }

}
