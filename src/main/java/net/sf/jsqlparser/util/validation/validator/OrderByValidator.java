/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class OrderByValidator extends AbstractValidator<OrderByElement> implements OrderByVisitor {

    @Override
    public void validate(OrderByElement element) {
        element.accept(this);
    }

    @Override
    public void visit(OrderByElement orderBy) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.orderBy);
            validateOptionalFeature(c, orderBy.getNullOrdering(), Feature.orderByNullOrdering);
        }
        getValidator(ExpressionValidator.class).validate(orderBy.getExpression());
    }
}
