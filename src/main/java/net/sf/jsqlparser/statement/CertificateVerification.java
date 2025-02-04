package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;

public class CertificateVerification {
    private Boolean ignoreCertificate;
    private StringValue publicKey;

    public StringValue getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(StringValue publicKey) {
        this.publicKey = publicKey;
    }

    public Boolean getIgnoreCertificate() {
        return ignoreCertificate;
    }

    public void setIgnoreCertificate(Boolean ignoreCertificate) {
        this.ignoreCertificate = ignoreCertificate;
    }

    public Boolean getVerifyCertificate() {
        return !ignoreCertificate;
    }

    public void setVerifyCertificate(Boolean verifyCertificate) {
        this.ignoreCertificate = !verifyCertificate;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        if (ignoreCertificate != null) {
            if (ignoreCertificate) {
               sql.append("IGNORE ");
            } else {
                sql.append("VERIFY ");
            }
            sql.append("CERTIFICATE");
        }

        if (publicKey != null) {
            if (ignoreCertificate != null) {
                sql.append(" ");
            }
            sql.append("PUBLIC KEY ");
            sql.append(publicKey);
        }

        return sql.toString();
    }
}
