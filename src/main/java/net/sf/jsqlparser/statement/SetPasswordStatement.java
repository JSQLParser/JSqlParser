/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * MySQL/MariaDB {@code SET PASSWORD [FOR user] {= password | TO RANDOM}
 * [REPLACE 'current'] [RETAIN CURRENT PASSWORD]}. The password is an expression, usually a string
 * literal; MariaDB also accepts {@code PASSWORD('...')}.
 */
public class SetPasswordStatement extends ASTNodeAccessImpl implements Statement {

    private UserAccount account;
    private Expression password;
    private boolean random;
    private StringValue currentCredential;
    private boolean retainCurrentPassword;

    /** The account after {@code FOR}, or null for the current user. */
    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    /** The password after {@code =}; null for {@code TO RANDOM}. */
    public Expression getPassword() {
        return password;
    }

    public void setPassword(Expression password) {
        this.password = password;
    }

    /** Whether the statement generates a password with {@code TO RANDOM}. */
    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    /** The current password of {@code REPLACE 'current'}, or null when omitted. */
    public StringValue getCurrentCredential() {
        return currentCredential;
    }

    public void setCurrentCredential(StringValue currentCredential) {
        this.currentCredential = currentCredential;
    }

    public boolean isRetainCurrentPassword() {
        return retainCurrentPassword;
    }

    public void setRetainCurrentPassword(boolean retainCurrentPassword) {
        this.retainCurrentPassword = retainCurrentPassword;
    }

    /** Shares statement rendering while letting deparsers render the password expression. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        builder.append("SET PASSWORD");
        if (account != null) {
            builder.append(" FOR ").append(account);
        }
        if (random) {
            builder.append(" TO RANDOM");
        } else {
            builder.append(" = ");
            expressionRenderer.accept(password);
        }
        if (currentCredential != null) {
            builder.append(" REPLACE ").append(currentCredential);
        }
        if (retainCurrentPassword) {
            builder.append(" RETAIN CURRENT PASSWORD");
        }
        return builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    public SetPasswordStatement withAccount(UserAccount account) {
        setAccount(account);
        return this;
    }

    public SetPasswordStatement withPassword(Expression password) {
        setPassword(password);
        return this;
    }

    public SetPasswordStatement withRandom(boolean random) {
        setRandom(random);
        return this;
    }

    public SetPasswordStatement withCurrentCredential(StringValue currentCredential) {
        setCurrentCredential(currentCredential);
        return this;
    }

    public SetPasswordStatement withRetainCurrentPassword(boolean retainCurrentPassword) {
        setRetainCurrentPassword(retainCurrentPassword);
        return this;
    }
}
