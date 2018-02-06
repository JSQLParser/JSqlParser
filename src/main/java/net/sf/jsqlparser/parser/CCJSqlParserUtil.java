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
package net.sf.jsqlparser.parser;

import java.io.InputStream;
import java.io.Reader;

import lombok.experimental.UtilityClass;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

/**
 * Toolfunctions to start and use JSqlParser.
 *
 * @author toben
 */
@UtilityClass
public final class CCJSqlParserUtil {

	public Statement parse(Reader statementReader) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(new StreamProvider(statementReader));
		try {
			return parser.Statement();
		} catch (Exception ex) {
			throw new JSQLParserException(ex);
		}
	}

	public Statement parse(String sql) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(new StringProvider(sql));
		try {
			return parser.Statement();
		} catch (Exception ex) {
			throw new JSQLParserException(ex);
		}
	}

	public Node parseAST(String sql) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(new StringProvider(sql));
		try {
			parser.Statement();
			return parser.jjtree.rootNode();
		} catch (Exception ex) {
			throw new JSQLParserException(ex);
		}
	}

	public Statement parse(InputStream is) throws JSQLParserException {
		try {
			CCJSqlParser parser = new CCJSqlParser(new StreamProvider(is));
			return parser.Statement();
		} catch (Exception ex) {
			throw new JSQLParserException(ex);
		}
	}

	public Statement parse(InputStream is, String encoding) throws JSQLParserException {
		try {
			CCJSqlParser parser = new CCJSqlParser(new StreamProvider(is, encoding));
			return parser.Statement();
		} catch (Exception ex) {
			throw new JSQLParserException(ex);
		}
	}

	/**
	 * Parse an expression.
	 *
	 * @param expression
	 * @return
	 * @throws JSQLParserException
	 */
	public Expression parseExpression(String expression) throws JSQLParserException {
		return parseExpression(expression, true);
	}

	public Expression parseExpression(String expression, boolean allowPartialParse) throws JSQLParserException {
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

	/**
	 * Parse an conditional expression. This is the expression after a where clause.
	 *
	 * @param condExpr
	 * @return
	 * @throws JSQLParserException
	 */
	public Expression parseCondExpression(String condExpr) throws JSQLParserException {
		return parseCondExpression(condExpr, true);
	}

	/**
	 * Parse an conditional expression. This is the expression after a where clause.
	 *
	 * @param condExpr
	 * @param allowPartialParse false: needs the whole string to be processed.
	 * @return
	 */
	public Expression parseCondExpression(String condExpr, boolean allowPartialParse) throws JSQLParserException {
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
	public Statements parseStatements(String sqls) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqls));
		try {
			return parser.Statements();
		} catch (Exception ex) {
			throw new JSQLParserException(ex);
		}
	}

}
