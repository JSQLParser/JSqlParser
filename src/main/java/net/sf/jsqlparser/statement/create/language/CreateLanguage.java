/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.language;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL CREATE [OR REPLACE] [TRUSTED] [PROCEDURAL] LANGUAGE, including the handlerless form.
 */
public class CreateLanguage extends ASTNodeAccessImpl implements Statement {
    private boolean orReplace;
    private boolean trusted;
    private boolean procedural;
    private String name;
    private String handler;
    private String inlineHandler;
    private String validator;
    private boolean noValidator;

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public CreateLanguage withOrReplace(boolean orReplace) {
        setOrReplace(orReplace);
        return this;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public CreateLanguage withTrusted(boolean trusted) {
        setTrusted(trusted);
        return this;
    }

    public boolean isProcedural() {
        return procedural;
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    public CreateLanguage withProcedural(boolean procedural) {
        setProcedural(procedural);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateLanguage withName(String name) {
        setName(name);
        return this;
    }

    public String getHandler() {
        return handler;
    }

    /** Removing HANDLER also removes its optional INLINE and VALIDATOR clauses. */
    public void setHandler(String handler) {
        this.handler = handler;
        if (handler == null) {
            inlineHandler = null;
            validator = null;
            noValidator = false;
        }
    }

    public CreateLanguage withHandler(String handler) {
        setHandler(handler);
        return this;
    }

    public String getInlineHandler() {
        return inlineHandler;
    }

    public void setInlineHandler(String inlineHandler) {
        this.inlineHandler = inlineHandler;
    }

    public CreateLanguage withInlineHandler(String inlineHandler) {
        setInlineHandler(inlineHandler);
        return this;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
        if (validator != null) {
            noValidator = false;
        }
    }

    public CreateLanguage withValidator(String validator) {
        setValidator(validator);
        return this;
    }

    /** Whether an explicit NO VALIDATOR is written, rather than an omitted validator. */
    public boolean isNoValidator() {
        return noValidator;
    }

    public void setNoValidator(boolean noValidator) {
        this.noValidator = noValidator;
        if (noValidator) {
            validator = null;
        }
    }

    public CreateLanguage withNoValidator(boolean noValidator) {
        setNoValidator(noValidator);
        return this;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("CREATE ");
        if (orReplace) {
            sql.append("OR REPLACE ");
        }
        if (trusted) {
            sql.append("TRUSTED ");
        }
        if (procedural) {
            sql.append("PROCEDURAL ");
        }
        sql.append("LANGUAGE ").append(name);
        if (handler != null) {
            sql.append(" HANDLER ").append(handler);
            if (inlineHandler != null) {
                sql.append(" INLINE ").append(inlineHandler);
            }
            if (noValidator) {
                sql.append(" NO VALIDATOR");
            } else if (validator != null) {
                sql.append(" VALIDATOR ").append(validator);
            }
        }
        return sql;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
