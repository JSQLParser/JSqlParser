/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
 */
public class CaseExpression extends ASTNodeAccessImpl implements Expression {

    private Expression switchExpression;
    private List<WhenClause> whenClauses;
    private Expression elseExpression;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Expression getSwitchExpression() {
        return switchExpression;
    }

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

    public CaseExpression switchExpression(Expression switchExpression) {
        this.setSwitchExpression(switchExpression);
        return this;
    }

    public CaseExpression whenClauses(List<WhenClause> whenClauses) {
        this.setWhenClauses(whenClauses);
        return this;
    }

    public CaseExpression elseExpression(Expression elseExpression) {
        this.setElseExpression(elseExpression);
        return this;
    }

    public CaseExpression addWhenClauses(WhenClause... whenClauses) {
        List<WhenClause> collection = Optional.ofNullable(getWhenClauses()).orElseGet(ArrayList::new);
        Collections.addAll(collection, whenClauses);
        return this.whenClauses(collection);
    }

    public CaseExpression addWhenClauses(Collection<? extends WhenClause> whenClauses) {
        List<WhenClause> collection = Optional.ofNullable(getWhenClauses()).orElseGet(ArrayList::new);
        collection.addAll(whenClauses);
        return this.whenClauses(collection);
    }

    public <E extends Expression> E getSwitchExpression(Class<? extends E> type) {
        return type.cast(getSwitchExpression());
    }

    public <E extends Expression> E getElseExpression(Class<? extends E> type) {
        return type.cast(getElseExpression());
    }
}
