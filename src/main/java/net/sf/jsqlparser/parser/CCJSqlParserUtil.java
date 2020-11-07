/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

/**
 * Toolfunctions to start and use JSqlParser.
 *
 * @author toben
 */
public final class CCJSqlParserUtil {

    private CCJSqlParserUtil() {
    }

    public static Statement parse(Reader statementReader) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StreamProvider(statementReader));
        return parseStatement(parser);
    }

    public static Statement parse(String sql) throws JSQLParserException {
        return parse(sql, null);
    }

    /**
     * Parses an sql statement while allowing via consumer to configure the used parser before.
     *
     * For instance to activate SQLServer bracket quotation on could use:
     *
     * {@code
     * CCJSqlParserUtil.parse("select * from [mytable]", parser -> parser.withSquareBracketQuotation(true));
     * }
     *
     * @param sql
     * @param consumer
     * @return
     * @throws JSQLParserException
     */
    public static Statement parse(String sql, Consumer<CCJSqlParser> consumer) throws JSQLParserException {
        CCJSqlParser parser = newParser(sql);
        if (consumer != null) {
            consumer.accept(parser);
        }
        return parseStatement(parser);
    }

    public static CCJSqlParser newParser(String sql) {
        return new CCJSqlParser(new StringProvider(sql));
    }

    public static CCJSqlParser newParser(InputStream is) throws IOException {
        return new CCJSqlParser(new StreamProvider(is));
    }

    public static CCJSqlParser newParser(InputStream is, String encoding) throws IOException {
        return new CCJSqlParser(new StreamProvider(is, encoding));
    }

    public static Node parseAST(String sql) throws JSQLParserException {
        CCJSqlParser parser = newParser(sql);
        try {
            parser.Statement();
            return parser.jjtree.rootNode();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(InputStream is) throws JSQLParserException {
        try {
            CCJSqlParser parser = newParser(is);
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(InputStream is, String encoding) throws JSQLParserException {
        try {
            CCJSqlParser parser = newParser(is, encoding);
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Expression parseExpression(String expression) throws JSQLParserException {
        return parseExpression(expression, true);
    }

    public static Expression parseExpression(String expression, boolean allowPartialParse) throws JSQLParserException {
        CCJSqlParser parser = newParser(expression);
        try {
            Expression expr = parser.SimpleExpression();
            if (!allowPartialParse && parser.getNextToken().kind != CCJSqlParserTokenManager.EOF) {
                throw new JSQLParserException("could only parse partial expression " + expr.toString());
            }
            return expr;
        } catch (JSQLParserException ex) {
            throw ex;
        } catch (ParseException ex) {
            throw new JSQLParserException(ex);
        }
    }

    /**
     * Parse an conditional expression. This is the expression after a where clause.
     * Partial parsing is enabled.
     *
     * @param condExpr
     * @return the expression parsed
     * @see #parseCondExpression(String, boolean)
     */
    public static Expression parseCondExpression(String condExpr) throws JSQLParserException {
        return parseCondExpression(condExpr, true);
    }

    /**
     * Parse an conditional expression. This is the expression after a where clause.
     *
     * @param condExpr
     * @param allowPartialParse false: needs the whole string to be processed.
     * @return the expression parsed
     * @see #parseCondExpression(String)
     */
    public static Expression parseCondExpression(String condExpr, boolean allowPartialParse) throws JSQLParserException {
        CCJSqlParser parser = newParser(condExpr);
        try {
            Expression expr = parser.Expression();
            if (!allowPartialParse && parser.getNextToken().kind != CCJSqlParserTokenManager.EOF) {
                throw new JSQLParserException("could only parse partial expression " + expr.toString());
            }
            return expr;
        } catch (JSQLParserException ex) {
            throw ex;
        } catch (ParseException ex) {
            throw new JSQLParserException(ex);
        }
    }

    /**
     * @param parser
     * @return the statement parsed
     * @throws JSQLParserException
     */
    public static Statement parseStatement(CCJSqlParser parser) throws JSQLParserException {
        try {
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    /**
     * Parse a statement list.
     *
     * @return the statements parsed
     */
    public static Statements parseStatements(String sqls) throws JSQLParserException {
        CCJSqlParser parser = newParser(sqls);
        return parseStatements(parser);
    }

    /**
     * @param parser
     * @return the statements parsed
     * @throws JSQLParserException
     */
    public static Statements parseStatements(CCJSqlParser parser) throws JSQLParserException {
        try {
            return parser.Statements();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static void streamStatements(StatementListener listener, InputStream is, String encoding) throws JSQLParserException {
        try {
            CCJSqlParser parser = newParser(is, encoding);
            while (true) {
                Statement stmt = parser.SingleStatement();
                listener.accept(stmt);
                if (parser.getToken(1).kind == CCJSqlParserTokenManager.ST_SEMICOLON) {
                    parser.getNextToken();
                }

                if (parser.getToken(1).kind == CCJSqlParserTokenManager.EOF) {
                    break;
                }
            }
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

}
