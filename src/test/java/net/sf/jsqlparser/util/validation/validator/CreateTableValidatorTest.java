/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class CreateTableValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationCreateTable() throws JSQLParserException {
        String sql = "CREATE TABLE tab1 (id NUMERIC(10), val VARCHAR(30));";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationDropNotAllowed() throws JSQLParserException {
        String sql = "CREATE TABLE tab1 (id NUMERIC(10), val VARCHAR(30));";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.createTable);
    }

    @Test
    public void testValidationCreateTableWithIndex() throws JSQLParserException {
        String sql = "CREATE TABLE test_descending_indexes (c1 INT, c2 INT, INDEX idx1 (c1 ASC, c2 DESC))";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }
    @Test
    public void testValidationCreateTableWithIndex2() throws JSQLParserException {
        String sql = "CREATE TABLE TABLE1 (COLUMN1 VARCHAR2 (15), COLUMN2 VARCHAR2 (15), CONSTRAINT P_PK PRIMARY KEY (COLUMN1) USING INDEX TABLESPACE \"T_INDEX\") TABLESPACE \"T_SPACE\"";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationCreateTableFromSelect() throws JSQLParserException {
        String sql = "CREATE TABLE public.sales1 AS (SELECT * FROM public.sales)";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationCreateTableForeignKeyPrimaryKey() throws JSQLParserException {
        String sql = "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, PRIMARY KEY (id), FOREIGN KEY (user_id) REFERENCES ra_user(id))";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }


}
