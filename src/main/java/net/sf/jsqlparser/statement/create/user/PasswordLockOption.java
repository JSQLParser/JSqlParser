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

/**
 * A MySQL account password-management or locking option. The option either has no argument, a
 * number ({@link #getValue()}) or a keyword ({@link #getSetting()}):
 *
 * <ul>
 * <li>{@code ACCOUNT LOCK}, {@code ACCOUNT UNLOCK}</li>
 * <li>{@code PASSWORD EXPIRE [DEFAULT | NEVER | INTERVAL n DAY]}</li>
 * <li>{@code PASSWORD HISTORY {DEFAULT | n}}</li>
 * <li>{@code PASSWORD REUSE INTERVAL {DEFAULT | n DAY}}</li>
 * <li>{@code PASSWORD REQUIRE CURRENT [DEFAULT | OPTIONAL]}</li>
 * <li>{@code FAILED_LOGIN_ATTEMPTS n}</li>
 * <li>{@code PASSWORD_LOCK_TIME {n | UNBOUNDED}}</li>
 * </ul>
 */
public class PasswordLockOption implements Serializable {

    public enum Kind {
        ACCOUNT_LOCK, ACCOUNT_UNLOCK, PASSWORD_EXPIRE, PASSWORD_HISTORY, PASSWORD_REUSE_INTERVAL, PASSWORD_REQUIRE_CURRENT, FAILED_LOGIN_ATTEMPTS, PASSWORD_LOCK_TIME
    }

    /** Keyword arguments of the options. */
    public enum Setting {
        DEFAULT, NEVER, OPTIONAL, UNBOUNDED
    }

    private Kind kind;
    private Long value;
    private Setting setting;

    public PasswordLockOption() {
        // empty constructor
    }

    public PasswordLockOption(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /** The count or number of days, or null when the option has no numeric argument. */
    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    /** The keyword argument, or null when the option has none. */
    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    @Override
    public String toString() {
        switch (kind) {
            case ACCOUNT_LOCK:
                return "ACCOUNT LOCK";
            case ACCOUNT_UNLOCK:
                return "ACCOUNT UNLOCK";
            case PASSWORD_EXPIRE:
                return "PASSWORD EXPIRE"
                        + (value != null ? " INTERVAL " + value + " DAY" : argument(""));
            case PASSWORD_HISTORY:
                return "PASSWORD HISTORY" + argument("");
            case PASSWORD_REUSE_INTERVAL:
                return "PASSWORD REUSE INTERVAL" + argument(" DAY");
            case PASSWORD_REQUIRE_CURRENT:
                return "PASSWORD REQUIRE CURRENT" + argument("");
            case FAILED_LOGIN_ATTEMPTS:
                return "FAILED_LOGIN_ATTEMPTS" + argument("");
            case PASSWORD_LOCK_TIME:
                return "PASSWORD_LOCK_TIME" + argument("");
            default:
                throw new IllegalStateException("Unsupported option: " + kind);
        }
    }

    private String argument(String unit) {
        if (setting != null) {
            return " " + setting;
        }
        return value != null ? " " + value + unit : "";
    }

    public PasswordLockOption withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public PasswordLockOption withValue(Long value) {
        setValue(value);
        return this;
    }

    public PasswordLockOption withSetting(Setting setting) {
        setSetting(setting);
        return this;
    }
}
