/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

public class TableSamplePipeOperator extends PipeOperator {
    double percent;

    public TableSamplePipeOperator(double percent) {
        this.percent = percent;
    }

    public TableSamplePipeOperator(String percentStr) {
        this.percent = Double.parseDouble(percentStr);
    }

    public double getPercent() {
        return percent;
    }

    public TableSamplePipeOperator setPercent(double percent) {
        this.percent = percent;
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append("TABLESAMPLE SYSTEM (").append(percent).append(" PERCENT)");
        return builder;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }
}
