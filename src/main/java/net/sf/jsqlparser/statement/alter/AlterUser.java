/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 *
 * @author dotan
 */
public class AlterUser extends Statement.Default {

    private boolean isModify = false; // MODIFY USER - Teradata 
    private boolean isLogin;
    private String userName;
    private String password;
    private boolean isIdentified = false;
    private Expression withPart = null;
    private boolean passwordHashed = false;

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        b.append(isModify() ? "MODIFY " : "ALTER ").append(isLogin() ? "LOGIN " : "USER ")
            .append(getUserName()).append(" ");

        if (isIdentified()) {
            b.append("IDENTIFIED ");

            if (getWithPart() != null) {
                b.append("WITH ").append(getWithPart());
            }

            b.append("BY ").append(password);
        } else {
            if (isModify) {
                b.append("AS PASSWORD=").append(password);
            } else {
                b.append("WITH PASSWORD=").append(password);
                if (isPasswordHashed()) {
                    b.append(" HASHED");
                }
            }
        }

        return b.toString();
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Expression getWithPart() {
        return withPart;
    }

    public void setWithPart(Expression withPart) {
        this.withPart = withPart;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordHashed() {
        return passwordHashed;
    }

    public void setPasswordHashed(boolean passwordHashed) {
        this.passwordHashed = passwordHashed;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public boolean isIdentified() {
        return isIdentified;
    }

    public void setIdentified(boolean isIdentified) {
        this.isIdentified = isIdentified;
    }

    public boolean isModify() {
        return isModify;
    }

    public void setModify(boolean isModify) {
        this.isModify = isModify;
    }

}
