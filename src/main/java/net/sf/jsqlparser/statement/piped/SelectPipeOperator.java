package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;

public class SelectPipeOperator extends PipeOperator {
    private final String operatorName;

    private final ArrayList<SelectItem<?>> selectItems = new ArrayList<>();

    public SelectPipeOperator(String operatorName, SelectItem<?> selectItem) {
        this.operatorName = operatorName;
        selectItems.add(selectItem);
    }

    public SelectPipeOperator(SelectItem<?> selectItem) {
        this("SELECT", selectItem);
    }

    public String getOperatorName() {
        return operatorName;
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
    public <T, S> T accept(PipeOperatorVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append(operatorName);
        int i = 0;
        for (SelectItem<?> selectItem : selectItems) {
            builder.append(i++ > 0 ? ", " : " ").append(selectItem);
        }
        builder.append("\n");
        return builder;
    }
}
