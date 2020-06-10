/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class Visualizer {

    public static void main(String[] args) throws Exception
    {
        CCJSqlParserUtil.parse("SELECT * FROM\n" +
                "(\n" +
                "  SELECT column1, column2\n" +
                "  FROM tables\n" +
                "  WHERE conditions\n" +
                ")\n" +
                "PIVOT \n" +
                "(\n" +
                "  aggregate_function(column2)\n" +
                "  FOR column2\n" +
                "  IN ( (1,2),(3,4)) )");

        String sql = "SELECT * FROM ( select a,b from foo) PIVOT ( func(column2) for column2 in ( (1,2), (3,4)) )";
        Statement statement = CCJSqlParserUtil.parse(sql);
        System.out.println(statement);
    }
}
