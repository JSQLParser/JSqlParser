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

import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class FileSource extends ImportFromItem implements Serializable {
    private DataSource dataSource;
    private List<ConnectionFileDefinition> connectionFileDefinitions;
    private Boolean local;
    private Boolean secure;
    private List<CSVColumn> csvColumns;
    private List<FBVColumn> fbvColumns;
    private List<FileOption> fileOptions;
    private CertificateVerification certificateVerification;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ConnectionFileDefinition> getConnectionFileDefinitions() {
        return connectionFileDefinitions;
    }

    public void setConnectionFileDefinitions(List<ConnectionFileDefinition> connectionFileDefinitions) {
        this.connectionFileDefinitions = connectionFileDefinitions;
    }

    public Boolean isLocal() {
        return local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public Boolean isSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public List<CSVColumn> getCSVColumns() {
        return csvColumns;
    }

    public void setCSVColumns(List<CSVColumn> csvColumns) {
        this.csvColumns = csvColumns;
    }

    public List<FBVColumn> getFBVColumns() {
        return fbvColumns;
    }

    public void setFBVColumns(List<FBVColumn> fbvColumns) {
        this.fbvColumns = fbvColumns;
    }

    public List<FileOption> getFileOptions() {
        return fileOptions;
    }

    public void setFileOptions(List<FileOption> fileOptions) {
        this.fileOptions = fileOptions;
    }

    public CertificateVerification getCertificateVerification() {
        return certificateVerification;
    }

    public void setCertificateVerification(CertificateVerification certificateVerification) {
        this.certificateVerification = certificateVerification;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        if (local != null) {
            if (local) {
                sql.append("LOCAL ");
            }

            if (Objects.requireNonNullElse(secure, false)) {
                sql.append("SECURE ");
            }
        }

        sql.append(dataSource);
        if (connectionFileDefinitions != null) {
            sql.append(" ");
            PlainSelect.appendStringListTo(sql, connectionFileDefinitions, false, false);
        }

        if (csvColumns != null) {
            sql.append(" ");
            PlainSelect.appendStringListTo(sql, csvColumns, true, true);
        } else if (fbvColumns != null) {
            sql.append(" ");
            PlainSelect.appendStringListTo(sql, fbvColumns, false, true);
        }

        if (fileOptions != null) {
            sql.append(" ");
            PlainSelect.appendStringListTo(sql, fileOptions, false, false);
        }

        if (certificateVerification != null) {
            sql.append(" ");
            sql.append(certificateVerification);
        }

        if (errorClause != null) {
            sql.append(" ");
            sql.append(errorClause);
        }

        return sql.toString();
    }
}
