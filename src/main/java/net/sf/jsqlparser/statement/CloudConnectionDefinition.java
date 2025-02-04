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

public class CloudConnectionDefinition extends ConnectionDefinition {
    private String storage;

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    @Override
    public void setCertificateVerification(CertificateVerification certificateVerification) {}

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append("AT CLOUD ");
        sql.append(storage);
        appendConnectionDefinition(sql);

        return sql.toString();
    }
}
