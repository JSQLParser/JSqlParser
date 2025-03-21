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
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append(join);
        builder.append("\n");
        return builder;
    }


}
