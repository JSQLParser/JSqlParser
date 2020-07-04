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
    public LateralSubSelect pivot(Pivot pivot) {
        return (LateralSubSelect) super.pivot(pivot);
    }

    @Override()
    public LateralSubSelect alias(Alias alias) {
        return (LateralSubSelect) super.alias(alias);
    }

    @Override()
    public LateralSubSelect subSelect(SubSelect subSelect) {
        return (LateralSubSelect) super.subSelect(subSelect);
    }
}
