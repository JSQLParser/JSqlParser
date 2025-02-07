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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.execute.Execute.ExecType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

public class ExecuteDeParserTest {

    private ExecuteDeParser executeDeParser;
    private ExpressionDeParser expressionVisitor;

    private StringBuilder buffer;

    @BeforeEach
    public void setUp() {
        buffer = new StringBuilder();
        expressionVisitor = new ExpressionDeParser();
        expressionVisitor.setBuilder(buffer);
        executeDeParser = new ExecuteDeParser(expressionVisitor, buffer);
    }

    @Test
    public void shouldDeParseExecute() {
        Execute execute = new Execute();
        String name = "name";

        ParenthesedExpressionList<Expression> expressions = new ParenthesedExpressionList<>();
        expressions.add(new JdbcParameter());
        expressions.add(new JdbcParameter());

        execute.withName(name)
                .withExecType(ExecType.EXECUTE)
                .withExprList(expressions);

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

        ExpressionList<?> exprList = new ExpressionList<>().addExpressions(expressions);
        execute.withName(name).withExprList(exprList);

        executeDeParser.deParse(execute);

        then(expression1).should().accept(expressionVisitor, null);
        then(expression2).should().accept(expressionVisitor, null);
    }
}
