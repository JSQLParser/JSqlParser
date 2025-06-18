package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TimeTravelTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM my_table AT(TIMESTAMP => 'Wed, 26 Jun 2024 09:20:00 -0700'::TIMESTAMP_LTZ);",
            "SELECT * FROM my_table AT(OFFSET => -60*5) AS T WHERE T.flag = 'valid';",
            "SELECT * FROM my_table AT(STATEMENT => '8e5d0ca9-005e-44e6-b858-a8f5b37c5726');",
            "SELECT * FROM my_table BEFORE(STATEMENT => '8e5d0ca9-005e-44e6-b858-a8f5b37c5726');",
            "SELECT oldt.* ,newt.*\n"
                    + "  FROM my_table BEFORE(STATEMENT => '8e5d0ca9-005e-44e6-b858-a8f5b37c5726') AS oldt\n"
                    + "    FULL OUTER JOIN my_table AT(STATEMENT => '8e5d0ca9-005e-44e6-b858-a8f5b37c5726') AS newt\n"
                    + "    ON oldt.id = newt.id\n"
                    + "  WHERE oldt.id IS NULL OR newt.id IS NULL;"
    })
    void testSnowflakeAtBefore(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT C1\n"
                    + "  FROM t1\n"
                    + "  CHANGES(INFORMATION => APPEND_ONLY)\n"
                    + "  AT(TIMESTAMP => CURRENT_TIMESTAMP)\n"
                    + "  END(TIMESTAMP => CURRENT_TIMESTAMP);",
            "SELECT *\n"
                    + " FROM t1\n"
                    + "   CHANGES(INFORMATION => APPEND_ONLY)\n"
                    + "   AT(TIMESTAMP => $ts1);",
            "CREATE OR REPLACE TABLE t2 (\n"
                    + "  c1 varchar(255) default NULL\n"
                    + "  )\n"
                    + "AS SELECT C1\n"
                    + "  FROM t1\n"
                    + "  CHANGES(INFORMATION => APPEND_ONLY)\n"
                    + "  AT(TIMESTAMP => $ts1)\n"
                    + "  END(TIMESTAMP => $ts2);\n",
            "CREATE OR REPLACE TABLE t2 (\n"
                    + "  c1 varchar(255) default NULL\n"
                    + "  )\n"
                    + "AS SELECT C1\n"
                    + "  FROM t1\n"
                    + "  CHANGES(INFORMATION => APPEND_ONLY)\n"
                    + "  AT(STREAM => 's1')\n"
                    + "  END(TIMESTAMP => $ts2);\n"
    })
    void testSnowflakeChange(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM delta.`/delta/events` @ 20240618093000000;\n",
            "SELECT * FROM delta.`/delta/events` @V 5;\n",
            "SELECT * FROM delta.`/delta/events` TIMESTAMP AS OF '2024-06-01T00:00:00';\n",
            "SELECT * FROM delta.`/delta/events` VERSION AS OF 3;\n",
            "MERGE INTO target_table AS t\n"
                    + "USING source_table VERSION AS OF 5 AS s\n"
                    + "ON t.id = s.id\n"
                    + "WHEN MATCHED THEN UPDATE SET t.value = s.value;\n"
    })
    void testDataBricksTemporalSpec(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
