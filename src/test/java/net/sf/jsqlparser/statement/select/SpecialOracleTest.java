/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * Tries to parse and deparse all statments in net.sf.jsqlparser.test.oracle-tests.
 *
 * As a matter of fact there are a lot of files that can still not processed. Here a step by step
 * improvement is the way to go.
 *
 * The test ensures, that the successful parsed file count does not decrease.
 *
 * @author toben
 */
public class SpecialOracleTest {

    // @todo: this is a workaround for Maven vs. Gradle
    // we will want to remove that after concluding the Gradle migration
    private static final File SQLS_DIR = new File(
            "target/test-classes/net/sf/jsqlparser/statement/select/oracle-tests").isDirectory()
                    ? new File(
                            "target/test-classes/net/sf/jsqlparser/statement/select/oracle-tests")
                    : new File(
                            "build/resources/test/net/sf/jsqlparser/statement/select/oracle-tests");

    private static final File SQL_SOURCE_DIR =
            new File("src/test/resources/net/sf/jsqlparser/statement/select/oracle-tests");

    private static final Logger LOG = Logger.getLogger(SpecialOracleTest.class.getName());

    private final List<String> EXPECTED_SUCCESSES = Arrays.asList("aggregate01.sql",
            "analytic_query04.sql", "analytic_query05.sql", "analytic_query06.sql",
            "analytic_query08.sql", "analytic_query09.sql", "analytic_query10.sql", "bindvar01.sql",
            "bindvar02.sql", "bindvar05.sql", "case_when01.sql", "case_when02.sql",
            "case_when03.sql", "case_when04.sql", "case_when05.sql", "cast_multiset01.sql",
            "cast_multiset02.sql", "cast_multiset03.sql", "cast_multiset04.sql",
            "cast_multiset05.sql", "cast_multiset06.sql", "cast_multiset07.sql",
            "cast_multiset08.sql", "cast_multiset10.sql", "cast_multiset11.sql",
            "cast_multiset12.sql", "cast_multiset16.sql", "cast_multiset17.sql",
            "cast_multiset18.sql", "cast_multiset19.sql", "cast_multiset20.sql",
            "cast_multiset21.sql", "cast_multiset22.sql", "cast_multiset23.sql",
            "cast_multiset24.sql", "cast_multiset25.sql", "cast_multiset26.sql",
            "cast_multiset27.sql", "cast_multiset28.sql", "cast_multiset29.sql",
            "cast_multiset30.sql", "cast_multiset31.sql", "cast_multiset32.sql",
            "cast_multiset33.sql", "cast_multiset35.sql", "cast_multiset36.sql",
            "cast_multiset40.sql", "cast_multiset41.sql", "cast_multiset42.sql",
            "cast_multiset43.sql", "columns01.sql", "condition01.sql", "condition02.sql",
            "condition03.sql", "condition04.sql", "condition05.sql", "condition07.sql",
            "condition08.sql", "condition09.sql", "condition10.sql", "condition12.sql",
            "condition14.sql", "condition15.sql", "condition19.sql", "condition20.sql",
            "connect_by01.sql", "connect_by02.sql", "connect_by03.sql", "connect_by04.sql",
            "connect_by05.sql", "connect_by06.sql", "connect_by07.sql", "datetime01.sql",
            "datetime02.sql", "datetime04.sql", "datetime05.sql", "datetime06.sql", "dblink01.sql",
            "for_update01.sql", "for_update02.sql", "for_update03.sql", "function04.sql",
            "function05.sql", "for_update04.sql", "for_update05.sql", "for_update06.sql",
            "for_update08.sql", "function01.sql", "function02.sql", "function03.sql",
            "function06.sql",
            "groupby01.sql",
            "groupby02.sql", "groupby03.sql", "groupby04.sql", "groupby05.sql", "groupby06.sql",
            "groupby08.sql", "groupby09.sql", "groupby10.sql", "groupby11.sql", "groupby12.sql",
            "groupby13.sql", "groupby14.sql", "groupby15.sql", "groupby16.sql", "groupby17.sql",
            "groupby19.sql", "groupby20.sql", "groupby21.sql", "groupby22.sql", "groupby23.sql",
            "insert02.sql", "insert11.sql", "insert12.sql", "interval02.sql", "interval04.sql",
            "interval05.sql", "join01.sql",
            "join02.sql", "join03.sql", "join04.sql", "join06.sql", "join07.sql", "join08.sql",
            "join09.sql", "join10.sql", "join11.sql", "join12.sql", "join13.sql", "join14.sql",
            "join15.sql", "join16.sql", "join17.sql", "join18.sql", "join19.sql", "join20.sql",
            "join21.sql", "keywordasidentifier01.sql", "keywordasidentifier02.sql",
            "keywordasidentifier03.sql", "keywordasidentifier04.sql", "keywordasidentifier05.sql",
            "lexer02.sql", "lexer03.sql", "lexer04.sql", "lexer05.sql", "like01.sql", "merge01.sql",
            "merge02.sql", "order_by01.sql", "order_by02.sql", "order_by03.sql", "order_by04.sql",
            "order_by05.sql", "order_by06.sql", "pivot01.sql", "pivot02.sql", "pivot03.sql",
            "pivot04.sql", "pivot05.sql", "pivot06.sql", "pivot07.sql", "pivot07_Parenthesis.sql",
            "pivot08.sql", "pivot09.sql", "pivot11.sql", "pivot12.sql", "query_factoring01.sql",
            "query_factoring02.sql", "query_factoring03.sql", "query_factoring06.sql",
            "query_factoring07.sql", "query_factoring08.sql", "query_factoring09.sql",
            "query_factoring11.sql", "query_factoring12.sql", "set01.sql", "set02.sql",
            "simple02.sql", "simple03.sql", "simple04.sql", "simple05.sql", "simple06.sql",
            "simple07.sql", "simple08.sql", "simple09.sql", "simple10.sql", "simple11.sql",
            "simple12.sql", "simple13.sql", "union01.sql", "union02.sql", "union03.sql",
            "union04.sql", "union05.sql", "union06.sql", "union07.sql", "union08.sql",
            "union09.sql", "union10.sql", "xmltable02.sql");

