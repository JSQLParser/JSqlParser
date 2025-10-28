/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
module net.sf.jsqlparser {
    requires java.sql;
    requires java.logging;
    requires java.desktop;

    exports net.sf.jsqlparser;
    exports net.sf.jsqlparser.expression;
    exports net.sf.jsqlparser.expression.operators.arithmetic;
    exports net.sf.jsqlparser.expression.operators.conditional;
    exports net.sf.jsqlparser.expression.operators.relational;
    exports net.sf.jsqlparser.parser;
    exports net.sf.jsqlparser.parser.feature;
    exports net.sf.jsqlparser.schema;
    exports net.sf.jsqlparser.statement;
    exports net.sf.jsqlparser.statement.alter;
    exports net.sf.jsqlparser.statement.alter.sequence;
    exports net.sf.jsqlparser.statement.analyze;
    exports net.sf.jsqlparser.statement.comment;
    exports net.sf.jsqlparser.statement.create.function;
    exports net.sf.jsqlparser.statement.create.index;
    exports net.sf.jsqlparser.statement.create.procedure;
    exports net.sf.jsqlparser.statement.create.schema;
    exports net.sf.jsqlparser.statement.create.sequence;
    exports net.sf.jsqlparser.statement.create.synonym;
    exports net.sf.jsqlparser.statement.create.table;
    exports net.sf.jsqlparser.statement.create.view;
    exports net.sf.jsqlparser.statement.delete;
    exports net.sf.jsqlparser.statement.drop;
    exports net.sf.jsqlparser.statement.execute;
    exports net.sf.jsqlparser.statement.export;
    exports net.sf.jsqlparser.statement.grant;
    exports net.sf.jsqlparser.statement.imprt;
    exports net.sf.jsqlparser.statement.insert;
    exports net.sf.jsqlparser.statement.lock;
    exports net.sf.jsqlparser.statement.merge;
    exports net.sf.jsqlparser.statement.piped;
    exports net.sf.jsqlparser.statement.refresh;
    exports net.sf.jsqlparser.statement.select;
    exports net.sf.jsqlparser.statement.show;
    exports net.sf.jsqlparser.statement.truncate;
    exports net.sf.jsqlparser.statement.update;
    exports net.sf.jsqlparser.statement.upsert;
    exports net.sf.jsqlparser.util;
    exports net.sf.jsqlparser.util.cnfexpression;
    exports net.sf.jsqlparser.util.deparser;
    exports net.sf.jsqlparser.util.validation;
    exports net.sf.jsqlparser.util.validation.allowedtypes;
    exports net.sf.jsqlparser.util.validation.feature;
    exports net.sf.jsqlparser.util.validation.metadata;
    exports net.sf.jsqlparser.util.validation.validator;
    exports net.sf.jsqlparser.expression.json;
    exports net.sf.jsqlparser.statement.from;
}
