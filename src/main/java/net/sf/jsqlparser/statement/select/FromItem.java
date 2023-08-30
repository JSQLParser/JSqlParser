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

import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.expression.Alias;

public interface FromItem extends Model {

    void accept(FromItemVisitor fromItemVisitor);

    Alias getAlias();

    default FromItem withAlias(Alias alias) {
        setAlias(alias);
        return this;
    }

    void setAlias(Alias alias);

    Pivot getPivot();

    default FromItem withPivot(Pivot pivot) {
        setPivot(pivot);
        return this;
    }

    void setPivot(Pivot pivot);

    UnPivot getUnPivot();

    default FromItem withUnPivot(UnPivot unpivot) {
        setUnPivot(unpivot);
        return this;
    }

    void setUnPivot(UnPivot unpivot);


}
