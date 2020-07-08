/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.values;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitor;

public class ValuesStatement implements Statement, SelectBody {
    
    private List<Expression> expressions;

    public ValuesStatement() {
        // empty constructor
    }

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
    
    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
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

    public ValuesStatement withExpressions(List<Expression> expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public ValuesStatement addExpressions(Expression... expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, expressions);
        return this.withExpressions(collection);
    }

    public ValuesStatement addExpressions(Collection<? extends Expression> expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        collection.addAll(expressions);
        return this.withExpressions(collection);
    }
}
