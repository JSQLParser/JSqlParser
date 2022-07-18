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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Select implements Statement {

    private SelectBody selectBody;
    private List<WithItem> withItemsList;

    private boolean useWithBrackets = false;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public Select withSelectBody(SelectBody body) {
        setSelectBody(body);
        return this;
    }

    public void setSelectBody(SelectBody body) {
        selectBody = body;
    }

    public void setUsingWithBrackets(boolean useWithBrackets) {
        this.useWithBrackets = useWithBrackets;
    }

    public Select withUsingWithBrackets(boolean useWithBrackets) {
        this.useWithBrackets = useWithBrackets;
        return this;
    }

    public boolean isUsingWithBrackets() {
        return this.useWithBrackets;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder retval = new StringBuilder();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            if (useWithBrackets) {
                retval.append("( ");
            }
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
        if (withItemsList != null && !withItemsList.isEmpty() && useWithBrackets) {
            retval.append(" )");
        }
        return retval.toString();
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public Select withWithItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public <E extends SelectBody> E getSelectBody(Class<E> type) {
        return type.cast(getSelectBody());
    }

    public Select addWithItemsList(WithItem... withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemsList);
        return this.withWithItemsList(collection);
    }

    public Select addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }
}
