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

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Select implements Statement {

    private SelectBody selectBody;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public Select withSelectBody(SelectBody body) {
        this.setSelectBody(body);
        return this;
    }

    public void setSelectBody(SelectBody body) {
        this.selectBody = body;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(selectBody);
        return builder.toString();
    }


    public <E extends SelectBody> E getSelectBody(Class<E> type) {
        return type.cast(getSelectBody());
    }
}
