package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;

public class AsPipeOperator extends PipeOperator {
    private Alias alias;

    public AsPipeOperator(Alias alias) {
        this.alias = alias;
    }

    public Alias getAlias() {
        return alias;
    }

    public AsPipeOperator setAlias(Alias alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append(alias);
        builder.append("\n");
        return builder;
    }
}
