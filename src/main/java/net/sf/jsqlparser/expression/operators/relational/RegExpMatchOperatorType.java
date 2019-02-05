/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

/**
 * PostgresSQL match operators.
 *
 * @author toben
 */
public enum RegExpMatchOperatorType {
    MATCH_CASESENSITIVE,
    MATCH_CASEINSENSITIVE,
    NOT_MATCH_CASESENSITIVE,
    NOT_MATCH_CASEINSENSITIVE
}
