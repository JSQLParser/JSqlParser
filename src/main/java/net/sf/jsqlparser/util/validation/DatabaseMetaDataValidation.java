package net.sf.jsqlparser.util.validation;

public interface DatabaseMetaDataValidation extends ValidationCapability {

    public enum NamedObject {
        database, schema, table, column, index, constraint
    }

    /**
     * @param o
     * @param fqn - fully qualified name
     * @return <code>true</code>, if name is not found within the database metadata
     */
    default boolean isNotValid(NamedObject o, String fqn) {
        try {
            return !exists(o, fqn);
        } catch (UnsupportedOperationException uoe) {
            return false;
        }
    }

    public boolean exists(NamedObject o, String fqn);

    @Override
    default String getErrorMessage(String fqn) {
        return fqn + " does not exist.";
    }

}
