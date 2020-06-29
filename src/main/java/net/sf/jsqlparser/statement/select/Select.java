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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Select implements Statement {

    private SelectBody selectBody;
    private List<WithItem> withItemsList;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public Select selectBody(SelectBody body) {
        setSelectBody(body);
        return this;
    }

    public void setSelectBody(SelectBody body) {
        selectBody = body;
    }

    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            retval.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                retval.append(withItem);
                if (iter.hasNext()) {
                    retval.append(",");
                }
                retval.append(" ");
            }
        }
        retval.append(selectBody);
        return retval.toString();
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public Select withItemsList(List<WithItem> withItemsList) {
        setWithItemsList(withItemsList);
        return this;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public static Select create() {
        return new Select();
    }

    public <T extends SelectBody> T getSelectBody(Class<T> type) {
        return Optional.ofNullable(selectBody).map(type::cast).orElseGet(null);
    }

}
