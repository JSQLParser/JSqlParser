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
package net.sf.jsqlparser.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;

/**
 * Toolfunctions to start and use JSqlParser.
 * @author toben
 */
public class CCJSqlParserUtil {
	public static Statement parse(Reader statementReader) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(statementReader);
		try {
			return parser.Statement();
		} catch (Throwable e) {
			throw new JSQLParserException(e);
		}
	}
	
	public static Statement parse(String sql) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(new StringReader(sql));
		try {
			return parser.Statement();
		} catch (Throwable e) {
			throw new JSQLParserException(e);
		}
	}
	
	public static Statement parse(InputStream is) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(is);
		try {
			return parser.Statement();
		} catch (Throwable e) {
			throw new JSQLParserException(e);
		}
	}
	
	public static Statement parse(InputStream is, String encoding) throws JSQLParserException {
		CCJSqlParser parser = new CCJSqlParser(is,encoding);
		try {
			return parser.Statement();
		} catch (Throwable e) {
			throw new JSQLParserException(e);
		}
	}
}
