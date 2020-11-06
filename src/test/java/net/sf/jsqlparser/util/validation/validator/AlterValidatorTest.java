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
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class AlterValidatorTest extends ValidationTestAsserts {

    @Test
    public void testAlterTableAddColumn() throws JSQLParserException {
        String sql = "ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddColumn_ColumnKeyWordImplicit() throws JSQLParserException {
        String sql = "ALTER TABLE mytable ADD mycolumn varchar (255)";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKey() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id)", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrable() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyNotDeferrable() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) NOT DEFERRABLE", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) VALIDATE", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyNoValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) NOVALIDATE", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrableValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE VALIDATE", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrableDisableNoValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE DISABLE NOVALIDATE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableUniqueKey() throws JSQLParserException {
        validateNoErrors("ALTER TABLE `schema_migrations` ADD UNIQUE KEY `unique_schema_migrations` (`version`)", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE CASCADE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddConstraint() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY)",
                1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddConstraintWithConstraintState() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY) DEFERRABLE DISABLE NOVALIDATE",
                1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddConstraintWithConstraintState2() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT RESOURCELINKTYPE_PRIMARYKEY PRIMARY KEY (PRIMARYKEY) DEFERRABLE NOVALIDATE",
                1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddUniqueConstraint() throws JSQLParserException {
        validateNoErrors("ALTER TABLE Persons ADD UNIQUE (ID)", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey2() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id)", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey3() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE RESTRICT", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey4() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE SET NULL", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableDropColumn() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test DROP COLUMN YYY", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAlterColumnDropNotNullIssue918() throws JSQLParserException {
        validateNoErrors("ALTER TABLE \"user_table_t\" ALTER COLUMN name DROP NOT NULL", 1, DatabaseType.DATABASES);
    }

}
