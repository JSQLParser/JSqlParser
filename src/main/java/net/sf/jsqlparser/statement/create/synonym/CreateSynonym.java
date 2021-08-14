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
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.ArrayList;
import java.util.List;

public class CreateSynonym extends DDLStatement {

    private boolean orReplace;
    private boolean publicSynonym;
    public Synonym synonym;
    private List<String> forList = new ArrayList<>();

    public void setSynonym(Synonym synonym) {
        this.synonym = synonym;
    }

    public Synonym getSynonym() {
        return synonym;
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

    public void setForList(List<String> forList) {
        this.forList = forList;
    }

    public List<String> getForList() {
        return forList;
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
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public CreateSynonym withSynonym(Synonym synonym) {
        this.setSynonym(synonym);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {

        builder.append("CREATE ");
        if (orReplace) {
            builder.append("OR REPLACE ");
        }
        if (publicSynonym) {
            builder.append("PUBLIC ");
        }
        builder.append("SYNONYM ").append(synonym);
        builder.append(' ');
        builder.append("FOR ").append(getFor());

        return builder;
    }
}
