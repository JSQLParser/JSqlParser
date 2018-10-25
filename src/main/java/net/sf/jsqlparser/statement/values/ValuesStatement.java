/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
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
package net.sf.jsqlparser.statement.values;

import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitor;

/**
 * The replace statement.
 */
public class ValuesStatement implements Statement, SelectBody {
    
    private List<Expression> expressions;
    
    public ValuesStatement(List<Expression> expressions) {
        this.expressions = expressions;
    }
    
    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }
    
    public List<Expression> getExpressions() {
        return expressions;
    }
    
    public void setExpressions(List<Expression> list) {
        expressions = list;
    }
    
    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("VALUES ");
        sql.append(PlainSelect.getStringList(expressions, true, true));
        return sql.toString();
    }
    
    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }
}
