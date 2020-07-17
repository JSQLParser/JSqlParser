package net.sf.jsqlparser.util.validation;

import java.sql.Connection;

/**
 * Validates against schema by jdbc-metadata
 * 
 * @author gitmotte
 *
 */
public class JdbcDatabaseMetaDataCapability extends AbstractDatabaseMetaDataCapability {

    public JdbcDatabaseMetaDataCapability(Connection connection) {
        super(connection);
    }

    @Override
    public boolean exists(NamedObject o, String name) {
        switch (o) {
        case table:
            return tableExists(name);
        case column:
            return columnExists(name);
        default:
        }
        throw new UnsupportedOperationException("cannot evaluate for " + o + " and " + name);
    }

    private boolean columnExists(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean tableExists(String name) {
        // TODO Auto-generated method stub
        return false;
    }

}
