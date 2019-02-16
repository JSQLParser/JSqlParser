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

import java.io.InputStream;
import java.io.Reader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

public final class CCJSqlParserUtil {

    private CCJSqlParserUtil() {
    }

    public static Statement parse(Reader statementReader) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StreamProvider(statementReader));
        try {
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(String sql) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sql));
        try {
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Node parseAST(String sql) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sql));
        try {
            parser.Statement();
            return parser.jjtree.rootNode();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(InputStream is) throws JSQLParserException {
        try {
            CCJSqlParser parser = new CCJSqlParser(new StreamProvider(is));
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(InputStream is, String encoding) throws JSQLParserException {
        try {
            CCJSqlParser parser = new CCJSqlParser(new StreamProvider(is, encoding));
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Expression parseExpression(String expression) throws JSQLParserException {
        return parseExpression(expression, true);
    }
    
    public static Expression parseExpression(String expression, boolean allowPartialParse) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(expression));
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

    public static Expression parseCondExpression(String condExpr) throws JSQLParserException {
        return parseCondExpression(condExpr, true);
    }

    /**
     * Parse an conditional expression. This is the expression after a where clause.
     *
     * @param condExpr
     * @param allowPartialParse false: needs the whole string to be processed.
     * @return
     */
    public static Expression parseCondExpression(String condExpr, boolean allowPartialParse) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(condExpr));
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
     * Parse a statement list.
     */
    public static Statements parseStatements(String sqls) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqls));
        try {
            return parser.Statements();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

}
