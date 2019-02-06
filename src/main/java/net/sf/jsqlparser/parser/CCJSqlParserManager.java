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

import java.io.Reader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;

public class CCJSqlParserManager implements JSqlParser {

    @Override
    public Statement parse(Reader statementReader) throws JSQLParserException {
        CCJSqlParser parser = new CCJSqlParser(new StreamProvider(statementReader));
        try {
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }
}