    @Test
    public void testAllSqlsParseDeparse() throws IOException {
        int count = 0;
        int success = 0;
        File[] sqlTestFiles = SQLS_DIR.listFiles();

        boolean foundUnexpectedFailures = false;

        for (File file : sqlTestFiles) {
            if (file.isFile()) {
                count++;
                String sql = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
                try {
                    assertSqlCanBeParsedAndDeparsed(sql, true);
                    success++;
                    recordSuccessOnSourceFile(file);
                } catch (JSQLParserException ex) {
                    String message = ex.getMessage();

                    // strip the Exception Class Name from the Message
                    if (message.startsWith(
                            net.sf.jsqlparser.parser.ParseException.class.getCanonicalName())) {
                        message = message.substring(net.sf.jsqlparser.parser.ParseException.class
                                .getCanonicalName().length() + 2);
                    }

                    int pos = message.indexOf('\n');
                    if (pos > 0) {
                        message = message.substring(0, pos);
                    }

                    if (sql.contains("@SUCCESSFULLY_PARSED_AND_DEPARSED")
                            || EXPECTED_SUCCESSES.contains(file.getName())) {
                        LOG.log(Level.SEVERE, "UNEXPECTED PARSING FAILURE: {0}\n\t" + message,
                                file.getName());
                        foundUnexpectedFailures = true;
                    } else {
                        LOG.log(Level.FINE, "EXPECTED PARSING FAILURE: {0}", file.getName());
                    }

                    recordFailureOnSourceFile(file, message);
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "UNEXPECTED EXCEPTION: {0}\n\t" + ex.getMessage(),
                            file.getName());
                    foundUnexpectedFailures = true;
                } catch (AssertionFailedError ex) {
                    if (sql.contains("@SUCCESSFULLY_PARSED_AND_DEPARSED")
                            || EXPECTED_SUCCESSES.contains(file.getName())) {
                        LOG.log(Level.SEVERE,
                                "UNEXPECTED DE-PARSING FAILURE: {0}\n" + ex.toString(),
                                file.getName());
                        foundUnexpectedFailures = true;
                    } else {
                        LOG.log(Level.FINE, "EXPECTED DE-PARSING FAILURE: {0}", file.getName());
                    }
                    recordFailureOnSourceFile(file, ex.getActual().getStringRepresentation());
                }
            }
        }

        LOG.log(Level.INFO, "tested {0} files. got {1} correct parse results, expected {2}",
                new Object[] {count, success, EXPECTED_SUCCESSES.size()});
        assertTrue(success >= EXPECTED_SUCCESSES.size());

