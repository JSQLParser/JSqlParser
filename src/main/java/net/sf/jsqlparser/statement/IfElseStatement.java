/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.statement;

import java.util.Objects;
import java.util.function.Consumer;

import net.sf.jsqlparser.expression.Expression;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class IfElseStatement implements Statement {
    private final Expression condition;
    private final Statement ifStatement;
    private Statement elseStatement;
    private boolean usingSemicolonForIfStatement = false;
    private boolean usingSemicolonForElseStatement = false;

    public IfElseStatement(Expression condition, Statement ifStatement) {
        this.condition =
                Objects.requireNonNull(condition,
                        "The CONDITION of the IfElseStatement must not be null.");
        this.ifStatement = Objects.requireNonNull(ifStatement,
                "The IF Statement of the IfElseStatement must not be null.");
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getIfStatement() {
        return ifStatement;
    }

    public Statement getElseStatement() {
        return elseStatement;
    }

    public void setElseStatement(Statement elseStatement) {
        this.elseStatement = elseStatement;
    }

    public boolean isUsingSemicolonForElseStatement() {
        return usingSemicolonForElseStatement;
    }

    public void setUsingSemicolonForElseStatement(boolean usingSemicolonForElseStatement) {
        this.usingSemicolonForElseStatement = usingSemicolonForElseStatement;
    }

    public boolean isUsingSemicolonForIfStatement() {
        return usingSemicolonForIfStatement;
    }

    public void setUsingSemicolonForIfStatement(boolean usingSemicolonForIfStatement) {
        this.usingSemicolonForIfStatement = usingSemicolonForIfStatement;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append, builder::append);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter,
            Consumer<Statement> statementPrinter) {
        builder.append("IF ");
        expressionPrinter.accept(condition);
        builder.append(' ');
        statementPrinter.accept(ifStatement);
        builder.append(usingSemicolonForIfStatement ? ";" : "");

        if (elseStatement != null) {
            builder.append(" ELSE ");
            statementPrinter.accept(elseStatement);
            builder.append(usingSemicolonForElseStatement ? ";" : "");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

}
