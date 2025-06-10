/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.export;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.CONCURRENT)
public class ExportTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv'",
            "EXPORT schemaName.tableName ( columnName ) INTO LOCAL CSV FILE 'file.csv'",
            "EXPORT schemaName.tableName ( columnName1, columnName2 ) INTO LOCAL CSV FILE 'file.csv'",

            "EXPORT ( select 1 ) INTO LOCAL CSV FILE 'file.csv'",
    })
    public void testExport(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file1.csv' FILE 'file2.csv'",

            "EXPORT schemaName.tableName INTO LOCAL SECURE CSV FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO LOCAL SECURE CSV FILE 'file1.csv' FILE 'file2.csv'"
    })
    public void testExportIntoFileCSV(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1, 2 )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 FORMAT = 'format' )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 DELIMIT = ALWAYS )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 DELIMIT = NEVER )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 DELIMIT = AUTO )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 FORMAT = 'format', 2 DELIMIT = NEVER )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 FORMAT = 'format', 2 DELIMIT = NEVER, 3 FORMAT = 'format' DELIMIT = ALWAYS )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1 .. 2 )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1, 1 .. 2 )",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ( 1, 1 .. 2, 3 )"
    })
    public void testExportIntoFileCSVCols(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv'",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file1.fbv' FILE 'file2.fbv'",

            "EXPORT schemaName.tableName INTO LOCAL SECURE FBV FILE 'file.fbv'",
            "EXPORT schemaName.tableName INTO LOCAL SECURE FBV FILE 'file1.fbv' FILE 'file2.fbv'"
    })
    public void testExportIntoFileFBV(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( SIZE = 1 )",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( FORMAT = 'format' )",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( ALIGN = LEFT )",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( ALIGN = RIGHT )",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( PADDING = '0' )",

            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( SIZE = 1, PADDING = '0' )",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( SIZE = 1 PADDING = '0' )",
            "EXPORT schemaName.tableName INTO LOCAL FBV FILE 'file.fbv' ( SIZE = 1 PADDING = '0', FORMAT = 'format' )"
    })
    public void testExportIntoFileFBVCols(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ENCODING = 'UTF-8'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' REPLACE",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' TRUNCATE",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' NULL = 'null'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' BOOLEAN = 'yes/no'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ROW SEPARATOR = 'CRLF'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' COLUMN SEPARATOR = ','",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' COLUMN DELIMITER = '\"'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' DELIMIT = ALWAYS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' DELIMIT = NEVER",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' DELIMIT = AUTO",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' WITH COLUMN NAMES",

            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ENCODING = 'UTF-8' REPLACE",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ENCODING = 'UTF-8' REPLACE WITH COLUMN NAMES"
    })
    public void testExportIntoFileFileOpts(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' VERIFY CERTIFICATE",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' IGNORE CERTIFICATE",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' PUBLIC KEY 'publicKey'",

            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' VERIFY CERTIFICATE PUBLIC KEY 'publicKey'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' IGNORE CERTIFICATE PUBLIC KEY 'publicKey'"
    })
    public void testExportIntoFileCertVerification(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO CSV AT connectionName FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' FILE 'file.csv'",

            "EXPORT schemaName.tableName INTO CSV AT connectionName USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",

            "EXPORT schemaName.tableName INTO CSV AT connectionName IGNORE CERTIFICATE FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName VERIFY CERTIFICATE FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName IGNORE CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName VERIFY CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",

            "EXPORT schemaName.tableName INTO CSV AT connectionName USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName USER 'user' IDENTIFIED BY 'password' PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' IGNORE CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT connectionName USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT '127.0.0.1' USER 'user' IDENTIFIED BY 'password' VERIFY CERTIFICATE PUBLIC KEY 'publicKey' FILE 'file.csv'"
    })
    public void testExportIntoConnectionDef(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO CSV AT CLOUD NONE connectionName FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD NONE '127.0.0.1' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD NONE connectionName USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD NONE '127.0.0.1' USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD AZURE BLOBSTORAGE connectionName FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD AZURE BLOBSTORAGE '127.0.0.1' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD AZURE BLOBSTORAGE connectionName USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO CSV AT CLOUD AZURE BLOBSTORAGE '127.0.0.1' USER 'user' IDENTIFIED BY 'password' FILE 'file.csv'"
    })
    public void testExportIntoCloudConnectionDef(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName",
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName ( columnName )",
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName ( columnName1, columnName2 )",

            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName REPLACE",
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName TRUNCATE",
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName CREATED BY 'CREATE OR REPLACE TABLE schemaName (columnName INTEGER)'",

            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName REPLACE TRUNCATE",
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName ( columnName ) REPLACE TRUNCATE",
            "EXPORT schemaName.tableName INTO EXA AT connectionName TABLE schemaName.tableName ( columnName1, columnName2 ) REPLACE TRUNCATE",

            "EXPORT schemaName.tableName INTO EXA AT connectionName STATEMENT 'insert into schemaName.tableName ( columnName ) values ( ? )'"
    })
    public void testExportIntoDBMSEXA(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO ORA AT connectionName TABLE schemaName.tableName",
            "EXPORT schemaName.tableName INTO ORA AT connectionName TABLE schemaName.tableName ( columnName )",
            "EXPORT schemaName.tableName INTO ORA AT connectionName TABLE schemaName.tableName ( columnName1, columnName2 )",

            "EXPORT schemaName.tableName INTO ORA AT connectionName STATEMENT 'insert into schemaName.tableName ( columnName ) values ( ? )'"
    })
    public void testExportIntoDBMSORA(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO JDBC AT connectionName TABLE tableName",
            "EXPORT schemaName.tableName INTO JDBC DRIVER = 'driverName' AT connectionName TABLE tableName"
    })
    public void testExportIntoDBMSJDBC(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO SCRIPT scriptName",
            "EXPORT schemaName.tableName INTO SCRIPT scriptName AT connectionName",
            "EXPORT schemaName.tableName INTO SCRIPT scriptName WITH propertyName = 'value'",
            "EXPORT schemaName.tableName INTO SCRIPT scriptName WITH propertyName = 'value' propertyName2 = 'value2'",
            "EXPORT schemaName.tableName INTO SCRIPT scriptName AT connectionName WITH propertyName = 'value' propertyName2 = 'value2'"
    })
    public void testExportIntoScript(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT 1",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED",

            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",

            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv'",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName",

            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT 1",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT 1",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT 1",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT 1",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT 1 ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT 1 ERRORS",

            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT UNLIMITED",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT UNLIMITED",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT UNLIMITED",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO CSV AT connectionName FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL CSV FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO LOCAL SECURE CSV FILE 'file.csv' REJECT LIMIT UNLIMITED ERRORS",
            "EXPORT schemaName.tableName INTO LOCAL CSV FILE 'file.csv' ERRORS INTO schemaName.tableName REJECT LIMIT UNLIMITED ERRORS"
    })
    public void testImportErrorClause(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withDialect(Dialect.EXASOL));
    }
}
