package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReferentialActionTest {

    @Test
    void testCaseSensitivity() throws JSQLParserException {
        String sqlStr= "CREATE TABLE DATABASES\n"
                       + "(\n"
                       + "NAME VARCHAR(50) NOT NULL,\n"
                       + "OWNER VARCHAR(50) NOT NULL,\n"
                       + "PRIMARY KEY (NAME),\n"
                       + "FOREIGN KEY(OWNER) REFERENCES USERS (USERNAME) ON delete cascade\n"
                       + ")";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}