package net.sf.jsqlparser.util.validation;

public interface ValidationCapability {

    /**
     * @param reference
     * @return the standard-error message for a reference of this
     *         {@link ValidationCapability}
     */
    String getErrorMessage(String reference);

}
