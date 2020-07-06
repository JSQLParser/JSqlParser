/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.execute;

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Execute implements Statement {

    private EXEC_TYPE execType = EXEC_TYPE.EXECUTE;
    private String name;
    private ExpressionList exprList;
    private boolean parenthesis = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setName(List<String> names) {
        for (String item : names) {
            if (this.name != null) {
                this.name = this.name + "." + item;
            } else {
                this.name = item;
            }
        }
    }

    public ExpressionList getExprList() {
        return exprList;
    }

    public void setExprList(ExpressionList exprList) {
        this.exprList = exprList;
    }

    public EXEC_TYPE getExecType() {
        return execType;
    }

    public void setExecType(EXEC_TYPE execType) {
        this.execType = execType;
    }

    public boolean isParenthesis() {
        return parenthesis;
    }

    public void setParenthesis(boolean parenthesis) {
        this.parenthesis = parenthesis;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return execType.name() + " " + name
                + (exprList != null && exprList.getExpressions() != null ? " "
                + PlainSelect.getStringList(exprList.getExpressions(), true, parenthesis) : "");
    }

    public Execute withExecType(EXEC_TYPE execType) {
        this.setExecType(execType);
        return this;
    }

    public Execute withName(String name) {
        this.setName(name);
        return this;
    }

    public Execute withExprList(ExpressionList exprList) {
        this.setExprList(exprList);
        return this;
    }

    public Execute withParenthesis(boolean parenthesis) {
        this.setParenthesis(parenthesis);
        return this;
    }

    public static enum EXEC_TYPE {
        EXECUTE,
        EXEC,
        CALL
    }
}
