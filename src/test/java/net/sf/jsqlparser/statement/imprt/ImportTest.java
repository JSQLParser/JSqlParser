/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.imprt;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.CONCURRENT)
public class ImportTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT INTO schemaName.tableName FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO schemaName.tableName ( columnName ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO schemaName.tableName ( columnName1, columnName2 ) FROM LOCAL CSV FILE 'file.csv'"
    })
    public void testImportIntoTable(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT INTO ( columnName integer ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO ( columnName1 integer, columnName2 varchar(100) ) FROM LOCAL CSV FILE 'file.csv'",

            "IMPORT INTO ( LIKE schemaName.tableName ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO ( LIKE schemaName.tableName ( columnName ) ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO ( LIKE schemaName.tableName ( columnName1, columnName2 ) ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO ( LIKE schemaName.tableName ( columnName AS aliasName ) ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO ( LIKE schemaName.tableName ( columnName aliasName ) ) FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT INTO ( LIKE schemaName.tableName ( columnName1 AS aliasName2, columnName2 AS aliasName2 ) ) FROM LOCAL CSV FILE 'file.csv'"
    })
    public void testImportIntoImportColumns(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL CSV FILE 'file.csv'",
            "IMPORT FROM LOCAL CSV FILE 'file1.csv' FILE 'file2.csv'",

            "IMPORT FROM LOCAL SECURE CSV FILE 'file.csv'",
            "IMPORT FROM LOCAL SECURE CSV FILE 'file1.csv' FILE 'file2.csv'"
    })
    public void testImportFromFileCSV(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1 )",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1, 2 )",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1 FORMAT = 'format' )",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1 FORMAT = 'format', 2 FORMAT = 'format' )",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1 .. 2 )",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1, 1 .. 2 )",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ( 1, 1 .. 2, 3 )"
    })
    public void testImportFromFileCSVCols(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL FBV FILE 'file.fbv'",
            "IMPORT FROM LOCAL FBV FILE 'file1.fbv' FILE 'file2.fbv'",

            "IMPORT FROM LOCAL SECURE FBV FILE 'file.fbv'",
            "IMPORT FROM LOCAL SECURE FBV FILE 'file1.fbv' FILE 'file2.fbv'"
    })
    public void testImportFromFileFBV(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( SIZE = 1 )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( START = 1 )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( FORMAT = 'format' )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( ALIGN = LEFT )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( ALIGN = RIGHT )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( PADDING = 'padding' )",

            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( SIZE = 1, START = 1 )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( SIZE = 1 START = 1 )",
            "IMPORT FROM LOCAL FBV FILE 'file.fbv' ( SIZE = 1 START = 1, FORMAT = 'format' )"
    })
    public void testImportFromFileFBVCols(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ENCODING = 'UTF-8'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' SKIP = 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' TRIM",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' LTRIM",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' RTRIM",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' NULL = 'null'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ROW SEPARATOR = 'CRLF'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' COLUMN SEPARATOR = ','",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' COLUMN DELIMITER = '\"'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ROW SIZE = 1",

            "IMPORT FROM LOCAL CSV FILE 'file.csv' ENCODING = 'UTF-8' SKIP = 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ENCODING = 'UTF-8' SKIP = 1 TRIM"
    })
    public void testImportFromFileFileOpts(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL CSV FILE 'file.csv' VERIFY CERTIFICATE",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' IGNORE CERTIFICATE",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' PUBLIC KEY 'publicKey'",

            "IMPORT FROM LOCAL CSV FILE 'file.csv' VERIFY CERTIFICATE PUBLIC KEY 'publicKey'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' IGNORE CERTIFICATE PUBLIC KEY 'publicKey'"
    })
    public void testImportFromFileCertVerification(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM CSV AT connectionName FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' FILE 'file.csv'",

            "IMPORT FROM CSV AT connectionName USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",

            "IMPORT FROM CSV AT connectionName IGNORE CERTIFICATE FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName VERIFY CERTIFICATE FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName IGNORE CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName VERIFY CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",

            "IMPORT FROM CSV AT connectionName USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName USER 'user' IDENTIFIED BY 'password' PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT connectionName USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "IMPORT FROM CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'"
    })
    public void testImportFromConnectionDef(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM CSV AT CLOUD NONE connectionName FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD NONE '127.0.0.1' FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD NONE connectionName USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD NONE '127.0.0.1' USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD AZURE BLOBSTORAGE connectionName FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD AZURE BLOBSTORAGE '127.0.0.1' FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD AZURE BLOBSTORAGE connectionName USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "IMPORT FROM CSV AT CLOUD AZURE BLOBSTORAGE '127.0.0.1' USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'"
    })
    public void testImportFromCloudConnectionDef(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM EXA AT connectionName TABLE schemaName.tableName",
            "IMPORT FROM EXA AT connectionName TABLE schemaName.tableName ( columnName )",
            "IMPORT FROM EXA AT connectionName TABLE schemaName.tableName ( columnName1, columnName2 )",

            "IMPORT FROM EXA AT connectionName STATEMENT 'select 1'",
            "IMPORT FROM EXA AT connectionName STATEMENT 'select 1' STATEMENT 'select 2'"
    })
    public void testImportFromDBMSEXA(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM ORA AT connectionName TABLE schemaName.tableName",
            "IMPORT FROM ORA AT connectionName TABLE schemaName.tableName ( columnName )",
            "IMPORT FROM ORA AT connectionName TABLE schemaName.tableName ( columnName1, columnName2 )",

            "IMPORT FROM ORA AT connectionName STATEMENT 'select 1'",
            "IMPORT FROM ORA AT connectionName STATEMENT 'select 1' STATEMENT 'select 2'"
    })
    public void testImportFromDBMSORA(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM JDBC AT connectionName TABLE tableName",
            "IMPORT FROM JDBC DRIVER = 'driverName' AT connectionName TABLE tableName"
    })
    public void testImportFromDBMSJDBC(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM SCRIPT scriptName",
            "IMPORT FROM SCRIPT scriptName AT connectionName",
            "IMPORT FROM SCRIPT scriptName WITH propertyName = 'value'",
            "IMPORT FROM SCRIPT scriptName WITH propertyName = 'value' propertyName2 = 'value2'",
            "IMPORT FROM SCRIPT scriptName AT connectionName WITH propertyName = 'value' propertyName2 = 'value2'"
    })
    public void testImportFromScript(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "IMPORT FROM LOCAL CSV FILE 'file.csv' REJECT LIMIT 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED",

            "IMPORT FROM LOCAL CSV FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",

            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv'",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName",

            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT 1",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT 1 ERRORS",

            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT UNLIMITED",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT UNLIMITED",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT UNLIMITED",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",
            "IMPORT FROM LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT UNLIMITED ERRORS"
    })
    public void testImportErrorClause(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }
}
