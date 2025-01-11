package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;

public class AggregatePipeOperator extends PipeOperator {
    private final ArrayList<SelectItem<?>> selectItems = new ArrayList<>();
    private final ArrayList<SelectItem<?>> groupItems = new ArrayList<>();
    private boolean usingShortHandOrdering = false;

    public AggregatePipeOperator(SelectItem<?> selectItem) {
        selectItems.add(selectItem);
    }

    public ArrayList<SelectItem<?>> getSelectItems() {
        return selectItems;
    }

    public ArrayList<SelectItem<?>> getGroupItems() {
        return groupItems;
    }

    public AggregatePipeOperator add(SelectItem<?> selectItem) {
        selectItems.add(selectItem);
        return this;
    }

    public AggregatePipeOperator with(SelectItem<?> selectItem) {
        return this.add(selectItem);
    }

    public AggregatePipeOperator addGroupItem(SelectItem<?> selectItem) {
        groupItems.add(selectItem);
        return this;
    }

    public AggregatePipeOperator withGroupItem(SelectItem<?> selectItem) {
        return this.addGroupItem(selectItem);
    }

    public boolean isUsingShortHandOrdering() {
        return usingShortHandOrdering;
    }

    public AggregatePipeOperator setShorthandOrdering(boolean usingShortHandOrdering) {
        this.usingShortHandOrdering = usingShortHandOrdering;
        return this;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append("AGGREGATE");
        int i = 0;
        for (SelectItem<?> selectItem : selectItems) {
            builder.append(i++ > 0 ? ", " : " ").append(selectItem);
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
                builder.append(i++ > 0 ? ", " : " ").append(selectItem);
            }
            builder.append("\n");
        }

        return builder;
    }
}
