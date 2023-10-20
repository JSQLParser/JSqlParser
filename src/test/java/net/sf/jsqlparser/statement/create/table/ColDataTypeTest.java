package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

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
}
