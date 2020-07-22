package net.sf.jsqlparser.util.validation;

/**
 * can be used on unexpected errors during validation
 * 
 * @author gitmotte
 */
public class UnexpectedValidationException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public UnexpectedValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedValidationException(String message) {
        super(message);
    }

    public UnexpectedValidationException(Throwable cause) {
        super(cause);
    }
}
