/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 * XML constructor functions taking a bare leading keyword before their first argument, e.g.
 * {@code XMLPARSE(CONTENT expr)}.
 */
public class SelectXMLParseTest {

    @Test
    public void testXmlParseContent() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlparse(content a) FROM mytable");
    }

    @Test
    public void testXmlParseDocument() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlparse(document a) FROM mytable");
    }

    @Test
    public void testXmlParseUpperCaseKeyword() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlparse(CONTENT a) FROM mytable");
    }

    @Test
    public void testXmlAggWithXmlParse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT xmlagg(xmlparse(content sn.CODE || ',') ORDER BY sn.CODE).getclobval() AS SN FROM GV_SYS_CODEINFO sn");
    }

    @Test
    public void testXmlElementName() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlelement(name a, b) FROM mytable");
    }

    @Test
    public void testXmlForestName() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT xmlforest(name a) FROM mytable");
    }

    /**
     * CONTENT, DOCUMENT and NAME must stay plain identifiers everywhere else.
     */
    @Test
    public void testKeywordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT content, document, name FROM mytable");
        assertSqlCanBeParsedAndDeparsed("SELECT content FROM content c WHERE c.document = 1");
        assertSqlCanBeParsedAndDeparsed("SELECT t.name AS document FROM mytable t");
        assertSqlCanBeParsedAndDeparsed("SELECT f(content) FROM mytable");
        assertSqlCanBeParsedAndDeparsed("SELECT xmlparse(content) FROM mytable");
    }
}
