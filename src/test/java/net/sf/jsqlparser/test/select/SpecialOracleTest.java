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
package net.sf.jsqlparser.test.select;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.parser.TokenMgrError;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Tries to parse and deparse all statments in
 * net.sf.jsqlparser.test.oracle-tests.
 *
 * As a matter of fact there are a lot of files that can still not processed.
 * Here a step by step improvement is the way to go.
 *
 * The test ensures, that the successfull parsed file count does not decrease.
 *
 * @author toben
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpecialOracleTest {

    private static final File SQLS_DIR = new File("target/test-classes/net/sf/jsqlparser/test/oracle-tests");
    private static final Logger LOG = Logger.getLogger(SpecialOracleTest.class.getName());
    private static final Set<String> EXCEPTION_FILES = new HashSet<String>(Arrays.asList("groupby07.sql"));

    private static Set<String> testedExceptions = null;
    
    @BeforeClass
    public static void setup() {
    	testedExceptions = new HashSet<String>();
    }

    @Test
    public void testAllSqlsParseDeparse() throws IOException {
        int count = 0;
        int success = 0;
        File[] sqlTestFiles = SQLS_DIR.listFiles();

        for (File file : sqlTestFiles) {
            if (file.isFile()) {
                count++;
                String filename = file.getName();
                if(EXCEPTION_FILES.contains(filename)) {
    				LOG.log(Level.INFO, "skipping {0}, is an exception to be tested separately", filename);
                } else {
					LOG.log(Level.INFO, "testing {0}", filename);
                	String sql = FileUtils.readFileToString(file);
                	try {
	                    assertSqlCanBeParsedAndDeparsed(sql, true);
                    	success++;
                    	LOG.info("   -> SUCCESS");
                	} catch (JSQLParserException ex) {
                    	//LOG.log(Level.SEVERE, null, ex);
                    	LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                	} catch (TokenMgrError ex) {
                    	//LOG.log(Level.SEVERE, null, ex);
                    	LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                	} catch (Exception ex) {
	                    LOG.log(Level.INFO, "   -> PROBLEM {0}", ex.toString());
                	}
            	}
        	}
        }

        LOG.log(Level.INFO, "tested {0} files. got {1} correct parse results", new Object[]{count, success});
        assertTrue(success >= 130);
    }

    @Test
    public void testExceptionGroupBy07() throws Exception {
        // This particular file has 'misordered' having and group by clauses.  This is valid in Oracle, but requires a
        // bit of special test handling, since the parser will 'correct' the order for us (upon 'deparsing').
    	Pattern swapHavingGroupBy = Pattern.compile("^(.*)\\b(having.*)\\b(group by.*)\\b(order by.*)$",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    	
    	String filename = "groupby07.sql";
    	testedExceptions.add(filename);
    	File file = new File(SQLS_DIR,filename);
        String sql = FileUtils.readFileToString(file);
        Statement parsed = CCJSqlParserUtil.parse(sql);
        sql = swapHavingGroupBy.matcher(sql).replaceFirst("$1$3$2$4"); // Swaps the 'having' and 'group by', as the parser will swap these around when deparsing.
        assertStatementCanBeDeparsedAs(parsed, sql, true);
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
    
    @Test // Named "zzz" to be executed last (see @FixMethodOrder annotation on this class).
    public void zzztestAllExceptionsHandled() throws Exception {
    	Set<String> tmp = new HashSet<String>(EXCEPTION_FILES);
    	tmp.removeAll(testedExceptions);
    	assertTrue("Some 'exception' SQL files were not tested separately -- still remaining: " + tmp, tmp.isEmpty());
    }
}
