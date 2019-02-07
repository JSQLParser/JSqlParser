/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

public class TestException extends Exception {

    private Throwable cause = null;

    public TestException() {
        super();
    }

    public TestException(String arg0) {
        super(arg0);
    }

    public TestException(Throwable arg0) {
        this.cause = arg0;
    }

    public TestException(String arg0, Throwable arg1) {
        super(arg0);
        this.cause = arg1;
    }

    @Override
    public Throwable getCause() {
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
