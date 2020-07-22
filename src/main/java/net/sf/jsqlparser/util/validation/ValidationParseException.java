package net.sf.jsqlparser.util.validation;

public class ValidationParseException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public ValidationParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationParseException(String message) {
        super(message);
    }

    public ValidationParseException(Throwable cause) {
        super(cause);
    }

}
