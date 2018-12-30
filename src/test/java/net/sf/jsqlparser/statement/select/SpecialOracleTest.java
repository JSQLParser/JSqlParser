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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertTrue;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 * Tries to parse and deparse all statments in net.sf.jsqlparser.test.oracle-tests.
 *
 * As a matter of fact there are a lot of files that can still not processed. Here a step by step
 * improvement is the way to go.
 *
 * The test ensures, that the successfull parsed file count does not decrease.
 *
 * @author toben
 */
public class SpecialOracleTest {

    private static final File SQLS_DIR = new File("target/test-classes/net/sf/jsqlparser/statement/oracle-tests");
    private static final Logger LOG = Logger.getLogger(SpecialOracleTest.class.getName());

    private List<String> successes = Arrays.asList("aggregate01.sql",
            "analytic_query06.sql",
            "analytic_query08.sql",
            "analytic_query09.sql",
            "analytic_query10.sql",
            "bindvar01.sql",
            "bindvar02.sql",
            "case_when01.sql",
            "case_when02.sql",
            "case_when03.sql",
            "case_when04.sql",
            "case_when05.sql",
            "cast_multiset01.sql",
            "cast_multiset02.sql",
            "cast_multiset03.sql",
            "cast_multiset04.sql",
            "cast_multiset05.sql",
            "cast_multiset06.sql",
            "cast_multiset08.sql",
            "cast_multiset10.sql",
            "cast_multiset11.sql",
            "cast_multiset12.sql",
            "cast_multiset16.sql",
            "cast_multiset17.sql",
            "cast_multiset20.sql",
            "cast_multiset21.sql",
            "cast_multiset23.sql",
            "cast_multiset25.sql",
            "cast_multiset28.sql",
            "cast_multiset29.sql",
            "cast_multiset30.sql",
            "cast_multiset31.sql",
            "cast_multiset32.sql",
            "cast_multiset33.sql",
            "cast_multiset35.sql",
            "cast_multiset36.sql",
            "cast_multiset40.sql",
            "cast_multiset41.sql",
            "cast_multiset42.sql",
            "cast_multiset43.sql",
            "columns01.sql",
            "condition01.sql",
            "condition02.sql",
            "condition03.sql",
            "condition04.sql",
            "condition05.sql",
            "condition07.sql",
            "condition09.sql",
            "condition10.sql",
            "condition12.sql",
            "condition14.sql",
            "condition20.sql",
            "connect_by02.sql",
            "connect_by03.sql",
            "connect_by04.sql",
            "connect_by05.sql",
            "connect_by07.sql",
            "datetime02.sql",
            "datetime04.sql",
            "datetime05.sql",
            "datetime06.sql",
            "dblink01.sql",
            "for_update01.sql",
            "for_update02.sql",
            "for_update03.sql",
            "for_update05.sql",
            "function01.sql",
            "function02.sql",
            "groupby08.sql",
            "groupby09.sql",
            "groupby12.sql",
            "groupby14.sql",
            "groupby19.sql",
            "groupby20.sql",
            "groupby21.sql",
            "groupby22.sql",
            "groupby23.sql",
            "interval02.sql",
            "interval04.sql",
            "join01.sql",
            "join02.sql",
            "join03.sql",
            "join04.sql",
            "join06.sql",
            "join07.sql",
            "join08.sql",
            "join09.sql",
            "join10.sql",
            "join11.sql",
            "join12.sql",
            "join14.sql",
            "join15.sql",
            "join16.sql",
            "join18.sql",
            "join19.sql",
            "join20.sql",
            "join21.sql",
            "keywordasidentifier01.sql",
            "keywordasidentifier02.sql",
            "keywordasidentifier03.sql",
            "keywordasidentifier05.sql",
            "lexer02.sql",
            "lexer03.sql",
            "lexer04.sql",
            "lexer05.sql",
            "like01.sql",
            "order_by01.sql",
            "order_by02.sql",
            "order_by03.sql",
            "order_by04.sql",
            "order_by05.sql",
            "order_by06.sql",
            "pivot02.sql",
            "pivot03.sql",
            "pivot05.sql",
            "pivot06.sql",
            "pivot07.sql",
            "pivot08.sql",
            "pivot09.sql",
            "pivot11.sql",
            "query_factoring01.sql",
            "query_factoring02.sql",
            "query_factoring03.sql",
            "query_factoring06.sql",
            "query_factoring08.sql",
            "query_factoring09.sql",
            "query_factoring11.sql",
            "query_factoring12.sql",
            "set01.sql",
            "set02.sql",
            "simple02.sql",
            "simple03.sql",
            "simple06.sql",
            "simple07.sql",
            "simple08.sql",
            "simple09.sql",
            "simple10.sql",
            "simple11.sql",
            "simple12.sql",
            "simple13.sql",
            "union01.sql",
            "union02.sql",
            "union03.sql",
            "union04.sql",
            "union05.sql",
            "union06.sql",
            "union07.sql",
            "union08.sql",
            "union09.sql",
            "union10.sql");

    @Test
    public void testAllSqlsParseDeparse() throws IOException {
        int count = 0;
        int success = 0;
        File[] sqlTestFiles = SQLS_DIR.listFiles();

        for (File file : sqlTestFiles) {
            if (file.isFile()) {
                count++;
                LOG.log(Level.INFO, "testing {0}", file.getName());
                String sql = FileUtils.readFileToString(file);
                boolean parsed = false;
                try {
                    assertSqlCanBeParsedAndDeparsed(sql, true);
                    success++;
                    parsed = true;
                    LOG.info("   -> SUCCESS");
                } catch (JSQLParserException ex) {
                    ex.printStackTrace();
                    //LOG.log(Level.SEVERE, null, ex);
                    LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                } catch (Exception ex) {
                    LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                } catch (ComparisonFailure ex) {
                    LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                }

                if (!parsed && successes.contains(file.getName())) {
                    LOG.log(Level.WARNING, "   -> regression on file {0}", file.getName());
                }
            }
        }

        LOG.
                log(Level.INFO, "tested {0} files. got {1} correct parse results", new Object[]{count, success});
        assertTrue(success >= 150);
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
}
