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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.util.SelectUtils;

/**
 * CASE/WHEN expression.
 *
 * Syntax:
 * 
 * <pre>
 * <code>
 * CASE
 * WHEN condition THEN expression
 * [WHEN condition THEN expression]...
 * [ELSE expression]
 * END
 * </code>
 * </pre>
 *
 * <br>
 * or <br>
 * <br>
 *
 * <pre>
 * <code>
 * CASE expression
 * WHEN condition THEN expression
 * [WHEN condition THEN expression]...
 * [ELSE expression]
 * END
 * </code>
 * </pre>
 *
 */
public class CaseExpression extends ASTNodeAccessImpl implements Expression {

    private boolean usingBrackets = false;
    private Expression switchExpression;
    private List<WhenClause> whenClauses;
    private Expression elseExpression;

    public CaseExpression() {}

    public CaseExpression(WhenClause... whenClauses) {
        this.whenClauses = Arrays.asList(whenClauses);
    }

    public CaseExpression(Expression elseExpression, WhenClause... whenClauses) {
        this.elseExpression = elseExpression;
        this.whenClauses = Arrays.asList(whenClauses);
    }


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
        return (usingBrackets ? "(" : "") + "CASE "
                + ((switchExpression != null) ? switchExpression + " " : "")
                + SelectUtils.getStringList(whenClauses, false, false) + " "
                + ((elseExpression != null) ? "ELSE " + elseExpression + " " : "") + "END"
                + (usingBrackets ? ")" : "");
    }

    public CaseExpression withSwitchExpression(Expression switchExpression) {
        this.setSwitchExpression(switchExpression);
        return this;
    }

    public CaseExpression withWhenClauses(WhenClause... whenClauses) {
        return this.withWhenClauses(Arrays.asList(whenClauses));
    }

    public CaseExpression withWhenClauses(List<WhenClause> whenClauses) {
        this.setWhenClauses(whenClauses);
        return this;
    }

    public CaseExpression withElseExpression(Expression elseExpression) {
        this.setElseExpression(elseExpression);
        return this;
    }

    public CaseExpression addWhenClauses(WhenClause... whenClauses) {
        List<WhenClause> collection =
                Optional.ofNullable(getWhenClauses()).orElseGet(ArrayList::new);
        Collections.addAll(collection, whenClauses);
        return this.withWhenClauses(collection);
    }

    public CaseExpression addWhenClauses(Collection<? extends WhenClause> whenClauses) {
        List<WhenClause> collection =
                Optional.ofNullable(getWhenClauses()).orElseGet(ArrayList::new);
        collection.addAll(whenClauses);
        return this.withWhenClauses(collection);
    }

    public <E extends Expression> E getSwitchExpression(Class<E> type) {
        return type.cast(getSwitchExpression());
    }

    public <E extends Expression> E getElseExpression(Class<E> type) {
        return type.cast(getElseExpression());
    }

    /**
     * @return the usingBrackets
     */
    public boolean isUsingBrackets() {
        return usingBrackets;
    }

    /**
     * @param usingBrackets the usingBrackets to set
     */
    public void setUsingBrackets(boolean usingBrackets) {
        this.usingBrackets = usingBrackets;
    }

    /**
     * @param usingBrackets the usingBrackets to set
     */
    public CaseExpression withUsingBrackets(boolean usingBrackets) {
        this.usingBrackets = usingBrackets;
        return this;
    }
}
