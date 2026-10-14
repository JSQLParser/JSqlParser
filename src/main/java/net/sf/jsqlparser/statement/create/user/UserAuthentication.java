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
import net.sf.jsqlparser.expression.StringValue;

/**
 * Structured authentication clause of a MySQL user account. {@code ALTER USER} adds
 * {@code REPLACE 'current'} and {@code RETAIN CURRENT PASSWORD} to a password assignment, and
 * {@code DISCARD OLD PASSWORD} as a mode of its own.
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
    private String plugin;
    private StringValue credential;
    private StringValue currentCredential;
    private boolean retainCurrentPassword;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public StringValue getCredential() {
        return credential;
    }

    public void setCredential(StringValue credential) {
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
                return "IDENTIFIED WITH " + plugin;
            case WITH_PLUGIN_BY_PASSWORD:
                return "IDENTIFIED WITH " + plugin + " BY " + credential;
            case WITH_PLUGIN_BY_RANDOM_PASSWORD:
                return "IDENTIFIED WITH " + plugin + " BY RANDOM PASSWORD";
            case WITH_PLUGIN_AS_STRING:
                return "IDENTIFIED WITH " + plugin + " AS " + credential;
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

    public UserAuthentication withCredential(StringValue credential) {
        setCredential(credential);
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
