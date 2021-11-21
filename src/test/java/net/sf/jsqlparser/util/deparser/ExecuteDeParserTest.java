/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.execute.Execute.ExecType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;

public class ExecuteDeParserTest {

    private ExecuteDeParser executeDeParser;
    private ExpressionDeParser expressionVisitor;

    private StringBuilder buffer;

    @BeforeAll
    public void setUp() {
        buffer = new StringBuilder();
        expressionVisitor = new ExpressionDeParser();
        expressionVisitor.setBuffer(buffer);
        executeDeParser = new ExecuteDeParser(expressionVisitor, buffer);
    }

    @Test
    public void shouldDeParseExecute() {
        Execute execute = new Execute();
        String name = "name";

        List<Expression> expressions = new ArrayList<>();
        expressions.add(new JdbcParameter());
        expressions.add(new JdbcParameter());

        execute.withName(name)
                .withExecType(ExecType.EXECUTE).withParenthesis(true)
                .withExprList(new ExpressionList().withExpressions(expressions));

        executeDeParser.deParse(execute);

        String actual = buffer.toString();
        assertEquals("EXECUTE " + name + " (?, ?)", actual);
    }

    @Test
    public void shouldUseProvidedExpressionVisitorWhenDeParsingExecute() {
        Execute execute = new Execute();
        String name = "name";

        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);

        List<Expression> expressions = new ArrayList<>();
        expressions.add(expression1);
        expressions.add(expression2);

        ExpressionList exprList = new ExpressionList().addExpressions(expressions);
        execute.withName(name).withExprList(exprList);

        executeDeParser.deParse(execute);

        then(expression1).should().accept(expressionVisitor);
        then(expression2).should().accept(expressionVisitor);
    }
}
