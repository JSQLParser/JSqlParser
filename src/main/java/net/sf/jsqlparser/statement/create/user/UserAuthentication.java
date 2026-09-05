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

/** Structured authentication clause of a MySQL user account. */
public class UserAuthentication implements Serializable {

    public enum Mode {
        BY_PASSWORD, BY_RANDOM_PASSWORD, WITH_PLUGIN, WITH_PLUGIN_BY_PASSWORD, WITH_PLUGIN_BY_RANDOM_PASSWORD, WITH_PLUGIN_AS_STRING
    }

    private Mode mode;
    private String plugin;
    private StringValue credential;

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

    @Override
    public String toString() {
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
}
