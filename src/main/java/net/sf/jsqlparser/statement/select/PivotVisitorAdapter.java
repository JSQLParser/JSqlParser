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

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class PivotVisitorAdapter<T> implements PivotVisitor<T> {

    @Override
    public T visit(Pivot pivot) {

        return null;
    }

    @Override
    public T visit(PivotXml pivot) {

        return null;
    }

    @Override
    public T visit(UnPivot unpivot) {

        return null;
    }
}
