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

public interface PipeOperatorVisitor<T, S> {
    T visit(AggregatePipeOperator aggregate, S context);

    T visit(AsPipeOperator as, S context);

    T visit(CallPipeOperator call, S context);

    T visit(DropPipeOperator drop, S context);

    T visit(ExtendPipeOperator extend, S context);

    T visit(JoinPipeOperator join, S context);

    T visit(LimitPipeOperator limit, S context);

    T visit(OrderByPipeOperator orderBy, S context);

    T visit(PivotPipeOperator pivot, S context);

    T visit(RenamePipeOperator rename, S context);

    T visit(SelectPipeOperator select, S context);

    T visit(SetPipeOperator set, S context);

    T visit(TableSamplePipeOperator tableSample, S context);

    T visit(SetOperationPipeOperator union, S context);

    T visit(UnPivotPipeOperator unPivot, S context);

    T visit(WherePipeOperator where, S context);

    T visit(WindowPipeOperator window, S context);
}

