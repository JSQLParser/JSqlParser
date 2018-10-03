/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.statement.execute;

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 *
 * @author toben
 */
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

    public static enum EXEC_TYPE {
        EXECUTE,
        EXEC,
        CALL
    }

}
