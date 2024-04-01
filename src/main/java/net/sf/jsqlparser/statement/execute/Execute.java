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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.util.SelectUtils;

import java.util.List;

public class Execute implements Statement {

    private ExecType execType = ExecType.EXECUTE;
    private String name;
    private ExpressionList exprList;

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

    public ExecType getExecType() {
        return execType;
    }

    public void setExecType(ExecType execType) {
        this.execType = execType;
    }

    @Deprecated
    public boolean isParenthesis() {
        return exprList instanceof ParenthesedExpressionList;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return execType.name() + " " + name
                + (exprList != null
                        ? " " + SelectUtils.getStringList(exprList, true,
                                exprList instanceof ParenthesedExpressionList)
                        : "");
    }

    public Execute withExecType(ExecType execType) {
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

    public enum ExecType {
        EXECUTE, EXEC, CALL;

        public static ExecType from(String type) {
            return Enum.valueOf(ExecType.class, type.toUpperCase());
        }
    }
}
