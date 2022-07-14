/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.analyze;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzeTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testAnalyze() throws JSQLParserException {
        String statement = "ANALYZE mytab";
        Analyze parsed = (Analyze) parserManager.parse(new StringReader(statement));
        assertEquals("mytab", parsed.getTable().getFullyQualifiedName());
        assertEquals(statement, "" + parsed);

        assertDeparse(new Analyze().withTable(new Table("mytab")), statement);
    }

}
