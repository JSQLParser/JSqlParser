package net.sf.jsqlparser.util.validation;

import java.sql.Connection;

public abstract class AbstractDatabaseMetaDataCapability implements DatabaseMetaDataValidation {

    protected Connection connection;

    public AbstractDatabaseMetaDataCapability(Connection connection) {
        this.connection = connection;
    }

}
