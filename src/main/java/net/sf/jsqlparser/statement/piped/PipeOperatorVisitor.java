package net.sf.jsqlparser.statement.piped;

public interface PipeOperatorVisitor<T> {
    <S> T visit(AggregatePipeOperator aggregate, S context);

    <S> T visit(AsPipeOperator as, S context);

    <S> T visit(CallPipeOperator call, S context);

    <S> T visit(DropPipeOperator drop, S context);

    <S> T visit(ExceptPipeOperator except, S context);

    <S> T visit(ExtendPipeOperator extend, S context);

    <S> T visit(IntersectPipeOperator intersect, S context);

    <S> T visit(JoinPipeOperator join, S context);

    <S> T visit(LimitPipeOperator limit, S context);

    <S> T visit(OrderByPipeOperator orderBy, S context);

    <S> T visit(PivotPipeOperator pivot, S context);

    <S> T visit(RenamePipeOperator rename, S context);

    <S> T visit(SelectPipeOperator select, S context);

    <S> T visit(SetPipeOperator set, S context);

    <S> T visit(TableSamplePipeOperator tableSample, S context);

    <S> T visit(SetOperationPipeOperator union, S context);

    <S> T visit(UnPivotPipeOperator unPivot, S context);

    <S> T visit(WherePipeOperator where, S context);

    <S> T visit(WindowPipeOperator window, S context);
}

