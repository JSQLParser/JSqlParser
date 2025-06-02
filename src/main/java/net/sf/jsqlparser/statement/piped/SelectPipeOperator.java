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

public class SelectPipeOperator extends PipeOperator {
    private final String operatorName;
    private final String modifier;

    private final ArrayList<SelectItem<?>> selectItems = new ArrayList<>();

    public SelectPipeOperator(String operatorName, SelectItem<?> selectItem, String modifier) {
        this.operatorName = operatorName;
        this.modifier = modifier;
        selectItems.add(selectItem);
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getModifier() {
        return modifier;
    }

    public ArrayList<SelectItem<?>> getSelectItems() {
        return selectItems;
    }

    public SelectPipeOperator add(SelectItem<?> selectItem) {
        selectItems.add(selectItem);
        return this;
    }

    public SelectPipeOperator with(SelectItem<?> selectItem) {
        return this.add(selectItem);
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append(operatorName);
        if (modifier != null && !modifier.isEmpty()) {
            builder.append(" ").append(modifier);
        }

        int i = 0;
        for (SelectItem<?> selectItem : selectItems) {
            builder.append(i++ > 0 ? ", " : " ").append(selectItem);
        }
        builder.append("\n");
        return builder;
    }
}
