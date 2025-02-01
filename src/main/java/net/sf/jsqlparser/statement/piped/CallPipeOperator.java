package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.TableFunction;

public class CallPipeOperator extends PipeOperator {
    private TableFunction tableFunction;
    private Alias alias;

    public CallPipeOperator(TableFunction tableFunction, Alias alias) {
        this.tableFunction = tableFunction;
        this.alias = alias;
    }

    public TableFunction getTableFunction() {
        return tableFunction;
    }

    public CallPipeOperator setTableFunction(TableFunction tableFunction) {
        this.tableFunction = tableFunction;
        return this;
    }

    public Alias getAlias() {
        return alias;
    }

    public CallPipeOperator setAlias(Alias alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> CALL ").append(tableFunction);
        if (alias != null) {
            builder.append(" ").append(alias);
        }

        return builder;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
