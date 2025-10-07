/*--- (C) 1999-2019 Techniker Krankenkasse ---*/

package net.sf.jsqlparser.statement.lock;

/**
 * Describes the LockMode of a LOCK TABLE-Statement.
 */
public enum LockMode {
	// These two modes are more common
	Share("SHARE"),
	Exclusive("EXCLUSIVE"),

	// These are only Oracle specific, as far as I know
	RowShare("ROW SHARE"),
	RowExclusive("ROW EXCLUSIVE"),
	ShareUpdate("SHARE UPDATE"),
	ShareRowExclusive("SHARE ROW EXCLUSIVE");

	private final String value;

	LockMode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}

/*--- Formatiert nach TK Code Konventionen vom 05.03.2002 ---*/
