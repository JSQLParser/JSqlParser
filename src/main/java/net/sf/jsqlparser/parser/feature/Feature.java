package net.sf.jsqlparser.parser.feature;

public enum Feature {

    upsert, insert, insertWithMulivalue, joinOldOracleSyntax, oraclePriorPosition;

    public boolean isEnabled() {
        return false;
    }


}
