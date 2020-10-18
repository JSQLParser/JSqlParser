package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Synonym extends ASTNodeAccessImpl implements MultiPartName {

    private static final int NAME_IDX = 0;
    private static final int SCHEMA_IDX = 1;
    private static final int DATABASE_IDX = 2;
    private static final int SERVER_IDX = 3;
    private List<String> partItems = new ArrayList<>();

    public Synonym() {
    }

    public Synonym(List<String> partItems) {
        this.partItems = new ArrayList<>(partItems);
        Collections.reverse(this.partItems);
    }

    public Database getDatabase() {
        return new Database(getIndex(DATABASE_IDX));
    }

    public void setDatabase(Database database) {
        setIndex(DATABASE_IDX, database.getDatabaseName());
        if (database.getServer() != null) {
            setIndex(SERVER_IDX, database.getServer().getFullyQualifiedName());
        }
    }

    public Synonym withDatabase(Database database) {
        setDatabase(database);
        return this;
    }

    public String getSchemaName() {
        return getIndex(SCHEMA_IDX);
    }

    public void setSchemaName(String string) {
        setIndex(SCHEMA_IDX, string);
    }

    public Synonym withSchemaName(String string) {
        setSchemaName(string);
        return this;
    }

    public String getName() {
        return getIndex(NAME_IDX);
    }

    public void setName(String string) {
        setIndex(NAME_IDX, string);
    }

    public Synonym withName(String string) {
        setName(string);
        return this;
    }

    private void setIndex(int idx, String value) {
        int size = partItems.size();
        for (int i = 0; i < idx - size + 1; i++) {
            partItems.add(null);
        }
        partItems.set(idx, value);
    }

    private String getIndex(int idx) {
        if (idx < partItems.size()) {
            return partItems.get(idx);
        } else {
            return null;
        }
    }

    @Override
    public String getFullyQualifiedName() {
        StringBuilder fqn = new StringBuilder();

        for (int i = partItems.size() - 1; i >= 0; i--) {
            String part = partItems.get(i);
            if (part == null) {
                part = "";
            }
            fqn.append(part);
            if (i != 0) {
                fqn.append(".");
            }
        }

        return fqn.toString();
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder(getFullyQualifiedName());
        return sql.toString();
    }
}
