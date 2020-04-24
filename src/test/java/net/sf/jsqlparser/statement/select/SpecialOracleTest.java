/*
 * Copyright (C) 2014 JSQLParser.
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
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.io.FileUtils;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertTrue;

/**
 * Tries to parse and deparse all statements in net.sf.jsqlparser.test.oracle-tests.
 *
 * As a matter of fact there are a lot of files that can still not processed. Here a step by step
 * improvement is the way to go.
 *
 * The test ensures that the successful parsed file count does not decrease.
 *
 * @author toben
 */
public class SpecialOracleTest {

    private static final File SQLS_DIR = new File("target/test-classes/net/sf/jsqlparser/statement/oracle-tests");
    private static final Logger LOG = Logger.getLogger(SpecialOracleTest.class.getName());

    @Test
    public void testAllSqlsParseDeparse() throws IOException {
        int count = 0;
        int success = 0;
        File[] sqlTestFiles = SQLS_DIR.listFiles();
        Map<String, Boolean> results = new TreeMap<>();

        for (File file : sqlTestFiles) {
            if (file.isFile()) {
                count++;
                LOG.log(Level.INFO, "testing {0}", file.getName());
                String sql = FileUtils.readFileToString(file);
                try {
                    assertSqlCanBeParsedAndDeparsed(sql, true);
                    success++;
                    results.put(file.getName(), Boolean.TRUE);
                    LOG.info("   -> SUCCESS");
                } catch (JSQLParserException ex) {
                    ex.printStackTrace();
                    //LOG.log(Level.SEVERE, null, ex);
                    LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                    results.put(file.getName(), Boolean.FALSE);
                } catch (Exception | ComparisonFailure ex) {
                    LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                    results.put(file.getName(), Boolean.FALSE);
                }
            }
        }

        LOG.log(Level.INFO, "tested {0} files. got {1} correct parse results", new Object[]{count, success});
        System.out.println("Results:\n" + formatResults(results));
        assertTrue(success >= 149);
    }

    @Test
    public void testAllSqlsOnlyParse() throws IOException {
        File[] sqlTestFiles = new File(SQLS_DIR, "only-parse-test").listFiles();

        for (File file : sqlTestFiles) {
            LOG.log(Level.INFO, "testing {0}", file.getName());
            String sql = FileUtils.readFileToString(file);
            try {
                CCJSqlParserUtil.parse(sql);

                LOG.info("   -> SUCCESS");
            } catch (JSQLParserException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Test
    public void testOperatorsWithSpaces() throws Exception {
        String sql;
        Statement statement;

        // First, the regular way (normal for most databases).
        sql = "SELECT\n"
                + "    Something\n"
                + "FROM\n"
                + "    Sometable\n"
                + "WHERE\n"
                + "    Somefield >= Somevalue\n"
                + "    AND Somefield <= Somevalue\n"
                + "    AND Somefield <> Somevalue\n"
                + "    AND Somefield != Somevalue\n";

        statement = CCJSqlParserUtil.parse(sql);

        System.out.println(statement.toString());

        assertSqlCanBeParsedAndDeparsed(sql, true);

        // Second, the special crap Oracle lets you get away with.
        sql = "SELECT\n"
                + "    Something\n"
                + "FROM\n"
                + "    Sometable\n"
                + "WHERE\n"
                + "    Somefield > = Somevalue\n"
                + "    AND Somefield < = Somevalue\n"
                + "    AND Somefield < > Somevalue\n";

        // Note, we do not (currently) test the "!=" with spaces in between -- Postgresql deals with this as two operators, "factorial" and "equals".
        statement = CCJSqlParserUtil.parse(sql);

        System.out.println(statement.toString());

        assertSqlCanBeParsedAndDeparsed(sql, true);

        // And then with multiple whitespace
        sql = "SELECT\n"
                + "    Something\n"
                + "FROM\n"
                + "    Sometable\n"
                + "WHERE\n"
                + "    Somefield > \t = Somevalue\n"
                + "    AND Somefield <   = Somevalue\n"
                + "    AND Somefield <\t\t> Somevalue\n";

        statement = CCJSqlParserUtil.parse(sql);

        System.out.println(statement.toString());

        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    private static String formatResults(Map<String, Boolean> results) {
        StringBuilder resultsString = new StringBuilder();
        for(Map.Entry<String, Boolean> result : results.entrySet()) {
            resultsString.append(result);
            resultsString.append(System.lineSeparator());
        }
        return resultsString.toString();
    }
}
