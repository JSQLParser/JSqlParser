package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public abstract class PipeOperator extends ASTNodeAccessImpl {
    public abstract <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context);
}
