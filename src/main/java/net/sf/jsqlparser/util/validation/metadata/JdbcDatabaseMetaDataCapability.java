/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.util.validation.ValidationException;

/**
 * Validates against schema by jdbc-metadata in a very basic way without
 * meta-data-caching and comparing names by
 * {@link String#equalsIgnoreCase(String)}
 *
 * @author gitmotte
 *
 */
public class JdbcDatabaseMetaDataCapability extends AbstractDatabaseMetaDataCapability {

    public JdbcDatabaseMetaDataCapability(Connection connection) {
        super(connection);
    }

    @Override
    protected boolean viewExists(String name) throws ValidationException {
        return jdbcMetadataTables(name, new String[] { "VIEW" });
    }

    @Override
    protected boolean columnExists(String name) throws ValidationException {
        String[] names = name.split("\\.");
        if (names.length < 2 || names.length > 4) {
            return false;
        }

        String columnName = names[names.length - 1];
        int lastIndexOf = name.lastIndexOf(".");
        String query = String.format("SELECT %s FROM %s", columnName, name.substring(0, lastIndexOf));
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            return columnName.equalsIgnoreCase(ps.getMetaData().getColumnLabel(1));
        } catch (SQLException e) {
            throw createDatabaseException(name, new String[] { "COLUMN" }, e);
        }
    }

    @Override
    protected boolean tableExists(String name) throws ValidationException {
        return jdbcMetadataTables(name, new String[] { "TABLE" });
    }

    protected boolean jdbcMetadataTables(String name, String[] types) throws ValidationException {
        String[] names = name.split("\\.");
        if (names.length == 0 || names.length > 3) {
            return false;
        }

        String catalog = null;
        String schemaPattern = null;
        String tableNamePattern;
        if (names.length > 2) {
            catalog = names[0];
            schemaPattern = names[1];
            tableNamePattern = names[2];
        } else if (names.length > 1) {
            schemaPattern = names[0];
            tableNamePattern = names[1];
        } else {
            tableNamePattern = names[0];
        }


        List<String> tables = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getTables(catalog, schemaPattern, tableNamePattern, types)) {
            while (rs.next()) {
                String tableCat = rs.getString("TABLE_CAT");
                String tableSchem = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase(names[names.length - 1])) {
                    if (names.length > 1) {
                        if (tableSchem.equalsIgnoreCase(names[names.length - 2])) {
                            if (names.length > 2) {
                                if (tableCat.equalsIgnoreCase(names[names.length - 3])) {
                                    tables.add(String.join(".", tableCat, tableSchem, tableName));
                                }
                            } else {
                                tables.add(String.join(".",  tableSchem, tableName));
                            }
                        }
                    }  else {
                        tables.add(tableName);
                    }
                }
            }
        } catch (SQLException e) {
            throw createDatabaseException(name, types, e);
        }

        return !tables.isEmpty();
    }

    private DatabaseException createDatabaseException(String name, String[] types, SQLException e) {
        throw new DatabaseException("cannot evaluate existance of " + types + " by name '" + name + "'", e);
    }

}
