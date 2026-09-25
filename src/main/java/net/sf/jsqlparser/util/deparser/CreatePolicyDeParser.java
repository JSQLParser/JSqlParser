/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;

public class CreatePolicyDeParser extends AbstractDeParser<CreatePolicy> {

    private final ExpressionVisitor<StringBuilder> expressionVisitor;

    public CreatePolicyDeParser(StringBuilder builder) {
        super(builder);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        expressionDeParser.setBuilder(builder);
        this.expressionVisitor = expressionDeParser;
    }

    public CreatePolicyDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder builder) {
        super(builder);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(CreatePolicy createPolicy) {
        createPolicy.appendTo(builder, expression -> expression.accept(expressionVisitor, null));
    }
}
