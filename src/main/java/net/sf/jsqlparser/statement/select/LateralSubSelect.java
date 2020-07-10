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

/**
 * lateral sub select
 * @author tobens
 */
public class LateralSubSelect extends SpecialSubSelect {
    
    public LateralSubSelect() {
        super("LATERAL");
    }
    
    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override()
    public LateralSubSelect withPivot(Pivot pivot) {
        return (LateralSubSelect) super.withPivot(pivot);
    }

    @Override()
    public LateralSubSelect withAlias(Alias alias) {
        return (LateralSubSelect) super.withAlias(alias);
    }

    @Override()
    public LateralSubSelect withSubSelect(SubSelect subSelect) {
        return (LateralSubSelect) super.withSubSelect(subSelect);
    }

    @Override
    public LateralSubSelect withUnPivot(UnPivot unpivot) {
        return (LateralSubSelect) super.withUnPivot(unpivot);
    }

}
