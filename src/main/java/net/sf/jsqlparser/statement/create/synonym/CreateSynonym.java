/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.synonym;

import net.sf.jsqlparser.schema.Synonym;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.ArrayList;
import java.util.List;

public class CreateSynonym implements Statement {

    public Synonym synonym;
    private boolean orReplace;
    private boolean publicSynonym;
    private List<String> forList = new ArrayList<>();

    public Synonym getSynonym() {
        return synonym;
    }

    public void setSynonym(Synonym synonym) {
        this.synonym = synonym;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public boolean isPublicSynonym() {
        return publicSynonym;
    }

    public void setPublicSynonym(boolean publicSynonym) {
        this.publicSynonym = publicSynonym;
    }

    public List<String> getForList() {
        return forList;
    }

    public void setForList(List<String> forList) {
        this.forList = forList;
    }

    public String getFor() {
        StringBuilder b = new StringBuilder();
        for (String name : forList) {
            if (b.length() > 0) {
                b.append(".");
            }
            b.append(name);
        }
        return b.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE ");
        if (orReplace) {
            sqlBuilder.append("OR REPLACE ");
        }
        if (publicSynonym) {
            sqlBuilder.append("PUBLIC ");
        }
        sqlBuilder.append("SYNONYM " + synonym);
        sqlBuilder.append(' ');
        sqlBuilder.append("FOR " + getFor());
        return sqlBuilder.toString();
    }

    public CreateSynonym withSynonym(Synonym synonym) {
        this.setSynonym(synonym);
        return this;
    }
}
