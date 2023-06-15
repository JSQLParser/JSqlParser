package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class ForClauseTest {

    @Test
    void testForBrowse() throws JSQLParserException {
        String sqlStr = "SELECT * FROM table FOR BROWSE";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForXMLPath() throws JSQLParserException {
        String sqlStr =
                "SELECT * " +
                        "   FROM table " +
                        "   FOR XML PATH('something'), ROOT('trkseg'), TYPE, BINARY BASE64, ELEMENTS ABSENT ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForXMLRaw() throws JSQLParserException {
        String sqlStr =
                "SELECT * " +
                        "   FROM table " +
                        "   FOR XML RAW('something'), ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForXMLAuto() throws JSQLParserException {
        String sqlStr =
                "SELECT * " +
                        "   FROM table " +
                        "   FOR XML AUTO, ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForXMLExplicit() throws JSQLParserException {
        String sqlStr =
                "SELECT * " +
                        "   FROM table " +
                        "   FOR XML EXPLICIT, ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForXML() throws JSQLParserException {
        String sqlStr =
                "SELECT * " +
                        "   FROM table " +
                        "   FOR XML EXPLICIT, ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA " +
                        "UNION ALL " +
                        "SELECT * " +
                        "   FROM table " +
                        "   FOR XML EXPLICIT, ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA " +
                        "UNION ALL " +
                        "SELECT * " +
                        "   FROM table " +
                        "   FOR XML AUTO, ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA " +
                        "UNION ALL " +
                        "SELECT * " +
                        "   FROM table " +
                        "   FOR XML RAW('something'), ROOT('trkseg'), TYPE, BINARY BASE64, XMLDATA ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForJSON() throws JSQLParserException {
        String sqlStr =
                "SELECT * " +
                        "   FROM table " +
                        "   FOR JSON AUTO, ROOT('trkseg'), WITHOUT_ARRAY_WRAPPER, INCLUDE_NULL_VALUES "
                        +
                        "UNION ALL " +
                        "SELECT * " +
                        "   FROM table " +
                        "   FOR JSON PATH, ROOT('trkseg'), INCLUDE_NULL_VALUES, WITHOUT_ARRAY_WRAPPER ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1800() throws JSQLParserException {
        String sqlStr =
                "SELECT (SELECT '1.0' AS '@Version', (SELECT 'Test' AS 'name', (SELECT (SELECT DISTINCT 51.64315 AS '@lat', 14.31709 AS '@lon' FOR XML PATH('trkpt'), TYPE) FOR XML PATH(''), ROOT('trkseg'), TYPE) FOR XML PATH('trk'), TYPE) FOR XML PATH('gpx'), TYPE) FOR XML PATH('')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
