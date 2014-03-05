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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Tries to parse and deparse all statments in net.sf.jsqlparser.test.oracle-tests.
 * @author toben
 */
public class SpecialOracleTests {
    
    private static final File SQLS_DIR = new File("target/test-classes/net/sf/jsqlparser/test/oracle-tests"); 
    private static final Logger LOG = Logger.getLogger(SpecialOracleTests.class.getName());
    
    @Test
    public void testAllSqls() throws IOException {
        File[] sqlTestFiles = SQLS_DIR.listFiles();
        
        for (File file : sqlTestFiles) {
            LOG.log(Level.INFO, "testing {0}", file.getName());
            String sql = FileUtils.readFileToString(file);
            try {
                assertSqlCanBeParsedAndDeparsed(sql, true);
                
                LOG.info("   -> SUCCESS");
            } catch (JSQLParserException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
}
