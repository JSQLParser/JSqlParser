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
        this.condition = Objects.requireNonNull(condition, "The CONDITION of the IfElseStatement must not be null.");
        this.ifStatement = Objects.requireNonNull(ifStatement, "The IF Statement of the IfElseStatement must not be null.");
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getIfStatement() {
        return ifStatement;
    }

    public void setElseStatement(Statement elseStatement) {
        this.elseStatement = elseStatement;
    }

    public Statement getElseStatement() {
        return elseStatement;
    }

    public void setUsingSemicolonForElseStatement(boolean usingSemicolonForElseStatement) {
        this.usingSemicolonForElseStatement = usingSemicolonForElseStatement;
    }

    public boolean isUsingSemicolonForElseStatement() {
        return usingSemicolonForElseStatement;
    }

    public void setUsingSemicolonForIfStatement(boolean usingSemicolonForIfStatement) {
        this.usingSemicolonForIfStatement = usingSemicolonForIfStatement;
    }

    public boolean isUsingSemicolonForIfStatement() {
        return usingSemicolonForIfStatement;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("IF ").append(condition).append(" ").append(ifStatement).append(usingSemicolonForIfStatement ? ";" : "");
        if (elseStatement != null) {
            builder.append(" ELSE ").append(elseStatement).append(usingSemicolonForElseStatement ? ";" : "");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }
}
