package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.Join;

public class JoinPipeOperator extends PipeOperator {
    private Join join;

    public JoinPipeOperator(Join join) {
        this.join = join;
    }

    public Join getJoin() {
        return join;
    }

    public JoinPipeOperator setJoin(Join join) {
        this.join = join;
        return this;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append(join);
        builder.append("\n");
        return builder;
    }


}
