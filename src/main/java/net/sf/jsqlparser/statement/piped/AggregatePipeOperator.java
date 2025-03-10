/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;

public class AggregatePipeOperator extends PipeOperator {
    private final ArrayList<SelectItem<?>> selectItems = new ArrayList<>();
    private final ArrayList<String> selectItemsOrderSuffices = new ArrayList<>();

    private final ArrayList<SelectItem<?>> groupItems = new ArrayList<>();
    private final ArrayList<String> groupItemsOrderSuffices = new ArrayList<>();
    private boolean usingShortHandOrdering = false;

    public AggregatePipeOperator(SelectItem<?> selectItem, String orderSuffix) {
        selectItems.add(selectItem);
        selectItemsOrderSuffices.add(orderSuffix);
    }

    public ArrayList<SelectItem<?>> getSelectItems() {
        return selectItems;
    }

    public ArrayList<SelectItem<?>> getGroupItems() {
        return groupItems;
    }

    public ArrayList<String> getSelectItemsOrderSuffices() {
        return selectItemsOrderSuffices;
    }

    public ArrayList<String> getGroupItemsOrderSuffices() {
        return groupItemsOrderSuffices;
    }

    public AggregatePipeOperator add(SelectItem<?> selectItem, String orderSuffix) {
        selectItems.add(selectItem);
        return this;
    }

    public AggregatePipeOperator with(SelectItem<?> selectItem, String orderSuffix) {
        return this.add(selectItem, orderSuffix);
    }

    public AggregatePipeOperator addGroupItem(SelectItem<?> selectItem, String orderSuffix) {
        groupItems.add(selectItem);
        groupItemsOrderSuffices.add(orderSuffix);
        return this;
    }

    public AggregatePipeOperator withGroupItem(SelectItem<?> selectItem, String orderSuffix) {
        return this.addGroupItem(selectItem, orderSuffix);
    }

    public boolean isUsingShortHandOrdering() {
        return usingShortHandOrdering;
    }

    public AggregatePipeOperator setShorthandOrdering(boolean usingShortHandOrdering) {
        this.usingShortHandOrdering = usingShortHandOrdering;
        return this;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append("AGGREGATE");
        int i = 0;
        for (SelectItem<?> selectItem : selectItems) {
            builder.append(i > 0 ? ", " : " ").append(selectItem);
            if (i < selectItemsOrderSuffices.size() && selectItemsOrderSuffices.get(i) != null
                    && !selectItemsOrderSuffices.get(i).isEmpty()) {
                builder.append(" ").append(selectItemsOrderSuffices.get(i));
            }
            i++;
        }
        builder.append("\n");

        if (!groupItems.isEmpty()) {
            builder.append("\t").append("GROUP");
            if (isUsingShortHandOrdering()) {
                builder.append(" AND ORDER");
            }
            builder.append(" BY");
            i = 0;
            for (SelectItem<?> selectItem : groupItems) {
                builder.append(i > 0 ? ", " : " ").append(selectItem);
                if (i < groupItemsOrderSuffices.size() && groupItemsOrderSuffices.get(i) != null
                        && !groupItemsOrderSuffices.get(i).isEmpty()) {
                    builder.append(" ").append(groupItemsOrderSuffices.get(i));
                }
                i++;
            }
            builder.append("\n");
        }

        return builder;
    }
}
