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

import java.util.Iterator;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 * CockroachDB's ALTER PRIMARY KEY USING COLUMNS operation. Key elements and storage options are
 * available through {@link #getIndex()}; hash sharding and the legacy WITH BUCKET_COUNT expression
 * are represented separately.
 */
public class AlterExpressionPrimaryKey extends AlterExpression {
    private boolean usingHash;
    private Expression bucketCount;

    public AlterExpressionPrimaryKey() {
        setOperation(AlterOperation.ALTER_PRIMARY_KEY);
        setIndex(new Index().withType("PRIMARY KEY"));
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
    public Index.Kind getConstraintKind() {
        return Index.Kind.PRIMARY_KEY;
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
        if (getIndex().getColumns() != null) {
            for (Iterator<Index.ColumnParams> columns = getIndex().getColumns().iterator(); columns
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
        if (getIndex().getStorageParameters() != null) {
            builder.append(" WITH (");
            for (Iterator<Index.Option> options =
                    getIndex().getStorageParameters().iterator(); options.hasNext();) {
                options.next().appendTo(builder, expressionPrinter);
                if (options.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(')');
        }
    }
}
