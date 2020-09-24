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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import net.sf.jsqlparser.util.validation.UnexpectedValidationException;
import net.sf.jsqlparser.util.validation.ValidationException;

/**
 * Validates against schema by jdbc-metadata in a very basic way with simple
 * caching and comparing names by {@link String#equalsIgnoreCase(String)}
 *
 * @author gitmotte
 *
 */
public class JdbcDatabaseMetaDataCapability extends AbstractDatabaseMetaDataCapability {

    /**
     * @param connection
     * @param namesLookup - see {@link NamesLookup}
     */
    public JdbcDatabaseMetaDataCapability(Connection connection, UnaryOperator<String> namesLookup) {
        super(connection, namesLookup);
    }

    /**
     * @param connection
     * @param namesLookup  - see {@link NamesLookup}
     * @param cacheResults - whether the results should be cached for later lookups
     */
    public JdbcDatabaseMetaDataCapability(Connection connection, UnaryOperator<String> namesLookup,
            boolean cacheResults) {
        super(connection, namesLookup, cacheResults);
    }

    @Override
    protected boolean viewExists(String name) throws ValidationException {
        return jdbcMetadataTables(NamedObject.view, name, new String[] { "VIEW" });
    }

    @Override
    protected boolean columnExists(String name) throws ValidationException {
        String[] names = splitAndValidateMinMax(NamedObject.column, name, 2, 4);
        String columnName = names[names.length - 1];
        int lastIndexOf = name.lastIndexOf(".");
        String fromClause = name.substring(0, lastIndexOf);
        String query = String.format("SELECT * FROM %s", fromClause);
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSetMetaData metaData = ps.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw createDatabaseException(name, new String[] { "COLUMN" }, e);
        }
        return false;
    }

    @Override
    protected boolean tableExists(String name) throws ValidationException {
        return jdbcMetadataTables(NamedObject.table, name, new String[] { "TABLE" });
    }

    protected boolean jdbcMetadataTables(NamedObject named, String name, String[] types) throws ValidationException {
        String[] names = splitAndValidateMinMax(named, name, 1, 3);

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

    /**
     * Split name by "." and validate expected path-elements
     *
     * @param named
     * @param name
     * @param min
     * @param max
     * @return
     */
    private String[] splitAndValidateMinMax(NamedObject named, String name, int min, int max) {
        String[] names = name.split("\\.");
        if (names.length < min || names.length > max) {
            throw new UnexpectedValidationException(String.format(
                    "%s has to much path-elements, needs to be between %s and %s for %s", name, min, max, named));
        }
        return names;
    }

    private DatabaseException createDatabaseException(String name, String[] types, SQLException e) {
        throw new DatabaseException(String.format(
                "cannot evaluate existence of %s by name '%s'", Arrays.toString(types), name), e);
    }

}
