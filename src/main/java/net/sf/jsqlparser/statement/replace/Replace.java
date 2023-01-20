/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replace;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Not Standard compliant REPLACE Statement
 * @deprecated
 * This class has been merged into the UPSERT statement and should not longer been used.
 * <p> Use {@link Upsert} instead.
 *
 */

@Deprecated
public class Replace extends Upsert {

    @Deprecated
    public boolean isUseIntoTables() {
        return super.isUsingInto();
    }

    @Deprecated
    public void setUseIntoTables(boolean useIntoTables) {
        super.setUsingInto( useIntoTables );
    }

    @Deprecated
    /**
     * A list of {@link net.sf.jsqlparser.expression.Expression}s (from a "REPLACE mytab SET
     * col1=exp1, col2=exp2"). <br>
     * it is null in case of a "REPLACE mytab (col1, col2) [...]"
     */
    public List<Expression> getExpressions() {
        return super.getSetExpressions();
    }

    @Deprecated
    public void setExpressions(List<Expression> list) {
        super.setItemsList( new ExpressionList(list) );
    }

    @Deprecated
    public Replace withUseIntoTables(boolean useIntoTables) {
        super.setUsingInto(useIntoTables);
        return this;
    }

    @Deprecated
    public Replace withExpressions(List<Expression> expressions) {
        super.setItemsList( new ExpressionList(expressions) );
        return this;
    }

    @Deprecated
    public Replace addExpressions(Expression... expressions) {
        List<Expression> collection = Optional.ofNullable( super.getSetExpressions() ).orElseGet(ArrayList::new);
        Collections.addAll(collection, expressions);
        return this.withExpressions(collection);
    }

    @Deprecated
    public Replace addExpressions(Collection<? extends Expression> expressions) {
        List<Expression> collection = Optional.ofNullable( super.getSetExpressions() ).orElseGet(ArrayList::new);
        collection.addAll(expressions);
        return this.withExpressions(collection);
    }
}
