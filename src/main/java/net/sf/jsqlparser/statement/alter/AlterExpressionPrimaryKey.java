/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import java.util.Iterator;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;

/**
 * CockroachDB's ALTER PRIMARY KEY USING COLUMNS operation. Key elements and storage options are
 * available through {@link #getConstraint()}; hash sharding and the legacy WITH BUCKET_COUNT
 * expression are represented separately.
 */
public class AlterExpressionPrimaryKey extends AlterExpression {
    private boolean usingHash;
    private Expression bucketCount;

    public AlterExpressionPrimaryKey() {
        setOperation(AlterOperation.ALTER_PRIMARY_KEY);
        setConstraint(new KeyConstraint().withType("PRIMARY KEY"));
    }

    @Override
    public boolean hasActiveTableDefinition() {
        return false;
    }

    @Override
    public KeyConstraint getConstraint() {
        return (KeyConstraint) super.getConstraint();
    }

    public boolean isUsingHash() {
        return usingHash;
    }

    public void setUsingHash(boolean usingHash) {
        this.usingHash = usingHash;
    }

    public Expression getBucketCount() {
        return bucketCount;
    }

    public void setBucketCount(Expression bucketCount) {
        this.bucketCount = bucketCount;
    }

    @Override
    public ConstraintKind getConstraintKind() {
        return ConstraintKind.PRIMARY_KEY;
    }

    @Override
    protected void appendBody(StringBuilder builder) {
        appendDefinition(builder, expression -> builder.append(expression));
    }

    /** Shares statement rendering while preserving expression visitor customization. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        appendDefinition(builder, expressionPrinter);
        appendCommonTail(builder);
        return builder;
    }

    private void appendDefinition(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append("ALTER PRIMARY KEY USING COLUMNS (");
        if (getConstraint().getColumns() != null) {
            for (Iterator<KeyElement> columns = getConstraint().getColumns().iterator(); columns
                    .hasNext();) {
                columns.next().appendTo(builder, expressionPrinter);
                if (columns.hasNext()) {
                    builder.append(", ");
                }
            }
        }
        builder.append(')');
        appendSharding(builder, expressionPrinter);
        appendStorageOptions(builder, expressionPrinter);
    }

    private void appendSharding(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (usingHash) {
            builder.append(" USING HASH");
            if (bucketCount != null) {
                builder.append(" WITH BUCKET_COUNT = ");
                expressionPrinter.accept(bucketCount);
            }
        }
    }

    private void appendStorageOptions(StringBuilder builder,
            Consumer<Expression> expressionPrinter) {
        if (getConstraint().getStorageParameters() != null) {
            builder.append(" WITH (");
            for (Iterator<IndexOption> options =
                    getConstraint().getStorageParameters().iterator(); options.hasNext();) {
                options.next().appendTo(builder, expressionPrinter);
                if (options.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(')');
        }
    }
}
