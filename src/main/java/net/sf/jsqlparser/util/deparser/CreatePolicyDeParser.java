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
        builder.append("CREATE POLICY ").append(createPolicy.getPolicyName());
        builder.append(" ON ").append(createPolicy.getTable());

        if (createPolicy.getPolicyMode() != null) {
            builder.append(" AS ").append(createPolicy.getPolicyMode());
        }

        if (createPolicy.getPolicyCommand() != null) {
            builder.append(" FOR ").append(createPolicy.getPolicyCommand());
        }

        if (createPolicy.getRoles() != null && !createPolicy.getRoles().isEmpty()) {
            builder.append(" TO ").append(String.join(", ", createPolicy.getRoles()));
        }

        if (createPolicy.getUsingExpression() != null) {
            builder.append(" USING (");
            createPolicy.getUsingExpression().accept(expressionVisitor, null);
            builder.append(")");
        }

        if (createPolicy.getWithCheckExpression() != null) {
            builder.append(" WITH CHECK (");
            createPolicy.getWithCheckExpression().accept(expressionVisitor, null);
            builder.append(")");
        }
    }
}
