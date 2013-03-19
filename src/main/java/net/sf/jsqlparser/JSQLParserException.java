/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser;

/**
 * An exception class with stack trace informations
 */
public class JSQLParserException extends Exception {

	/* The serial class version */
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
