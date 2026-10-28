/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.user;

import java.io.Serializable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.StringValue;

/**
 * Structured authentication clause of a MySQL user account. {@code ALTER USER} adds
 * {@code REPLACE 'current'} and {@code RETAIN CURRENT PASSWORD} to a password assignment, and
 * {@code DISCARD OLD PASSWORD} as a mode of its own. The plugin name keeps its quote character, as
 * in {@code IDENTIFIED WITH 'caching_sha2_password'}, and the hash of {@code AS} may be a
 * hexadecimal literal, as SHOW CREATE USER prints it.
 */
public class UserAuthentication implements Serializable {

    public enum Mode {
        BY_PASSWORD, BY_RANDOM_PASSWORD, WITH_PLUGIN, WITH_PLUGIN_BY_PASSWORD, WITH_PLUGIN_BY_RANDOM_PASSWORD, WITH_PLUGIN_AS_STRING, DISCARD_OLD_PASSWORD;

        /** Whether this mode assigns a new password, which REPLACE and RETAIN may follow. */
        public boolean isPasswordAssignment() {
            return this == BY_PASSWORD || this == BY_RANDOM_PASSWORD
                    || this == WITH_PLUGIN_BY_PASSWORD || this == WITH_PLUGIN_BY_RANDOM_PASSWORD;
        }
    }

    private Mode mode;
    private StringValue pluginName;
    private Expression credential;
    private StringValue currentCredential;
    private boolean retainCurrentPassword;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /** The plugin name without its quotes, or null without {@code IDENTIFIED WITH}. */
    public String getPlugin() {
        return pluginName == null ? null : pluginName.getValue();
    }

    /** Sets an unquoted plugin name. */
    public void setPlugin(String plugin) {
        this.pluginName =
                plugin == null ? null : new StringValue().withValue(plugin).setQuoteStr("");
    }

    /** The plugin name with its quote character, which is empty for an unquoted name. */
    public StringValue getPluginName() {
        return pluginName;
    }

    public void setPluginName(StringValue pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * The password or hash as a string literal, or null when there is none or the hash of
     * {@code AS} is a hexadecimal literal; {@link #getCredentialValue()} returns either.
     */
    public StringValue getCredential() {
        return credential instanceof StringValue ? (StringValue) credential : null;
    }

    public void setCredential(StringValue credential) {
        this.credential = credential;
    }

    /** The password or hash as written: a {@link StringValue}, or a {@link HexValue} after AS. */
    public Expression getCredentialValue() {
        return credential;
    }

    public void setCredentialValue(Expression credential) {
        if (credential != null && !(credential instanceof StringValue)
                && !(credential instanceof HexValue)) {
            throw new IllegalArgumentException(
                    "A credential is a string or hexadecimal literal: " + credential);
        }
        this.credential = credential;
    }

    /** The current password of {@code REPLACE 'current'}, or null when omitted. */
    public StringValue getCurrentCredential() {
        return currentCredential;
    }

    public void setCurrentCredential(StringValue currentCredential) {
        this.currentCredential = currentCredential;
    }

    /** Whether {@code RETAIN CURRENT PASSWORD} keeps the replaced password as a secondary one. */
    public boolean isRetainCurrentPassword() {
        return retainCurrentPassword;
    }

    public void setRetainCurrentPassword(boolean retainCurrentPassword) {
        this.retainCurrentPassword = retainCurrentPassword;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(modeToString());
        if (currentCredential != null) {
            builder.append(" REPLACE ").append(currentCredential);
        }
        if (retainCurrentPassword) {
            builder.append(" RETAIN CURRENT PASSWORD");
        }
        return builder.toString();
    }

    private String modeToString() {
        switch (mode) {
            case BY_PASSWORD:
                return "IDENTIFIED BY " + credential;
            case BY_RANDOM_PASSWORD:
                return "IDENTIFIED BY RANDOM PASSWORD";
            case WITH_PLUGIN:
                return "IDENTIFIED WITH " + pluginName;
            case WITH_PLUGIN_BY_PASSWORD:
                return "IDENTIFIED WITH " + pluginName + " BY " + credential;
            case WITH_PLUGIN_BY_RANDOM_PASSWORD:
                return "IDENTIFIED WITH " + pluginName + " BY RANDOM PASSWORD";
            case WITH_PLUGIN_AS_STRING:
                return "IDENTIFIED WITH " + pluginName + " AS " + credential;
            case DISCARD_OLD_PASSWORD:
                return "DISCARD OLD PASSWORD";
            default:
                throw new IllegalStateException("Unsupported authentication mode: " + mode);
        }
    }

    public UserAuthentication withMode(Mode mode) {
        setMode(mode);
        return this;
    }

    public UserAuthentication withPlugin(String plugin) {
        setPlugin(plugin);
        return this;
    }

    public UserAuthentication withPluginName(StringValue pluginName) {
        setPluginName(pluginName);
        return this;
    }

    public UserAuthentication withCredential(StringValue credential) {
        setCredential(credential);
        return this;
    }

    public UserAuthentication withCredentialValue(Expression credential) {
        setCredentialValue(credential);
        return this;
    }

    public UserAuthentication withCurrentCredential(StringValue currentCredential) {
        setCurrentCredential(currentCredential);
        return this;
    }

    public UserAuthentication withRetainCurrentPassword(boolean retainCurrentPassword) {
        setRetainCurrentPassword(retainCurrentPassword);
        return this;
    }
}
