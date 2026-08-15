/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;

import net.sf.jsqlparser.expression.Expression;

/**
 * Models the ClickHouse {@code WITH FILL} modifier of an {@link OrderByElement}, which fills gaps
 * in the sorted sequence of the order by expression (see
 * <a href="https://clickhouse.com/docs/en/sql-reference/statements/select/order-by">ClickHouse:
 * ORDER BY</a>).
 * <p>
 * The modifier takes optional {@code FROM}, {@code TO}, {@code STEP} and {@code STALENESS} bounds,
 * where the step may be an {@code INTERVAL} literal. Its presence on an {@link OrderByElement}
 * marks the element as filled.
 */
public class WithFill implements Serializable {

    private Expression from;
    private Expression to;
    private Expression step;
    private Expression staleness;

    public Expression getFrom() {
        return from;
    }

    public void setFrom(Expression from) {
        this.from = from;
    }

    public WithFill withFrom(Expression from) {
        setFrom(from);
        return this;
    }

    public Expression getTo() {
        return to;
    }

    public void setTo(Expression to) {
        this.to = to;
    }

    public WithFill withTo(Expression to) {
        setTo(to);
        return this;
    }

    public Expression getStep() {
        return step;
    }

    public void setStep(Expression step) {
        this.step = step;
    }

    public WithFill withStep(Expression step) {
        setStep(step);
        return this;
    }

    public Expression getStaleness() {
        return staleness;
    }

    public void setStaleness(Expression staleness) {
        this.staleness = staleness;
    }

    public WithFill withStaleness(Expression staleness) {
        setStaleness(staleness);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("WITH FILL");
        if (from != null) {
            b.append(" FROM ").append(from);
        }
        if (to != null) {
            b.append(" TO ").append(to);
        }
        if (step != null) {
            b.append(" STEP ").append(step);
        }
        if (staleness != null) {
            b.append(" STALENESS ").append(staleness);
        }
        return b.toString();
    }
}
