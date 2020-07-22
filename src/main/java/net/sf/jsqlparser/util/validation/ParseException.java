package net.sf.jsqlparser.util.validation;

import net.sf.jsqlparser.JSQLParserException;

/**
 * wraps a {@link JSQLParserException} to add to the errors collected by
 * validation
 * 
 * @author gitmotte
 */
public class ParseException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

}
