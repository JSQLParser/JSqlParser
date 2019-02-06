/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser;

public class JSQLParserException extends Exception {

    private static final long serialVersionUID = -1099039459759769980L;
    private Throwable cause = null;

    public JSQLParserException() {
        super();
    }

    public JSQLParserException(String arg0) {
        super(arg0);
    }

    public JSQLParserException(Throwable arg0) {
        this.cause = arg0;
    }

    public JSQLParserException(String arg0, Throwable arg1) {
        super(arg0);
        this.cause = arg1;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(java.io.PrintWriter pw) {
        super.printStackTrace(pw);
        if (cause != null) {
            pw.println("Caused by:");
            cause.printStackTrace(pw);
        }
    }

    @Override
    public void printStackTrace(java.io.PrintStream ps) {
        super.printStackTrace(ps);
        if (cause != null) {
            ps.println("Caused by:");
            cause.printStackTrace(ps);
        }
    }
}
