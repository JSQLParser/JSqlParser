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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;

/**
 * lateral sub select
 *
 * @author tobens
 */
public class LateralSubSelect extends SpecialSubSelect implements SupportsOldOracleJoinSyntax {

    private int oldOracleJoinSyntax = NO_ORACLE_JOIN;

    public LateralSubSelect() {
        super("LATERAL");
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override
    public LateralSubSelect withPivot(Pivot pivot) {
        return (LateralSubSelect) super.withPivot(pivot);
    }

    @Override
    public LateralSubSelect withAlias(Alias alias) {
        return (LateralSubSelect) super.withAlias(alias);
    }

    @Override
    public LateralSubSelect withSubSelect(SubSelect subSelect) {
        return (LateralSubSelect) super.withSubSelect(subSelect);
    }

    @Override
    public LateralSubSelect withUnPivot(UnPivot unpivot) {
        return (LateralSubSelect) super.withUnPivot(unpivot);
    }

    @Override
    public String toString() {
        return getPrefix() + getSubSelect().toString()
                + (oldOracleJoinSyntax == ORACLE_JOIN_LEFT ? "(+)" : "")
                + ((getAlias() != null) ? getAlias().toString() : "")
                + ((getPivot() != null) ? " " + getPivot() : "")
                + ((getUnPivot() != null) ? " " + getUnPivot() : "");
    }

    @Override
    public int getOldOracleJoinSyntax() {
        return oldOracleJoinSyntax;
    }

    @Override
    public void setOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        this.oldOracleJoinSyntax = oldOracleJoinSyntax;
        if (oldOracleJoinSyntax != 0 && oldOracleJoinSyntax != 2) {
            throw new IllegalArgumentException("unknown join type for oracle found (type=" + oldOracleJoinSyntax + ")");
        }
    }

    @Override
    public SupportsOldOracleJoinSyntax withOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        this.setOldOracleJoinSyntax(oldOracleJoinSyntax);
        return this;
    }

    @Override
    public int getOraclePriorPosition() {
        return SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR;
    }

    @Override
    public void setOraclePriorPosition(int priorPosition) {
        if (priorPosition != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR) {
            throw new IllegalArgumentException("unexpected prior for oracle found");
        }
    }

    @Override
    public SupportsOldOracleJoinSyntax withOraclePriorPosition(int priorPosition) {
        this.setOraclePriorPosition(priorPosition);
        return this;
    }
}
