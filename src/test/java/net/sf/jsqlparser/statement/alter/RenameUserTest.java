/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RenameUserTest {

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "MARIADB"})
    void renamesAccounts(Dialect dialect) throws JSQLParserException {
        String sql = "RENAME USER 'a'@'%' TO 'b'@'%', old_name TO `new name`@localhost";
        RenameUser renameUser = assertInstanceOf(RenameUser.class,
                TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false,
                        parser -> parser.withDialect(dialect)));
        assertEquals(sql, CCJSqlParserUtil
                .parse(renameUser.toString(), parser -> parser.withDialect(dialect)).toString());

        List<RenameUser.Rename> renames = renameUser.getRenames();
        assertEquals(2, renames.size());
        assertEquals("a", renames.get(0).getFrom().getUser().getValue());
        assertEquals("%", renames.get(0).getTo().getHost().getValue());
        assertEquals("old_name", renames.get(1).getFrom().toString());
        assertEquals("new name", renames.get(1).getTo().getUser().getValue());
        assertEquals("`", renames.get(1).getTo().getUser().getQuoteStr());
        assertEquals("localhost", renames.get(1).getTo().getHost().getValue());
    }

    @Test
    void keepsTableRenames() throws JSQLParserException {
        assertInstanceOf(RenameTableStatement.class,
                TestUtils.assertSqlCanBeParsedAndDeparsed("RENAME TABLE a TO b"));
        // a table named user, renamed without the optional TABLE keyword
        assertInstanceOf(RenameTableStatement.class, CCJSqlParserUtil.parse("RENAME user TO b"));
    }

    @Test
    void buildsAndDeparses() {
        RenameUser renameUser = new RenameUser().addRename(
                new UserAccount().withUser(new StringValue("a")).withHost(new StringValue("%")),
                new UserAccount().withUser(new StringValue("b")).withHost(new StringValue("%")));

        assertDeparse(renameUser, "RENAME USER 'a'@'%' TO 'b'@'%'");
        renameUser.setRenames(null);
        assertEquals(0, renameUser.getRenames().size());
    }

    @Test
    void reportsSchemaChangeWithoutTables() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("RENAME USER a TO b");

        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(TablesNamesFinder.findTables(statement.toString())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"RENAME USER a", "RENAME USER a TO",
            "RENAME USER a IDENTIFIED BY 'x' TO b", "RENAME USER a TO b,"})
    void rejectsIncompleteRenames(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }
}
