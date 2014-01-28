/*
 * Copyright (C) 2013 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.test;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.util.deparser.*;

import java.io.*;

import static junit.framework.Assert.assertEquals;

/**
 *
 * @author toben
 */
public class TestUtils {
	public static void assertSqlCanBeParsedAndDeparsed(String statement) throws JSQLParserException {
		Statement parsed = CCJSqlParserUtil.parse(new StringReader(statement));
		assertStatementCanBeDeparsedAs(parsed, statement);
	}

	public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement) {
		assertEquals(statement, parsed.toString());

		StatementDeParser deParser = new StatementDeParser(new StringBuilder());
		parsed.accept(deParser);
		assertEquals(statement, deParser.getBuffer().toString());
	}

	public static void assertExpressionCanBeDeparsedAs(final Expression parsed, String expression) {
		ExpressionDeParser expressionDeParser = new ExpressionDeParser();
		StringBuilder stringBuilder = new StringBuilder();
		expressionDeParser.setBuffer(stringBuilder);
		SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, stringBuilder);
		expressionDeParser.setSelectVisitor(selectDeParser);
		parsed.accept(expressionDeParser);

		assertEquals(expression, stringBuilder.toString());
	}
}
