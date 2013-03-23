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
