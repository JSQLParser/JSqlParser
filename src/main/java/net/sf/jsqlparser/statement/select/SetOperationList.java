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

public class SetOperationList implements SelectBody {

    private List<SelectBody> selects;
    private List<SetOperation> operations;
    private List<OrderByElement> orderByElements;
    private Limit limit;
    private Offset offset;
    private Fetch fetch;
    private WithIsolation withIsolation;

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

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    public WithIsolation getWithIsolation() {
        return this.withIsolation;
    }

    public void setWithIsolation(WithIsolation withIsolation) {
        this.withIsolation = withIsolation;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < selects.size(); i++) {
            if (i != 0) {
                buffer.append(" ").append(operations.get(i - 1).toString()).append(" ");
            }
            buffer.append(selects.get(i).toString());
        }

        if (orderByElements != null) {
            buffer.append(PlainSelect.orderByToString(orderByElements));
        }
        if (limit != null) {
            buffer.append(limit);
        }
        if (offset != null) {
            buffer.append(offset);
        }
        if (fetch != null) {
            buffer.append(fetch);
        }
        if (withIsolation != null) {
            buffer.append(withIsolation);
        }
        return buffer.toString();
    }

    public SetOperationList withOperations(List<SetOperation> operationList) {
        setOperations(operationList);
        return this;
    }

    public SetOperationList withSelects(List<SelectBody> selects) {
        setSelects(selects);
        return this;
    }

    public SetOperationList withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public SetOperationList withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public SetOperationList withOffset(Offset offset) {
        this.setOffset(offset);
        return this;
    }

    public SetOperationList withFetch(Fetch fetch) {
        this.setFetch(fetch);
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
        List<SetOperation> collection = Optional.ofNullable(getOperations()).orElseGet(ArrayList::new);
        Collections.addAll(collection, operationList);
        return this.withOperations(collection);
    }

    public SetOperationList addOperations(Collection<? extends SetOperation> operationList) {
        List<SetOperation> collection = Optional.ofNullable(getOperations()).orElseGet(ArrayList::new);
        collection.addAll(operationList);
        return this.withOperations(collection);
    }

    public SetOperationList addOrderByElements(OrderByElement... orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, orderByElements);
        return this.withOrderByElements(collection);
    }

    public SetOperationList addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }

    public enum SetOperationType {

        INTERSECT,
        EXCEPT,
        MINUS,
        UNION
    }
}
