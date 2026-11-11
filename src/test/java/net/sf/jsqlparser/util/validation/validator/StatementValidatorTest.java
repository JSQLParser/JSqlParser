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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StatementValidatorTest extends ValidationTestAsserts {

    static Stream<Arguments> statementsOutsideSelect() {
        return Stream.of(
                Arguments.of("COPY t TO '/tmp/t.csv'", Feature.copy),
                Arguments.of("LOAD DATA INTO ds.t FROM FILES (format = 'CSV')", Feature.loadData),
                Arguments.of("EXPORT DATA OPTIONS (uri = 'gs://b/*.csv', format = 'CSV') "
                        + "AS SELECT a FROM t", Feature.export),
                Arguments.of("ATTACH DATABASE '/tmp/x.db' AS x", Feature.attach),
                Arguments.of("DETACH DATABASE x", Feature.detach),
                Arguments.of("CONNECT 'postgres://localhost/db'", Feature.connect),
                Arguments.of("DISCONNECT", Feature.disconnect),
                Arguments.of("PRAGMA database_list", Feature.pragma),
                Arguments.of("INSTALL httpfs", Feature.installExtension),
                Arguments.of("LOAD httpfs", Feature.loadExtension),
                Arguments.of("CREATE MACRO m(a) AS a + 1", Feature.createMacro),
                Arguments.of("CREATE EXTENSION REPOSITORY my_repo",
                        Feature.createExtensionRepository),
                Arguments.of("ASSERT (1 = 1) AS 'expected'", Feature.assertStatement),
                Arguments.of("SESSION START", Feature.session),
                Arguments.of("PREPARE p AS SELECT 1", Feature.prepare),
                Arguments.of("DEALLOCATE PREPARE p", Feature.deallocate),
                Arguments.of("SAVEPOINT s", Feature.savepoint),
                Arguments.of("ROLLBACK", Feature.rollback),
                Arguments.of("PREPARE TRANSACTION 'x'", Feature.twoPhaseCommit),
                Arguments.of("XA RECOVER", Feature.xa),
                Arguments.of("CREATE RESOURCE GROUP rg TYPE = USER", Feature.createResourceGroup),
                Arguments.of("ALTER RESOURCE GROUP rg DISABLE", Feature.alterResourceGroup),
                Arguments.of("SET RESOURCE GROUP rg", Feature.setResourceGroup),
                Arguments.of("CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x'",
                        Feature.createSpatialReferenceSystem),
                Arguments.of("ALTER INSTANCE RELOAD TLS", Feature.alterInstance),
                Arguments.of("LOCK TABLE t IN EXCLUSIVE MODE", Feature.lock),
                Arguments.of("ALTER SESSION SET CURRENT_SCHEMA = other", Feature.alterSession),
                Arguments.of("ALTER SYSTEM SET archive_command = 'cp %p /tmp/%f'",
                        Feature.alterSystem),
                Arguments.of("PURGE TABLE t", Feature.purge),
                Arguments.of("RENAME TABLE a TO b", Feature.alterTable),
                Arguments.of("ALTER TABLE ALL IN TABLESPACE a SET TABLESPACE b",
                        Feature.alterTablespace),
                Arguments.of("CREATE USER 'app'@'%' REQUIRE SSL", Feature.createUser),
                Arguments.of("CREATE POLICY p ON t", Feature.createPolicy),
                Arguments.of("ALTER POLICY p ON t RENAME TO q", Feature.alterPolicy),
                Arguments.of("CREATE STATISTICS s ON a, b FROM t", Feature.createStatistics),
                Arguments.of("ALTER STATISTICS s RENAME TO s2", Feature.alterStatistics),
                Arguments.of("CREATE EVENT e ON SCHEDULE EVERY 1 DAY DO SELECT 1",
                        Feature.createEvent),
                Arguments.of("ALTER EVENT e DISABLE", Feature.alterEvent),
                Arguments.of("CREATE TRIGGER trg BEFORE INSERT ON t FOR EACH ROW SELECT 1",
                        Feature.createTrigger),
                Arguments.of("CREATE LANGUAGE plperlu", Feature.allowUnsupportedStatements));
    }

    @ParameterizedTest
    @MethodSource("statementsOutsideSelect")
    public void testValidateStatementNotAllowedForSelect(String sql, Feature feature) {
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT, feature);
    }

    @Test
    public void testValidateExasolImportExportNotAllowedForSelect() {
        FeatureConfiguration exasol =
                new FeatureConfiguration().setValue(Feature.dialect, Dialect.EXASOL.name());
        for (String sql : Arrays.asList("IMPORT INTO t FROM LOCAL CSV FILE 'file.csv'",
                "SELECT * FROM (IMPORT FROM LOCAL CSV FILE 'file.csv')",
                "EXPORT t INTO LOCAL CSV FILE 'file.csv'")) {
            List<ValidationError> errors =
                    new Validation(exasol, List.of(FeaturesAllowed.SELECT), sql).validate();
            assertErrorsSize(errors, 1);
            assertNotAllowed(errors.get(0).getErrors(),
                    sql.startsWith("EXPORT") ? Feature.export : Feature.imprt);
        }
    }

    @Test
    public void testValidateStatementsForSupportingDatabases() throws JSQLParserException {
        validateNoErrors("ROLLBACK", 1, DatabaseType.DATABASES);
        validateNoErrors("SAVEPOINT s", 1, DatabaseType.ORACLE, DatabaseType.MYSQL,
                DatabaseType.MARIADB, DatabaseType.POSTGRESQL, DatabaseType.H2);
        validateNoErrors("LOCK TABLE t IN EXCLUSIVE MODE", 1, DatabaseType.ORACLE,
                DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER SYSTEM SET x = 1", 1, DatabaseType.ORACLE,
                DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER SESSION SET CURRENT_SCHEMA = other", 1, DatabaseType.ORACLE);
        validateNoErrors("DEALLOCATE PREPARE p", 1, DatabaseType.MYSQL, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL);
        for (String sql : Arrays.asList("COPY t FROM '/tmp/t.csv'", "PREPARE p AS SELECT 1",
                "CREATE POLICY p ON t", "ALTER POLICY p ON t RENAME TO q",
                "CREATE STATISTICS s ON a, b FROM t", "ALTER STATISTICS s RENAME TO s2")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        }
        for (String sql : Arrays.asList("CREATE EVENT e ON SCHEDULE EVERY 1 DAY DO DELETE FROM t",
                "ALTER EVENT e DISABLE")) {
            validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void testValidateCreateSchema() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE SCHEMA my_schema",
                "CREATE SCHEMA myschema AUTHORIZATION myauth")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }

    @Test
    public void testValidateCreateSchemaNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE SCHEMA my_schema",
                "CREATE SCHEMA myschema AUTHORIZATION myauth")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.createSchema);
        }
    }

    @Test
    public void testValidateDescNoErrors() throws JSQLParserException {
        for (String sql : Arrays.asList("DESC table_name", "EXPLAIN table_name")) {
            validateNoErrors(sql, 1, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testValidateTruncate() throws JSQLParserException {
        validateNoErrors("TRUNCATE TABLE my_table", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidateCommit() throws JSQLParserException {
        validateNoErrors("COMMIT", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidateBlock() throws JSQLParserException {
        validateNoErrors("BEGIN UPDATE tab SET val = 1 WHERE col = 2; END;", 1, DatabaseType.ORACLE,
                DatabaseType.SQLSERVER);
    }

    @Test
    public void testValidateComment() throws JSQLParserException {
        for (String sql : Arrays.asList("COMMENT ON VIEW myschema.myView IS 'myComment'",
                "COMMENT ON COLUMN myTable.myColumn is 'Some comment'",
                "COMMENT ON TABLE table1 IS 'comment1'")) {
            validateNoErrors(sql, 1, DatabaseType.H2, DatabaseType.ORACLE, DatabaseType.POSTGRESQL);
        }
    }

}
