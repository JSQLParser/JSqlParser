/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class JsonFunctionTest {
    @Test
    public void testObjectAgg() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( foo:bar) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( foo:bar FORMAT JSON) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar ABSENT ON NULL) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar WITH UNIQUE KEYS) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar WITHOUT UNIQUE KEYS) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FROM dual ", true);
        
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FILTER( WHERE name = 'Raj' ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) OVER( PARTITION BY name ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FILTER( WHERE name = 'Raj' ) OVER( PARTITION BY name ) FROM dual ", true);
    }
    
    @Test
    public void testArrayAgg() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a ORDER BY a ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a NULL ON NULL ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON NULL ON NULL ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FILTER( WHERE name = 'Raj' ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) OVER( PARTITION BY name )  FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FILTER( WHERE name = 'Raj' ) OVER( PARTITION BY name ) FROM dual ", true);
    }
}
