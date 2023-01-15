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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class LikeExpressionTest {

    @Test
    public void testLikeWithEscapeExpressionIssue420() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("a LIKE ?1 ESCAPE ?2", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed("select * from dual where a LIKE ?1 ESCAPE ?2", true);
    }

    @Test
    public void testEscapeExpressionIssue1638() throws JSQLParserException {
        String sqlStr = "select case \n"
                        + "    when id_portfolio like '%\\_1' escape '\\' then '1'\n"
                        + "    end";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );

        Assertions.assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parse(
                        sqlStr
                        , parser -> parser.withBackslashEscapeCharacter(true)
                );
            }
        });
    }

    @Test
    public void testEscapingIssue1209() throws JSQLParserException {
        String sqlStr="INSERT INTO \"a\".\"b\"(\"c\", \"d\", \"e\") VALUES ('c c\\', 'dd', 'ee\\')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );
    }

    @Test
    public void testEscapingIssue1173() throws JSQLParserException {
        String sqlStr="update PARAM_TBL set PARA_DESC = null where PARA_DESC = '\\' and DEFAULT_VALUE = '\\'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );
    }

    @Test
    public void testEscapingIssue1172() throws JSQLParserException {
        String sqlStr="SELECT A ALIA1, CASE WHEN B LIKE 'ABC\\_%' ESCAPE '\\' THEN 'DEF' ELSE 'CCCC' END AS OBJ_SUB_TYPE FROM TABLE2";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );
    }

    @Test
    public void testEscapingIssue832() throws JSQLParserException {
        String sqlStr="SELECT * FROM T1 WHERE (name LIKE ? ESCAPE '\\') AND (description LIKE ? ESCAPE '\\')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );
    }

    @Test
    public void testEscapingIssue827() throws JSQLParserException {
        String sqlStr="INSERT INTO my_table (my_column_1, my_column_2) VALUES ('my_value_1\\', 'my_value_2')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );
    }

    @Test
    public void testEscapingIssue578() throws JSQLParserException {
        String sqlStr="SELECT * FROM t1 WHERE UPPER(t1.TIPCOR_A8) like ? ESCAPE '' ORDER BY PERFILB2||TRANSLATE(UPPER(AP1SOL10 || ' ' || AP2SOL10 || ',' || NOMSOL10), '?', 'A') asc";
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                sqlStr
                , true
                , parser -> parser.withBackslashEscapeCharacter(false)
        );
    }
}
