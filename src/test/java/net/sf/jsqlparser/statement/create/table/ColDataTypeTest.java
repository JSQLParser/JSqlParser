/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ColDataTypeTest {
    @Test
    void testPublicType() throws JSQLParserException {
        String sqlStr = "select 1::public.integer";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1879() throws JSQLParserException {
        String sqlStr = "CREATE TABLE public.film (\n" +
                "    film_id integer DEFAULT nextval('public.film_film_id_seq'::regclass) NOT NULL,\n"
                +
                "    title character varying(255) NOT NULL,\n" +
                "    description text,\n" +
                "    release_year public.year,\n" +
                "    language_id smallint NOT NULL,\n" +
                "    rental_duration smallint DEFAULT 3 NOT NULL,\n" +
                "    rental_rate numeric(4,2) DEFAULT 4.99 NOT NULL,\n" +
                "    length smallint,\n" +
                "    replacement_cost numeric(5,2) DEFAULT 19.99 NOT NULL,\n" +
                "    rating public.mpaa_rating DEFAULT 'G'::public.mpaa_rating,\n" +
                "    last_update timestamp without time zone DEFAULT now() NOT NULL,\n" +
                "    special_features text[],\n" +
                "    fulltext tsvector NOT NULL\n" +
                ")";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testNestedCast() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT acolumn::bit(64)::int(64) FROM mytable");
    }

    @Test
    void testStruct() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE IT.u (\n" +
                        "    details struct( id varchar(255), name varchar(255)) NOT NULL,\n" +
                        "    name VARCHAR(255) NOT NULL\n" +
                        "  );\n";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    // PG/Oracle allow an INTERVAL column type carrying a time-unit qualifier,
    // e.g. `interval hour to minute`. Previously the qualifier was not consumed
    // as part of the data type, so parsing failed with "Encountered ... <K_DATE_LITERAL>".
    @Test
    void testIntervalQualifierIssue1728() throws JSQLParserException {
        String sqlStr = "CREATE TABLE films (code char(5), len interval hour to minute)";
        CreateTable create = (CreateTable) assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        // The qualifier must be attached to the column's data type, not parsed as an
        // alias or left unconsumed.
        ColumnDefinition len = create.getColumnDefinitions().stream()
                .filter(c -> c.getColumnName().equalsIgnoreCase("len"))
                .findFirst()
                .orElseThrow();
        assertEquals("interval hour to minute", len.getColDataType().getDataType());
    }

    @Test
    void testIntervalQualifierYearToMonthIssue1728() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (a interval year to month)", true);
    }
}
