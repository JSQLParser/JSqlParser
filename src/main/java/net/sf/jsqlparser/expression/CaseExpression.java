/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.List;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * CASE/WHEN expression.
 *
 * Syntax:  <code><pre>
 * CASE
 * WHEN condition THEN expression
 * [WHEN condition THEN expression]...
 * [ELSE expression]
 * END
 * </pre></code>
 *
 * <br/>
 * or <br/>
 * <br/>
 *
 * <code><pre>
 * CASE expression
 * WHEN condition THEN expression
 * [WHEN condition THEN expression]...
 * [ELSE expression]
 * END
 * </pre></code>
 *
 * See also: https://aurora.vcu.edu/db2help/db2s0/frame3.htm#casexp
 * http://sybooks.sybase.com/onlinebooks/group-as/asg1251e /commands/
 *
 * @ebt-link;pt=5954?target=%25N%15_52628_START_RESTART_N%25
 *
 *
 * @author Havard Rast Blok
 */
public class CaseExpression extends ASTNodeAccessImpl implements Expression {

    private Expression switchExpression;
    private List<WhenClause> whenClauses;
    private Expression elseExpression;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    /**
     * @return Returns the switchExpression.
     */
    public Expression getSwitchExpression() {
        return switchExpression;
    }

    /**
     * @param switchExpression The switchExpression to set.
     */
    public void setSwitchExpression(Expression switchExpression) {
        this.switchExpression = switchExpression;
    }

    /**
     * @return Returns the elseExpression.
     */
    public Expression getElseExpression() {
        return elseExpression;
    }

    /**
     * @param elseExpression The elseExpression to set.
     */
    public void setElseExpression(Expression elseExpression) {
        this.elseExpression = elseExpression;
    }

    /**
     * @return Returns the whenClauses.
     */
    public List<WhenClause> getWhenClauses() {
        return whenClauses;
    }

    /**
     * @param whenClauses The whenClauses to set.
     */
    public void setWhenClauses(List<WhenClause> whenClauses) {
        this.whenClauses = whenClauses;
    }

    @Override
    public String toString() {
        return "CASE " + ((switchExpression != null) ? switchExpression + " " : "")
                + PlainSelect.getStringList(whenClauses, false, false) + " "
                + ((elseExpression != null) ? "ELSE " + elseExpression + " " : "") + "END";
    }
}
