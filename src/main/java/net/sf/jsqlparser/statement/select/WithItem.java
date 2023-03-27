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

public class WithItem extends ParenthesedSelect {

    private List<SelectItem> withItemList;

    private boolean recursive = false;

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
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
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append(recursive ? "RECURSIVE " : "");
        builder.append(alias.getName());
        builder.append(
                (withItemList != null) ? " " + PlainSelect.getStringList(withItemList, true, true)
                        : "");
        builder.append(" AS ");

        select.appendTo(builder);

        return builder;
    }

    @Override
    public void accept(SelectVisitor visitor) {
        visitor.visit(this);
    }


    public WithItem withWithItemList(List<SelectItem> withItemList) {
        this.setWithItemList(withItemList);
        return this;
    }

    public WithItem withRecursive(boolean recursive) {
        this.setRecursive(recursive);
        return this;
    }

    public WithItem addWithItemList(SelectItem... withItemList) {
        List<SelectItem> collection =
                Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemList);
        return this.withWithItemList(collection);
    }

    public WithItem addWithItemList(Collection<? extends SelectItem> withItemList) {
        List<SelectItem> collection =
                Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
        collection.addAll(withItemList);
        return this.withWithItemList(collection);
    }
}
