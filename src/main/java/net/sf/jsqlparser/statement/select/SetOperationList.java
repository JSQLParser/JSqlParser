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
import java.util.List;
import java.util.Optional;

public class SetOperationList extends SelectBody {

    private List<SelectBody> selects;
    private List<SetOperation> operations;
    private List<OrderByElement> orderByElements;

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public List<SelectBody> getSelects() {
        return selects;
    }

    public void setSelects(List<SelectBody> selects) {
        this.selects = selects;
    }

    public void setOperations(List<SetOperation> operations) {
        this.operations = operations;
    }

    public List<SetOperation> getOperations() {
        return operations;
    }



    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public void setBracketsOpsAndSelects(List<SelectBody> select, List<SetOperation> ops) {
        selects = select;
        operations = ops;
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        for (int i = 0; i < selects.size(); i++) {
            if (i != 0) {
                builder.append(" ").append(operations.get(i - 1).toString()).append(" ");
            }
            builder.append(selects.get(i).toString());
        }

        if (orderByElements != null) {
            builder.append(PlainSelect.orderByToString(orderByElements));
        }
        return builder;
    }

    public SetOperationList withOperations(List<SetOperation> operationList) {
        setOperations(operationList);
        return this;
    }

    public SetOperationList withSelects(List<SelectBody> selects) {
        setSelects(selects);
        return this;
    }

    public SetOperationList addSelects(SelectBody... selects) {
        List<SelectBody> collection = Optional.ofNullable(getSelects()).orElseGet(ArrayList::new);
        Collections.addAll(collection, selects);
        return this.withSelects(collection);
    }

    public SetOperationList addSelects(Collection<? extends SelectBody> selects) {
        List<SelectBody> collection = Optional.ofNullable(getSelects()).orElseGet(ArrayList::new);
        collection.addAll(selects);
        return this.withSelects(collection);
    }

    public SetOperationList addOperations(SetOperation... operationList) {
        List<SetOperation> collection =
                Optional.ofNullable(getOperations()).orElseGet(ArrayList::new);
        Collections.addAll(collection, operationList);
        return this.withOperations(collection);
    }

    public SetOperationList addOperations(Collection<? extends SetOperation> operationList) {
        List<SetOperation> collection =
                Optional.ofNullable(getOperations()).orElseGet(ArrayList::new);
        collection.addAll(operationList);
        return this.withOperations(collection);
    }

    public enum SetOperationType {
        INTERSECT, EXCEPT, MINUS, UNION
    }
}
