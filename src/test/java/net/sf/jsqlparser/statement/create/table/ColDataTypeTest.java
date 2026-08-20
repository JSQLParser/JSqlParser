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
import net.sf.jsqlparser.expression.IntervalQualifier;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    // ---- INTERVAL column type with qualifier (issue #1728 / SQL standard) ----

    @Test
    void testIntervalColumnFieldToFieldRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (a interval hour to minute)", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (a interval year to month)", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (a interval day to second)", true);
    }

    @Test
    void testIntervalColumnBarePrecisionUnaffected() throws JSQLParserException {
        // the leading-field-less precision form must keep working
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (a interval(2))", true);
    }

    @Test
    void testIntervalQualifierStructurallyAttachedToColumnType() throws JSQLParserException {
        CreateTable create = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE t (len interval hour to minute)", true);
        ColumnDefinition len = create.getColumnDefinitions().stream()
                .filter(c -> c.getColumnName().equalsIgnoreCase("len"))
                .findFirst()
                .orElseThrow();
        // The qualifier must be a structured property of the column type, not a bare string.
        IntervalQualifier qualifier = len.getColDataType().getIntervalQualifier();
        assertNotNull(qualifier);
        assertEquals("hour", qualifier.getLeadingField());
        assertEquals("minute", qualifier.getTrailingField());
    }

    @Test
    void testCastAsIntervalWithQualifierRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(col AS INTERVAL DAY TO SECOND)", true);
        assertSqlCanBeParsedAndDeparsed("SELECT CAST(col AS INTERVAL HOUR)", true);
    }
}
