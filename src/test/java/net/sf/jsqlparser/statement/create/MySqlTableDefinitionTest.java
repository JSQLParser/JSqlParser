/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.TableOption;
import org.junit.jupiter.api.Test;

/** Regression tests for MySQL table definitions and their structured AST representation. */
public class MySqlTableDefinitionTest {

    @Test
    public void testConstraintBeforeColumnDefinition() throws JSQLParserException {
        CreateTable table =
                parseMySql("CREATE TABLE inventory (PRIMARY KEY (id), id INT NOT NULL)");

        assertEquals(2, table.getTableElements().size());
        assertInstanceOf(Index.class, table.getTableElements().get(0));
        assertInstanceOf(ColumnDefinition.class, table.getTableElements().get(1));
        assertEquals("PRIMARY KEY", table.getIndexes().get(0).getType());
        assertReparse(table);
    }

    @Test
    public void testNamedPrimaryAndSpecializedIndexes() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE documents (id INT, body TEXT, data JSON, "
                + "PRIMARY KEY pk_documents (id), FULLTEXT INDEX ft_body (body), "
                + "INDEX ((CAST(data AS CHAR(30)))))");

        NamedConstraint primary = (NamedConstraint) table.getIndexes().get(0);
        assertEquals("pk_documents", primary.getIndexName());
        assertEquals(Index.Kind.PRIMARY_KEY, primary.getKind());
        assertEquals(Index.Kind.FULLTEXT, table.getIndexes().get(1).getKind());
        assertTrue(table.getIndexes().get(2).getColumns().get(0).isExpression());
        assertReparse(table);
    }

    @Test
    public void testFulltextAndSpatialIndexes() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE map_entry (body TEXT, coordinates POINT, "
                + "FULLTEXT KEY ft_body (body), SPATIAL INDEX sp_coordinates (coordinates))");

        assertEquals(Index.Kind.FULLTEXT, table.getIndexes().get(0).getKind());
        assertEquals("KEY", table.getIndexes().get(0).getType().split(" ")[1]);
        assertEquals(Index.Kind.SPATIAL, table.getIndexes().get(1).getKind());
        assertReparse(table);
    }

    @Test
    public void testStructuredColumnOptions() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE child_record (id SMALLINT UNSIGNED "
                + "SERIAL DEFAULT VALUE NOT NULL, parent_id INT REFERENCES parent_record(id) "
                + "MATCH FULL ON UPDATE CASCADE ON DELETE SET NULL)");

        ColumnDefinition id = table.getColumnDefinitions().get(0);
        assertTrue(id.isSerialDefaultValue());

        ForeignKeyReference reference = table.getColumnDefinitions().get(1)
                .getForeignKeyReference();
        assertNotNull(reference);
        assertEquals("parent_record", reference.getTable().getName());
        assertEquals(Arrays.asList("id"), reference.getReferencedColumnNames());
        assertEquals(ForeignKeyReference.MatchType.FULL, reference.getMatchType());
        assertNotNull(reference.getReferentialAction(
                net.sf.jsqlparser.statement.ReferentialAction.Type.UPDATE));
        assertReparse(table);
    }

    @Test
    public void testNationalCharacterTypesAndOrderedModifiers() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE type_samples (label NATIONAL CHARACTER "
                + "VARYING(64), code NATIONAL CHAR(8), flags BIGINT ZEROFILL SIGNED UNSIGNED "
                + "SIGNED ZEROFILL)");

        assertEquals(ColDataType.NationalCharacterType.VARCHAR,
                table.getColumnDefinitions().get(0).getColDataType().getNationalCharacterType());
        assertEquals(ColDataType.NationalCharacterType.CHAR,
                table.getColumnDefinitions().get(1).getColDataType().getNationalCharacterType());
        assertEquals(Arrays.asList(ColDataType.TypeModifier.ZEROFILL,
                ColDataType.TypeModifier.SIGNED, ColDataType.TypeModifier.UNSIGNED,
                ColDataType.TypeModifier.SIGNED, ColDataType.TypeModifier.ZEROFILL),
                table.getColumnDefinitions().get(2).getColDataType().getTypeModifiers());
        assertReparse(table);
    }

    @Test
    public void testNationalCharacterAliases() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE localized_text (short_name NCHAR(16), "
                + "long_name NVARCHAR(255), legacy_name NCHAR VARCHAR(64))");

        assertEquals(ColDataType.NationalCharacterType.CHAR,
                table.getColumnDefinitions().get(0).getColDataType().getNationalCharacterType());
        assertEquals(ColDataType.NationalCharacterType.VARCHAR,
                table.getColumnDefinitions().get(1).getColDataType().getNationalCharacterType());
        assertEquals(ColDataType.NationalCharacterType.VARCHAR,
                table.getColumnDefinitions().get(2).getColDataType().getNationalCharacterType());
        assertReparse(table);
    }

    @Test
    public void testStructuredMySqlTableOptionsAndKeywordColumn() throws JSQLParserException {
        CreateTable table =
                parseMySql("CREATE TABLE log_entry (Position BIGINT, File VARCHAR(255)) "
                        + "ENGINE=CSV DEFAULT CHAR SET=utf8mb4");

        assertEquals("File", table.getColumnDefinitions().get(1).getColumnName());
        assertEquals(TableOption.Kind.ENGINE, table.getTableOptions().get(0).getKind());
        assertEquals("CSV", table.getTableOptions().get(0).getValue());
        assertEquals(TableOption.Kind.CHARACTER_SET, table.getTableOptions().get(1).getKind());
        assertEquals("utf8mb4", table.getTableOptions().get(1).getValue());
        assertReparse(table);
    }

    @Test
    public void testEscapedBacktickIdentifier() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE `odd``name` (`value``part` INT)");

        assertEquals("`odd``name`", table.getTable().getName());
        assertEquals("`value``part`", table.getColumnDefinitions().get(0).getColumnName());
        assertReparse(table);
    }

    @Test
    public void testTableForeignKeyUsesStructuredReference() throws JSQLParserException {
        CreateTable table = parseMySql("CREATE TABLE order_line (customer_id BIGINT, "
                + "CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(id) "
                + "MATCH FULL ON DELETE CASCADE)");

        ForeignKeyIndex foreignKey = (ForeignKeyIndex) table.getIndexes().get(0);
        assertEquals(Index.Kind.FOREIGN_KEY, foreignKey.getKind());
        assertEquals("customer", foreignKey.getReference().getTable().getName());
        assertEquals(ForeignKeyReference.MatchType.FULL, foreignKey.getMatchType());
        assertReparse(table);
    }

    @Test
    public void testAlterColumnUsesStructuredOptions() throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse("ALTER TABLE child_record ADD COLUMN "
                + "parent_id INT REFERENCES parent_record(id) MATCH FULL ON DELETE CASCADE",
                parser -> parser.withDialect(Dialect.MYSQL));

        AlterExpression.ColumnDataType column =
                alter.getAlterExpressions().get(0).getColDataTypeList().get(0);
        assertEquals("parent_record", column.getForeignKeyReference().getTable().getName());
        assertEquals(ForeignKeyReference.MatchType.FULL,
                column.getForeignKeyReference().getMatchType());
        CCJSqlParserUtil.parse(alter.toString(), parser -> parser.withDialect(Dialect.MYSQL));
    }

    @Test
    public void testCreateAndAlterTableIndexParity() throws JSQLParserException {
        CreateTable create = parseMySql("CREATE TABLE search_item (id INT, body TEXT, "
                + "PRIMARY KEY pk_search (id), FULLTEXT INDEX ft_body (body))");
        Alter alter = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE search_item ADD PRIMARY KEY pk_search (id), "
                        + "ADD FULLTEXT INDEX ft_body (body)",
                parser -> parser.withDialect(Dialect.MYSQL));

        assertEquals(create.getIndexes().get(0).getClass(),
                alter.getAlterExpressions().get(0).getIndex().getClass());
        assertEquals(create.getIndexes().get(0).getKind(),
                alter.getAlterExpressions().get(0).getIndex().getKind());
        assertEquals(create.getIndexes().get(0).toString(),
                alter.getAlterExpressions().get(0).getIndex().toString());
        assertEquals(create.getIndexes().get(1).toString(),
                alter.getAlterExpressions().get(1).getIndex().toString());
        CCJSqlParserUtil.parse(alter.toString(), parser -> parser.withDialect(Dialect.MYSQL));
    }

    private static CreateTable parseMySql(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL));
    }

    private static void assertReparse(CreateTable table) throws JSQLParserException {
        parseMySql(table.toString());
    }
}
