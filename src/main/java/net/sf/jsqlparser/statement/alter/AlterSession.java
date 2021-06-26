/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.statement.alter;

import java.util.List;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 *
 * @author are
 * @@link  https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_2012.htm
 */
public class AlterSession implements Statement  {
    private AlterSessionOperation operation;
    private List<String> parameters;

    public AlterSession(AlterSessionOperation operation, List<String> parameters) {
        this.operation = operation;
        this.parameters = parameters;
    }

    public AlterSessionOperation getOperation() {
        return operation;
    }

    public void setOperation(AlterSessionOperation operation) {
        this.operation = operation;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
    private static void appendParamaters(StringBuilder builder, List<String> parameters) {
        for (String s: parameters) {
            builder.append(" ").append(s);
        }
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.CyclomaticComplexity"})  
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ALTER SESSION ");
        switch (operation) {
            case ADVISE_COMMIT:
                builder.append("ADVISE COMMIT");
                break;
            case ADVISE_ROLLBACK:
                builder.append("ADVISE ROLLBACK");
                break;
            case ADVISE_NOTHING:
                builder.append("ADVISE NOTHING");
                break;
            case CLOSE_DATABASE_LINK:
                builder.append("CLOSE DATABASE LINK ");
                appendParamaters(builder, parameters);
                break;
            case ENABLE_COMMIT_IN_PROCEDURE:
                builder.append("ENABLE COMMIT IN PROCEDURE");
                break;
            case DISABLE_COMMIT_IN_PROCEDURE:
                builder.append("DISABLE COMMIT IN PROCEDURE");
                break;
            case ENABLE_GUARD:
                builder.append("ENABLE GUARD");
                break;
            case DISABLE_GUARD:
                builder.append("DISABLE GUARD");
                break;
            
            case ENABLE_PARALLEL_DML:
                builder.append("ENABLE PARALLEL DML");
                appendParamaters(builder, parameters);
                break;
                
            case DISABLE_PARALLEL_DML:
                builder.append("DISABLE PARALLEL DML");
                appendParamaters(builder, parameters);
                break;
                
            case FORCE_PARALLEL_DML:
                builder.append("FORCE PARALLEL DML");
                appendParamaters(builder, parameters);
                break;
                
            case ENABLE_PARALLEL_DDL:
                builder.append("ENABLE PARALLEL DDL");
                appendParamaters(builder, parameters);
                break;
                
            case DISABLE_PARALLEL_DDL:
                builder.append("DISABLE PARALLEL DDL");
                break;
                
            case FORCE_PARALLEL_DDL:
                builder.append("FORCE PARALLEL DDL");
                appendParamaters(builder, parameters);
                break;
                
            case ENABLE_PARALLEL_QUERY:
                builder.append("ENABLE PARALLEL QUERY");
                appendParamaters(builder, parameters);
                break;
                
            case DISABLE_PARALLEL_QUERY:
                builder.append("DISABLE PARALLEL QUERY");
                break;
                
            case FORCE_PARALLEL_QUERY:
                builder.append("FORCE PARALLEL QUERY");
                appendParamaters(builder, parameters);
                break;
                
            case ENABLE_RESUMABLE:
                builder.append("ENABLE RESUMABLE");
                appendParamaters(builder, parameters);
                break;
            
            case DISABLE_RESUMABLE:
                builder.append("DISABLE RESUMABLE");
                break;
            
            case SET:
                builder.append("SET");
                appendParamaters(builder, parameters);
                break;
            default:
                // not going to happen
                
        }
        return builder.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }
}
