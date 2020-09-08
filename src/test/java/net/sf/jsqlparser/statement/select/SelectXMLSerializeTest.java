/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Test;

/**
 *
 * @author tobens
 */
public class SelectXMLSerializeTest {
    @Test
    public void testXmlAgg1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) AS varchar (1024)) FROM mytable GROUP BY COMMENT_NUMBER");
    }
    
    @Test
    public void testXmlAgg2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE, COMMENT_LINE) AS varchar (1024)) FROM mytable GROUP BY COMMENT_NUMBER");
    }
    
    @Test
    public void testXmlAgg3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) AS varchar (1024))");
    }
    
    @Test
    public void testXmlAgg4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlserialize(xmlagg(xmltext(COMMENT_LINE_PREFIX || COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) AS varchar (1024))");
    }

    @Test
    public void testXmlAgg5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlserialize(xmlagg(xmltext(CONCAT(', ', TRIM(SOME_COLUMN))) ORDER BY MY_SEQUENCE) AS varchar (1024))");
    }
}