        assertFalse(foundUnexpectedFailures, "Found Testcases failing unexpectedly.");
    }

    @Test
    // @Ignore
    public void debugSpecificSql() throws IOException, JSQLParserException {
        File[] sqlTestFiles = SQLS_DIR.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return "pivot04.sql".equals(name);
            }
        });

        for (File file : sqlTestFiles) {
            if (file.isFile()) {
                String sql = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
                assertSqlCanBeParsedAndDeparsed(sql, true);
            }
        }
    }

    public void recordSuccessOnSourceFile(File file) throws IOException {
        File sourceFile = new File(SQL_SOURCE_DIR, file.getName());
        String sourceSql = FileUtils.readFileToString(sourceFile, Charset.forName("UTF-8"));
        if (!sourceSql.contains("@SUCCESSFULLY_PARSED_AND_DEPARSED")) {
            LOG.log(Level.INFO, "NEW SUCCESS: {0}", file.getName());
            if (sourceFile.exists() && sourceFile.canWrite()) {
                try (FileWriter writer = new FileWriter(sourceFile, true)) {
                    writer.append("\n--@SUCCESSFULLY_PARSED_AND_DEPARSED first on ")
                            .append(DateFormat.getDateTimeInstance().format(new Date()));
                }
            }
        } else {
            if (EXPECTED_SUCCESSES.contains(file.getName())) {
                LOG.log(Level.FINE, "EXPECTED SUCCESS: {0}", file.getName());
            } else {
                LOG.log(Level.WARNING,
                        "UNRECORDED SUCCESS: {0}, please add to the EXPECTED_SUCCESSES List in SpecialOracleTest.java",
                        file.getName());
            }
        }
    }

    public void recordFailureOnSourceFile(File file, String message) throws IOException {
        File sourceFile = new File(SQL_SOURCE_DIR, file.getName());
        String sourceSql = FileUtils.readFileToString(sourceFile, Charset.forName("UTF-8"));
        if (!sourceSql.contains("@FAILURE: " + message) && sourceFile.canWrite()) {
            try (FileWriter writer = new FileWriter(sourceFile, true)) {
                writer.append("\n--@FAILURE: " + message + " recorded first on ")
                        .append(DateFormat.getDateTimeInstance().format(new Date()));
            }
        }
    }

    @Test
    public void testAllSqlsOnlyParse() throws IOException {
        File[] sqlTestFiles = new File(SQLS_DIR, "only-parse-test").listFiles();

        List<String> regressionFiles = new LinkedList<>();
        for (File file : sqlTestFiles) {
            String sql = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
            try {
                CCJSqlParserUtil.parse(sql);
                LOG.log(Level.FINE, "EXPECTED SUCCESS: {0}", file.getName());
            } catch (JSQLParserException ex) {
                regressionFiles.add(file.getName());

                String message = ex.getMessage();
                int pos = message.indexOf('\n');
                if (pos > 0) {
                    message = message.substring(0, pos);
                }

                LOG.log(Level.SEVERE, "UNEXPECTED PARSING FAILURE: {0}\n\t" + message,
                        file.getName());
            }
        }

        Assertions.assertThat(regressionFiles)
                .describedAs("All files should parse successfully, a regression was detected!")
                .isEmpty();
    }

    @Test
    public void testOperatorsWithSpaces() throws Exception {
        String sql;

        // First, the regular way (normal for most databases).
        sql = "SELECT\n" + "    Something\n" + "FROM\n" + "    Sometable\n" + "WHERE\n"
                + "    Somefield >= Somevalue\n" + "    AND Somefield <= Somevalue\n"
                + "    AND Somefield <> Somevalue\n" + "    AND Somefield != Somevalue\n";

        assertSqlCanBeParsedAndDeparsed(sql, true);

        // Second, the special crap Oracle lets you get away with.
        sql = "SELECT\n" + "    Something\n" + "FROM\n" + "    Sometable\n" + "WHERE\n"
                + "    Somefield > = Somevalue\n" + "    AND Somefield < = Somevalue\n"
                + "    AND Somefield < > Somevalue\n";

        // Note, we do not (currently) test the "!=" with spaces in between -- Postgresql deals with
        // this as two operators, "factorial" and "equals".
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // And then with multiple whitespace
        sql = "SELECT\n" + "    Something\n" + "FROM\n" + "    Sometable\n" + "WHERE\n"
                + "    Somefield > \t = Somevalue\n" + "    AND Somefield <   = Somevalue\n"
                + "    AND Somefield <\t\t> Somevalue\n";

        assertSqlCanBeParsedAndDeparsed(sql, true);
    }
}
