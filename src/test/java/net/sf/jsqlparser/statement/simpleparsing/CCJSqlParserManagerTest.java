/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.simpleparsing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Objects;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.CreateTableTest;
import net.sf.jsqlparser.test.TestException;
import org.junit.jupiter.api.Test;

public class CCJSqlParserManagerTest {

    @Test
    public void testParse() throws Exception {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        BufferedReader in = new BufferedReader(new InputStreamReader(Objects.requireNonNull(CreateTableTest.class.getResourceAsStream("/simple_parsing.txt"))));
        String statement = "";
        while (true) {
            try {
                statement = CCJSqlParserManagerTest.getStatement(in);
                if (statement == null) {
                    break;
                }
                parserManager.parse(new StringReader(statement));
            } catch (JSQLParserException e) {
                throw new TestException("impossible to parse statement: " + statement, e);
            }
        }
    }

    public static String getStatement(BufferedReader in) throws Exception {
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = CCJSqlParserManagerTest.getLine(in)) != null) {
            if (line.length() == 0) {
                break;
            }
            buf.append(line);
            buf.append("\n");
        }
        if (buf.length() > 0) {
            return buf.toString();
        } else {
            return null;
        }
    }

    public static String getLine(BufferedReader in) throws Exception {
        String line;
        while (true) {
            line = in.readLine();
            if (line != null) {
                if (line.length() < 2 || !(line.charAt(0) == '/' && line.charAt(1) == '/')) {
                    break;
                }
            } else {
                break;
            }
        }
        return line;
    }
}
