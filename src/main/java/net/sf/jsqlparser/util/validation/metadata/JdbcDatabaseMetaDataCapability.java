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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

    private static final String VIEW = "VIEW";
    private static final String TABLE = "TABLE";
    private static final String COLUMN = "COLUMN";
    private static final Logger LOG = Logger.getLogger(JdbcDatabaseMetaDataCapability.class.getName());

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
    protected boolean columnExists(Map<Named, Boolean> results, Named named) throws ValidationException {
        String[] names = splitAndValidateMinMax(COLUMN, named.getFqnLookup(), 1, 4);
        String columnName = names[names.length - 1];

        List<String> possibleParents = null;
        List<NamedObject> parents = named.getParents().isEmpty() ? Arrays.asList(NamedObject.table)
                : named.getParents();

        int lastIndexOf = named.getFqnLookup().lastIndexOf(".");
        String fqnParent = lastIndexOf != -1 ? named.getFqnLookup().substring(0, lastIndexOf) : null;

        // try to match parents in results
        Predicate<? super Named> predicate = null;
        if (fqnParent != null) {
            predicate = n -> parents.contains(n.getNamedObject())
                    && (fqnParent.equals(n.getAliasLookup()) || fqnParent.equals(n.getFqnLookup()));
        } else {
            predicate = n -> parents.contains(n.getNamedObject());
        }
        possibleParents = results.keySet().stream().filter(predicate).map(Named::getFqnLookup)
                .collect(Collectors.toList());

        if (possibleParents.isEmpty()) {
            possibleParents = Collections.singletonList(fqnParent);
        }

        for (String fqn : possibleParents) {
            if (existsFromItem(results, fqn)) {
                String query = String.format("SELECT * FROM %s", fqn);
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ResultSetMetaData metaData = ps.getMetaData();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                            return true;
                        }
                    }
                } catch (SQLException e) {
                    throw createDatabaseException(fqn, COLUMN, e);
                }
            } else if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("%s does not exists, cannot evaluate COLUMN from %s", fqn, named.getFqn()));
            }
        }
        return false;
    }

    private boolean existsFromItem(Map<Named, Boolean> results, String fqn) {
        Named named = new Named(NamedObject.table, fqn).setFqnLookup(fqn);
        return viewExists(results, named) || tableExists(results, named);
    }

    @Override
    protected boolean viewExists(Map<Named, Boolean> results, Named named) throws ValidationException {
        return jdbcMetadataTables(named, VIEW);
    }

    @Override
    protected boolean tableExists(Map<Named, Boolean> results, Named named) throws ValidationException {
        return jdbcMetadataTables(named, TABLE);
    }

    protected boolean jdbcMetadataTables(Named named, String type) throws ValidationException {
        String[] names = splitAndValidateMinMax(type, named.getFqnLookup(), 1, 3);

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
        try (ResultSet rs = connection.getMetaData().getTables(catalog, schemaPattern, tableNamePattern,
                new String[] { type })) {
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
            throw createDatabaseException(named.getFqn(), type, e);
        }

        return !tables.isEmpty();
    }

    /**
     * Split fqn by "." and validate expected path-elements
     *
     * @param type
     * @param fqn
     * @param min
     * @param max
     * @return the fqn-parts
     */
    private String[] splitAndValidateMinMax(String type, String fqn, int min, int max) {
        String[] names = fqn.split("\\.");
        if (names.length < min || names.length > max) {
            throw new UnexpectedValidationException(String.format(
                    "%s path-elements count needs to be between %s and %s for %s", fqn, min, max, type));
        }
        return names;
    }

    private DatabaseException createDatabaseException(String fqn, String type, SQLException e) {
        return new DatabaseException(String.format(
                "cannot evaluate existence of %s by name '%s'", type, fqn), e);
    }

}
