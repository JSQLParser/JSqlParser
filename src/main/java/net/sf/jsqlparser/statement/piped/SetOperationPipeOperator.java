package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.SetOperationList;

import java.util.ArrayList;

public class SetOperationPipeOperator extends PipeOperator {
    private ArrayList<ParenthesedSelect> selects;
    private SetOperationList.SetOperationType setOperationType;
    private String modifier;

    public SetOperationPipeOperator(ParenthesedSelect select,
            SetOperationList.SetOperationType setOperationType, String modifier) {
        this.selects = new ArrayList<>();
        this.selects.add(select);

        this.setOperationType = setOperationType;
        this.modifier = modifier;
    }

    public SetOperationPipeOperator add(ParenthesedSelect select) {
        this.selects.add(select);
        return this;
    }

    public ArrayList<ParenthesedSelect> getSelects() {
        return selects;
    }

    public SetOperationPipeOperator setSelects(ArrayList<ParenthesedSelect> selects) {
        this.selects = selects;
        return this;
    }

    public SetOperationList.SetOperationType getSetOperationType() {
        return setOperationType;
    }

    public SetOperationPipeOperator setSetOperationType(
            SetOperationList.SetOperationType setOperationType) {
        this.setOperationType = setOperationType;
        return this;
    }

    public String getModifier() {
        return modifier;
    }

    public SetOperationPipeOperator setModifier(String modifier) {
        this.modifier = modifier;
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append(setOperationType);
        if (modifier != null) {
            builder.append(" ").append(modifier);
        }

        int i = 0;
        for (ParenthesedSelect select : selects) {
            if (i++ > 0) {
                builder.append(", ");
            }
            builder.append(select);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }
}
