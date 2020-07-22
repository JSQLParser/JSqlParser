package net.sf.jsqlparser.util.validation.metadata;

import net.sf.jsqlparser.util.validation.ValidationException;

/**
 * database-errors wrapping a {@link SQLException} or PersistenceException
 * @author gitmotte
 */
public class DatabaseException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

}
