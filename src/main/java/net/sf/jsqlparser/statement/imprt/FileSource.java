/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.imprt;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;
import java.util.List;

public class FileSource implements ImportFromItem, Serializable {
    private DataSource dataSource;
    private ConnectionDefinition connectionDefinition;
    private List<ConnectionFileDefinition> connectionFileDefinitions;
    private Boolean local;
    private Boolean secure;
    private List<CSVColumn> csvColumns;
    private List<FBVColumn> fbvColumns;
    private List<FileOption> fileOptions;
    private CertificateVerification certificateVerification;

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        if (local != null) {
            if (local) {
                sql.append("LOCAL ");
            }

            if (secure) {
                sql.append("SECURE ");
            }
        }

        sql.append(dataSource);
        PlainSelect.appendStringListTo(sql, connectionFileDefinitions, false, false);

        if (csvColumns != null) {
            PlainSelect.appendStringListTo(sql, csvColumns, true, true);
        } else if (fbvColumns != null) {
            PlainSelect.appendStringListTo(sql, fbvColumns, false, true);
        }

        if (fileOptions != null) {
            PlainSelect.appendStringListTo(sql, fileOptions, false, false);
        }

        if(certificateVerification != null) {
            sql.append(certificateVerification);
        }

        return sql.toString();
    }
}
