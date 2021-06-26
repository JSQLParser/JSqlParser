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

/**
 *
 * @author are
 */
public enum AlterSessionOperation {
        ADVISE_COMMIT
        , ADVISE_ROLLBACK
        , ADVISE_NOTHING
        , CLOSE_DATABASE_LINK
        , ENABLE_COMMIT_IN_PROCEDURE
        , DISABLE_COMMIT_IN_PROCEDURE
        , ENABLE_GUARD
        , DISABLE_GUARD
        , ENABLE_PARALLEL_DML
        , DISABLE_PARALLEL_DML
        , FORCE_PARALLEL_DML
        , ENABLE_PARALLEL_DDL
        , DISABLE_PARALLEL_DDL
        , FORCE_PARALLEL_DDL
        , ENABLE_PARALLEL_QUERY
        , DISABLE_PARALLEL_QUERY
        , FORCE_PARALLEL_QUERY
        , ENABLE_RESUMABLE
        , DISABLE_RESUMABLE
        , SET
    }
