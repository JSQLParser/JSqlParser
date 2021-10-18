package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class KeywordsTest {

    @Test
    public void testRelObjectNameWithoutValue() throws JSQLParserException {

        // JEDIT REGEX + BEANSHELL REPLACE
        // SEARCH FOR:
        // \|\s*tk=\<K_(.*)\
        // REPLACE WITH:
        // "assertSqlCanBeParsedAndDeparsed(\"SELECT " + _1.toLowerCase() + " AS " + _1.toLowerCase() + " from " + _1.toLowerCase() + "." + _1.toLowerCase() + "\", true);"

        /* assertSqlCanBeParsedAndDeparsed("SELECT absent AS absent from absent.absent", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT action.action AS action from action.action", true);
        assertSqlCanBeParsedAndDeparsed("SELECT active AS active from active.active", true);
        assertSqlCanBeParsedAndDeparsed("SELECT add AS add from add.add", true);
        assertSqlCanBeParsedAndDeparsed("SELECT advance AS advance from advance.advance", true);
        assertSqlCanBeParsedAndDeparsed("SELECT advise AS advise from advise.advise", true);
        assertSqlCanBeParsedAndDeparsed("SELECT against AS against from against.against", true);
        assertSqlCanBeParsedAndDeparsed("SELECT algorithm AS algorithm from algorithm.algorithm", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT all AS all from all.all", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT alter AS alter from alter.alter", true);
        assertSqlCanBeParsedAndDeparsed("SELECT analyze AS analyze from analyze.analyze", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT and AS and from and.and", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT and_operator AS and_operator from and_operator.and_operator", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT any AS any from any.any", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT apply AS apply from apply.apply", true);
        assertSqlCanBeParsedAndDeparsed("SELECT archive AS archive from archive.archive", true);
        assertSqlCanBeParsedAndDeparsed("SELECT array_literal AS array_literal from array_literal.array_literal", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT as AS as from as.as", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT at AS at from at.at", true);
        assertSqlCanBeParsedAndDeparsed("SELECT asc AS asc from asc.asc", true);
        assertSqlCanBeParsedAndDeparsed("SELECT authorization AS authorization from authorization.authorization", true);
        assertSqlCanBeParsedAndDeparsed("SELECT begin AS begin from begin.begin", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT between AS between from between.between", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT binary AS binary from binary.binary", true);
        assertSqlCanBeParsedAndDeparsed("SELECT bit AS bit from bit.bit", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT both AS both from both.both", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT by AS by from by.by", true);
        assertSqlCanBeParsedAndDeparsed("SELECT buffers AS buffers from buffers.buffers", true);
        assertSqlCanBeParsedAndDeparsed("SELECT byte AS byte from byte.byte", true);
        assertSqlCanBeParsedAndDeparsed("SELECT cache AS cache from cache.cache", true);
        assertSqlCanBeParsedAndDeparsed("SELECT call AS call from call.call", true);
        assertSqlCanBeParsedAndDeparsed("SELECT cascade AS cascade from cascade.cascade", true);
        assertSqlCanBeParsedAndDeparsed("SELECT case AS case from case.case", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT casewhen AS casewhen from casewhen.casewhen", true);  +++ */
        assertSqlCanBeParsedAndDeparsed("SELECT cast AS cast from cast.cast", true);
        assertSqlCanBeParsedAndDeparsed("SELECT char AS char from char.char", true);
        assertSqlCanBeParsedAndDeparsed("SELECT change AS change from change.change", true);
        assertSqlCanBeParsedAndDeparsed("SELECT character AS character from character.character", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT check AS check from check.check", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT checkpoint AS checkpoint from checkpoint.checkpoint", true);
        assertSqlCanBeParsedAndDeparsed("SELECT close AS close from close.close", true);
        assertSqlCanBeParsedAndDeparsed("SELECT collate AS collate from collate.collate", true);
        assertSqlCanBeParsedAndDeparsed("SELECT column AS column from column.column", true);
        assertSqlCanBeParsedAndDeparsed("SELECT columns AS columns from columns.columns", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT comma AS comma from comma.comma", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT comment AS comment from comment.comment", true);
        assertSqlCanBeParsedAndDeparsed("SELECT commit AS commit from commit.commit", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT connect AS connect from connect.connect", true); +++ */
        /* assertSqlCanBeParsedAndDeparsed("SELECT connect_by_root AS connect_by_root from connect_by_root.connect_by_root", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT constraint AS constraint from constraint.constraint", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT costs AS costs from costs.costs", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT create AS create from create.create", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT cross AS cross from cross.cross", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT current AS current from current.current", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT cycle AS cycle from cycle.cycle", true);
        assertSqlCanBeParsedAndDeparsed("SELECT database AS database from database.database", true);
        assertSqlCanBeParsedAndDeparsed("SELECT declare AS declare from declare.declare", true);
        assertSqlCanBeParsedAndDeparsed("SELECT date_literal AS date_literal from date_literal.date_literal", true);
        assertSqlCanBeParsedAndDeparsed("SELECT datetimeliteral AS datetimeliteral from datetimeliteral.datetimeliteral", true);
        assertSqlCanBeParsedAndDeparsed("SELECT dba_recyclebin AS dba_recyclebin from dba_recyclebin.dba_recyclebin", true);
        assertSqlCanBeParsedAndDeparsed("SELECT default AS default from default.default", true);
        assertSqlCanBeParsedAndDeparsed("SELECT deferrable AS deferrable from deferrable.deferrable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT delayed AS delayed from delayed.delayed", true);
        assertSqlCanBeParsedAndDeparsed("SELECT delete AS delete from delete.delete", true);
        assertSqlCanBeParsedAndDeparsed("SELECT desc AS desc from desc.desc", true);
        assertSqlCanBeParsedAndDeparsed("SELECT describe AS describe from describe.describe", true);
        assertSqlCanBeParsedAndDeparsed("SELECT disable AS disable from disable.disable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT disconnect AS disconnect from disconnect.disconnect", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT distinct AS distinct from distinct.distinct", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT div AS div from div.div", true);
        assertSqlCanBeParsedAndDeparsed("SELECT ddl AS ddl from ddl.ddl", true);
        assertSqlCanBeParsedAndDeparsed("SELECT dml AS dml from dml.dml", true);
        assertSqlCanBeParsedAndDeparsed("SELECT do AS do from do.do", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT double AS double from double.double", true); +++ */
        assertSqlCanBeParsedAndDeparsed("SELECT drop AS drop from drop.drop", true);
        assertSqlCanBeParsedAndDeparsed("SELECT dump AS dump from dump.dump", true);
        assertSqlCanBeParsedAndDeparsed("SELECT duplicate AS duplicate from duplicate.duplicate", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT else AS else from else.else", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT enable AS enable from enable.enable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT end AS end from end.end", true);
        assertSqlCanBeParsedAndDeparsed("SELECT escape AS escape from escape.escape", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT except AS except from except.except", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT exclude AS exclude from exclude.exclude", true);
        assertSqlCanBeParsedAndDeparsed("SELECT exec AS exec from exec.exec", true);
        assertSqlCanBeParsedAndDeparsed("SELECT execute AS execute from execute.execute", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT exists AS exists from exists.exists", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT explain AS explain from explain.explain", true);
        assertSqlCanBeParsedAndDeparsed("SELECT extended AS extended from extended.extended", true);
        assertSqlCanBeParsedAndDeparsed("SELECT extract AS extract from extract.extract", true);
        assertSqlCanBeParsedAndDeparsed("SELECT false AS false from false.false", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT fetch AS fetch from fetch.fetch", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT filter AS filter from filter.filter", true);
        assertSqlCanBeParsedAndDeparsed("SELECT first AS first from first.first", true);
        assertSqlCanBeParsedAndDeparsed("SELECT flush AS flush from flush.flush", true);
        assertSqlCanBeParsedAndDeparsed("SELECT fn AS fn from fn.fn", true);
        assertSqlCanBeParsedAndDeparsed("SELECT following AS following from following.following", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT for AS for from for.for", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT force AS force from force.force", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT foreign AS foreign from foreign.foreign", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT format AS format from format.format", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT from AS from from from.from", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT full AS full from full.full", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT fulltext AS fulltext from fulltext.fulltext", true);
        assertSqlCanBeParsedAndDeparsed("SELECT function AS function from function.function", true);
        assertSqlCanBeParsedAndDeparsed("SELECT global AS global from global.global", true);
        assertSqlCanBeParsedAndDeparsed("SELECT grant AS grant from grant.grant", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT group AS group from group.group", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT grouping AS grouping from grouping.grouping", true); +++ */
        assertSqlCanBeParsedAndDeparsed("SELECT group_concat AS group_concat from group_concat.group_concat", true);
        assertSqlCanBeParsedAndDeparsed("SELECT guard AS guard from guard.guard", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT having AS having from having.having", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT high_priority AS high_priority from high_priority.high_priority", true);
        assertSqlCanBeParsedAndDeparsed("SELECT history AS history from history.history", true);
        assertSqlCanBeParsedAndDeparsed("SELECT hopping AS hopping from hopping.hopping", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT if AS if from if.if", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT iif AS iif from iif.iif", true); +++ */
        /* assertSqlCanBeParsedAndDeparsed("SELECT ignore AS ignore from ignore.ignore", true); +++ */
        /* assertSqlCanBeParsedAndDeparsed("SELECT ilike AS ilike from ilike.ilike", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT in AS in from in.in", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT include AS include from include.include", true);
        assertSqlCanBeParsedAndDeparsed("SELECT increment AS increment from increment.increment", true);
        assertSqlCanBeParsedAndDeparsed("SELECT index AS index from index.index", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT inner AS inner from inner.inner", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT insert AS insert from insert.insert", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT intersect AS intersect from intersect.intersect", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT interval AS interval from interval.interval", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT into AS into from into.into", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT is AS is from is.is", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT isnull AS isnull from isnull.isnull", true);
        assertSqlCanBeParsedAndDeparsed("SELECT json AS json from json.json", true);
        assertSqlCanBeParsedAndDeparsed("SELECT json_object AS json_object from json_object.json_object", true);
        assertSqlCanBeParsedAndDeparsed("SELECT json_objectagg AS json_objectagg from json_objectagg.json_objectagg", true);
        assertSqlCanBeParsedAndDeparsed("SELECT json_array AS json_array from json_array.json_array", true);
        assertSqlCanBeParsedAndDeparsed("SELECT json_arrayagg AS json_arrayagg from json_arrayagg.json_arrayagg", true);
        assertSqlCanBeParsedAndDeparsed("SELECT keep AS keep from keep.keep", true);
        assertSqlCanBeParsedAndDeparsed("SELECT key AS key from key.key", true);
        assertSqlCanBeParsedAndDeparsed("SELECT keys AS keys from keys.keys", true);
        assertSqlCanBeParsedAndDeparsed("SELECT last AS last from last.last", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT lateral AS lateral from lateral.lateral", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT leading AS leading from leading.leading", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT left AS left from left.left", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT like AS like from like.like", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT limit AS limit from limit.limit", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT link AS link from link.link", true);
        assertSqlCanBeParsedAndDeparsed("SELECT local AS local from local.local", true);
        assertSqlCanBeParsedAndDeparsed("SELECT log AS log from log.log", true);
        assertSqlCanBeParsedAndDeparsed("SELECT low_priority AS low_priority from low_priority.low_priority", true);
        assertSqlCanBeParsedAndDeparsed("SELECT match AS match from match.match", true);
        assertSqlCanBeParsedAndDeparsed("SELECT matched AS matched from matched.matched", true);
        assertSqlCanBeParsedAndDeparsed("SELECT materialized AS materialized from materialized.materialized", true);
        assertSqlCanBeParsedAndDeparsed("SELECT maxvalue AS maxvalue from maxvalue.maxvalue", true);
        assertSqlCanBeParsedAndDeparsed("SELECT merge AS merge from merge.merge", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT minus AS minus from minus.minus", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT minvalue AS minvalue from minvalue.minvalue", true);
        assertSqlCanBeParsedAndDeparsed("SELECT modify AS modify from modify.modify", true);
        assertSqlCanBeParsedAndDeparsed("SELECT movement AS movement from movement.movement", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT natural AS natural from natural.natural", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT next AS next from next.next", true);
        assertSqlCanBeParsedAndDeparsed("SELECT nextval AS nextval from nextval.nextval", true);
        assertSqlCanBeParsedAndDeparsed("SELECT no AS no from no.no", true);
        assertSqlCanBeParsedAndDeparsed("SELECT nocache AS nocache from nocache.nocache", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT nocycle AS nocycle from nocycle.nocycle", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT nokeep AS nokeep from nokeep.nokeep", true);
        assertSqlCanBeParsedAndDeparsed("SELECT nolock AS nolock from nolock.nolock", true);
        assertSqlCanBeParsedAndDeparsed("SELECT nomaxvalue AS nomaxvalue from nomaxvalue.nomaxvalue", true);
        assertSqlCanBeParsedAndDeparsed("SELECT nominvalue AS nominvalue from nominvalue.nominvalue", true);
        assertSqlCanBeParsedAndDeparsed("SELECT noorder AS noorder from noorder.noorder", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT not AS not from not.not", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT nothing AS nothing from nothing.nothing", true);
        assertSqlCanBeParsedAndDeparsed("SELECT novalidate AS novalidate from novalidate.novalidate", true);
        assertSqlCanBeParsedAndDeparsed("SELECT nowait AS nowait from nowait.nowait", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT null AS null from null.null", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT nulls AS nulls from nulls.nulls", true);
        assertSqlCanBeParsedAndDeparsed("SELECT of AS of from of.of", true);
        assertSqlCanBeParsedAndDeparsed("SELECT off AS off from off.off", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT offset AS offset from offset.offset", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT on AS on from on.on", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT only AS only from only.only", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT open AS open from open.open", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT or AS or from or.or", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT oracle_named_parameter_assignment AS oracle_named_parameter_assignment from oracle_named_parameter_assignment.oracle_named_parameter_assignment", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT order AS order from order.order", true); +++ */
        /* assertSqlCanBeParsedAndDeparsed("SELECT outer AS outer from outer.outer", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT over AS over from over.over", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT optimize AS optimize from optimize.optimize", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT parallel AS parallel from parallel.parallel", true);
        assertSqlCanBeParsedAndDeparsed("SELECT partition AS partition from partition.partition", true);
        assertSqlCanBeParsedAndDeparsed("SELECT path AS path from path.path", true);
        assertSqlCanBeParsedAndDeparsed("SELECT percent AS percent from percent.percent", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT pivot AS pivot from pivot.pivot", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT placing AS placing from placing.placing", true);
        assertSqlCanBeParsedAndDeparsed("SELECT preceding AS preceding from preceding.preceding", true);
        assertSqlCanBeParsedAndDeparsed("SELECT precision AS precision from precision.precision", true);
        assertSqlCanBeParsedAndDeparsed("SELECT primary AS primary from primary.primary", true);
        assertSqlCanBeParsedAndDeparsed("SELECT prior AS prior from prior.prior", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT procedure AS procedure from procedure.procedure", true); +++ */
        /* assertSqlCanBeParsedAndDeparsed("SELECT public AS public from public.public", true); +++ */
        assertSqlCanBeParsedAndDeparsed("SELECT purge AS purge from purge.purge", true);
        assertSqlCanBeParsedAndDeparsed("SELECT query AS query from query.query", true);
        assertSqlCanBeParsedAndDeparsed("SELECT quiesce AS quiesce from quiesce.quiesce", true);
        assertSqlCanBeParsedAndDeparsed("SELECT range AS range from range.range", true);
        assertSqlCanBeParsedAndDeparsed("SELECT read AS read from read.read", true);
        assertSqlCanBeParsedAndDeparsed("SELECT recyclebin AS recyclebin from recyclebin.recyclebin", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT recursive AS recursive from recursive.recursive", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT references AS references from references.references", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT regexp AS regexp from regexp.regexp", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT rlike AS rlike from rlike.rlike", true);
        assertSqlCanBeParsedAndDeparsed("SELECT register AS register from register.register", true);
        assertSqlCanBeParsedAndDeparsed("SELECT rename AS rename from rename.rename", true);
        assertSqlCanBeParsedAndDeparsed("SELECT replace AS replace from replace.replace", true);
        assertSqlCanBeParsedAndDeparsed("SELECT reset AS reset from reset.reset", true);
        assertSqlCanBeParsedAndDeparsed("SELECT restrict AS restrict from restrict.restrict", true);
        assertSqlCanBeParsedAndDeparsed("SELECT restricted AS restricted from restricted.restricted", true);
        assertSqlCanBeParsedAndDeparsed("SELECT resumable AS resumable from resumable.resumable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT resume AS resume from resume.resume", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT returning AS returning from returning.returning", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT right AS right from right.right", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT rollback AS rollback from rollback.rollback", true);
        assertSqlCanBeParsedAndDeparsed("SELECT row AS row from row.row", true);
        assertSqlCanBeParsedAndDeparsed("SELECT rows AS rows from rows.rows", true);

        assertSqlCanBeParsedAndDeparsed("SELECT savepoint AS savepoint from savepoint.savepoint", true);
        assertSqlCanBeParsedAndDeparsed("SELECT schema AS schema from schema.schema", true);
        assertSqlCanBeParsedAndDeparsed("SELECT sequence AS sequence from sequence.sequence", true);
        assertSqlCanBeParsedAndDeparsed("SELECT separator AS separator from separator.separator", true);
        assertSqlCanBeParsedAndDeparsed("SELECT session AS session from session.session", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT set AS set from set.set", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT sets AS sets from sets.sets", true);
        assertSqlCanBeParsedAndDeparsed("SELECT show AS show from show.show", true);
        assertSqlCanBeParsedAndDeparsed("SELECT shutdown AS shutdown from shutdown.shutdown", true);
        assertSqlCanBeParsedAndDeparsed("SELECT siblings AS siblings from siblings.siblings", true);
        assertSqlCanBeParsedAndDeparsed("SELECT signed AS signed from signed.signed", true);
        assertSqlCanBeParsedAndDeparsed("SELECT similar AS similar from similar.similar", true);
        assertSqlCanBeParsedAndDeparsed("SELECT size AS size from size.size", true);
        assertSqlCanBeParsedAndDeparsed("SELECT skip AS skip from skip.skip", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT some AS some from some.some", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT start AS start from start.start", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT string_function_name AS string_function_name from string_function_name.string_function_name", true);
        assertSqlCanBeParsedAndDeparsed("SELECT suspend AS suspend from suspend.suspend", true);
        assertSqlCanBeParsedAndDeparsed("SELECT switch AS switch from switch.switch", true);
        assertSqlCanBeParsedAndDeparsed("SELECT synonym AS synonym from synonym.synonym", true);
        assertSqlCanBeParsedAndDeparsed("SELECT system AS system from system.system", true);

        assertSqlCanBeParsedAndDeparsed("SELECT table AS table from table.table", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT tables AS tables from tables.tables", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT tablespace AS tablespace from tablespace.tablespace", true);
        assertSqlCanBeParsedAndDeparsed("SELECT temp AS temp from temp.temp", true);
        assertSqlCanBeParsedAndDeparsed("SELECT temporary AS temporary from temporary.temporary", true);
        assertSqlCanBeParsedAndDeparsed("SELECT then AS then from then.then", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT time_key_expr AS time_key_expr from time_key_expr.time_key_expr", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT timeout AS timeout from timeout.timeout", true);
        assertSqlCanBeParsedAndDeparsed("SELECT to AS to from to.to", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT top AS top from top.top", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT trailing AS trailing from trailing.trailing", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT truncate AS truncate from truncate.truncate", true);
        assertSqlCanBeParsedAndDeparsed("SELECT true AS true from true.true", true);
        assertSqlCanBeParsedAndDeparsed("SELECT tumbling AS tumbling from tumbling.tumbling", true);
        assertSqlCanBeParsedAndDeparsed("SELECT type AS type from type.type", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT unbounded AS unbounded from unbounded.unbounded", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT union AS union from union.union", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT unique AS unique from unique.unique", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT unlogged AS unlogged from unlogged.unlogged", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT unpivot AS unpivot from unpivot.unpivot", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT update AS update from update.update", true);
        assertSqlCanBeParsedAndDeparsed("SELECT upsert AS upsert from upsert.upsert", true);
        assertSqlCanBeParsedAndDeparsed("SELECT unqiesce AS unqiesce from unqiesce.unqiesce", true);
        assertSqlCanBeParsedAndDeparsed("SELECT unsigned AS unsigned from unsigned.unsigned", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT use AS use from use.use", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT user AS user from user.user", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT using AS using from using.using", true); */

        assertSqlCanBeParsedAndDeparsed("SELECT a1 AS sql_calc_found_rows from sql_calc_found_rows.sql_calc_found_rows", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a1 AS sql_no_cache from sql_no_cache.sql_no_cache", true);

        /* assertSqlCanBeParsedAndDeparsed("SELECT signed AS signed from signed.signed", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT string_function_name AS string_function_name from string_function_name.string_function_name", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT unsigned AS unsigned from unsigned.unsigned", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT validate AS validate from validate.validate", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT value AS value from value.value", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT values AS values from values.values", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT varying AS varying from varying.varying", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT verbose AS verbose from verbose.verbose", true);
        assertSqlCanBeParsedAndDeparsed("SELECT view AS view from view.view", true);
        assertSqlCanBeParsedAndDeparsed("SELECT wait AS wait from wait.wait", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT when AS when from when.when", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT where AS where from where.where", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT window AS window from window.window", true); */
        /* assertSqlCanBeParsedAndDeparsed("SELECT with AS with from with.with", true); */
        assertSqlCanBeParsedAndDeparsed("SELECT within AS within from within.within", true);
        assertSqlCanBeParsedAndDeparsed("SELECT without AS without from without.without", true);
        assertSqlCanBeParsedAndDeparsed("SELECT work AS work from work.work", true);
        assertSqlCanBeParsedAndDeparsed("SELECT xml AS xml from xml.xml", true);
        /* assertSqlCanBeParsedAndDeparsed("SELECT xor AS xor from xor.xor", true); !!! */
        /* assertSqlCanBeParsedAndDeparsed("SELECT xmlserialize AS xmlserialize from xmlserialize.xmlserialize", true); !!! */
        assertSqlCanBeParsedAndDeparsed("SELECT xmlagg AS xmlagg from xmlagg.xmlagg", true);
        assertSqlCanBeParsedAndDeparsed("SELECT xmltext AS xmltext from xmltext.xmltext", true);
        assertSqlCanBeParsedAndDeparsed("SELECT yaml AS yaml from yaml.yaml", true);
        assertSqlCanBeParsedAndDeparsed("SELECT zone AS zone from zone.zone", true);


    }

    @Test
    public void testRelObjectNameExt() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT connect AS connect1 from connect.connect", true);
        assertSqlCanBeParsedAndDeparsed("SELECT create AS create1 from create.create", true);
        assertSqlCanBeParsedAndDeparsed("SELECT current AS a1 from a1.current", true);
        assertSqlCanBeParsedAndDeparsed("SELECT ignore AS ignore1 from ignore.ignore", true);
        assertSqlCanBeParsedAndDeparsed("SELECT iif AS a1 from iif.iif", true);
        assertSqlCanBeParsedAndDeparsed("SELECT order AS order1 from order.order", true);
    }
}
