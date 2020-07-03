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

public class WithItem implements SelectBody {

    private String name;
    private List<SelectItem> withItemList;
    private SelectBody selectBody;
    private boolean recursive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody selectBody) {
        this.selectBody = selectBody;
    }

    /**
     * The {@link SelectItem}s in this WITH (for example the A,B,C in "WITH mywith (A,B,C) AS ...")
     *
     * @return a list of {@link SelectItem}s
     */
    public List<SelectItem> getWithItemList() {
        return withItemList;
    }

    public void setWithItemList(List<SelectItem> withItemList) {
        this.withItemList = withItemList;
    }

    @Override
    public String toString() {
        return (recursive ? "RECURSIVE " : "") + name + ((withItemList != null) ? " " + PlainSelect.
                getStringList(withItemList, true, true) : "")
                + " AS (" + selectBody + ")";
    }

    @Override
    public void accept(SelectVisitor visitor) {
        visitor.visit(this);
    }

    public WithItem name(String name) {
        this.setName(name);
        return this;
    }

    public WithItem withItemList(List<SelectItem> withItemList) {
        this.setWithItemList(withItemList);
        return this;
    }

    public WithItem selectBody(SelectBody selectBody) {
        this.setSelectBody(selectBody);
        return this;
    }

    public WithItem recursive(boolean recursive) {
        this.setRecursive(recursive);
        return this;
    }

    public WithItem addWithItemList(SelectItem... withItemList) {
        List<SelectItem> collection = Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemList);
        return this.withItemList(collection);
    }

    public WithItem addWithItemList(Collection<? extends SelectItem> withItemList) {
        List<SelectItem> collection = Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
        collection.addAll(withItemList);
        return this.withItemList(collection);
    }

    public <E extends SelectBody> E getSelectBody(Class<? extends E> type) {
        return type.cast(getSelectBody());
    }
}
