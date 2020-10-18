package net.sf.jsqlparser.statement.create.synonym;

import net.sf.jsqlparser.schema.Synonym;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.ArrayList;
import java.util.List;

public class CreateSynonym implements Statement {

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

    @Override
    public String toString() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE ");
        if(orReplace) {
            sqlBuilder.append("OR REPLACE ");
        }
        if(publicSynonym) {
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
