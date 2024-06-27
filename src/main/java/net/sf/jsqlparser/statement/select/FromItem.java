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
import net.sf.jsqlparser.parser.ASTNodeAccess;

public interface FromItem extends ASTNodeAccess {

    <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context);

    default void accept(FromItemVisitor<?> fromItemVisitor) {
        this.accept(fromItemVisitor, null);
    }

    Alias getAlias();

    void setAlias(Alias alias);

    default FromItem withAlias(Alias alias) {
        setAlias(alias);
        return this;
    }

    Pivot getPivot();

    void setPivot(Pivot pivot);

    default FromItem withPivot(Pivot pivot) {
        setPivot(pivot);
        return this;
    }

    UnPivot getUnPivot();

    void setUnPivot(UnPivot unpivot);

    default FromItem withUnPivot(UnPivot unpivot) {
        setUnPivot(unpivot);
        return this;
    }


}
