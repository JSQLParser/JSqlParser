/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;

import java.io.Serializable;

public class ConnectionDefinition implements Serializable {
    private String connectionObjectName;
    private StringValue connectionDefinition;
    private UserIdentification userIdentification;
    private CertificateVerification certificateVerification;

    public String getConnectionObjectName() {
        return connectionObjectName;
    }

    public void setConnectionObjectName(String connectionObjectName) {
        this.connectionObjectName = connectionObjectName;
    }

    public StringValue getConnectionDefinition() {
        return connectionDefinition;
    }

    public void setConnectionDefinition(StringValue connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public void setConnection(String connectionObjectName) {
        this.connectionObjectName = connectionObjectName;
    }

    public void setConnection(StringValue connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public UserIdentification getUserIdentification() {
        return userIdentification;
    }

    public void setUserIdentification(UserIdentification userIdentification) {
        this.userIdentification = userIdentification;
    }

    public CertificateVerification getCertificateVerification() {
        return certificateVerification;
    }

    public void setCertificateVerification(CertificateVerification certificateVerification) {
        this.certificateVerification = certificateVerification;
    }

    protected StringBuilder appendConnectionDefinition(StringBuilder sql) {
        if (connectionObjectName != null) {
            sql.append(connectionObjectName);
        } else if (connectionDefinition != null) {
            sql.append(connectionDefinition);
        }

        if (userIdentification != null) {
            sql.append(" ");
            sql.append(userIdentification);
        }

        if (certificateVerification != null) {
            sql.append(" ");
            sql.append(certificateVerification);
        }

        return sql;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append("AT ");
        appendConnectionDefinition(sql);

        return sql.toString();
    }
}
