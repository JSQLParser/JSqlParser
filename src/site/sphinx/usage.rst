******************************
How to use it
******************************

JSQLParser turns SQL text into a tree of Java objects, lets you inspect or rewrite that tree, and prints it back out as SQL. Everything on this page is built on those three moves.

.. code-block:: java
    :caption: The whole idea in five lines

    Statement statement = CCJSqlParserUtil.parse("SELECT a FROM my_table WHERE id = 42");

    // inspect it
    PlainSelect select = (PlainSelect) statement;
    Table table = (Table) select.getFromItem();      // my_table

    // print it back
    String sql = statement.toString();

.. tip::

    **New here?** Read :ref:`Add JSQLParser to your Project`, then :ref:`Parse a SQL Statement`, then :ref:`Explore the Parsed Tree`. Everything after that is optional and can be read in any order.

.. list-table:: What is on this page
    :header-rows: 1
    :widths: 30 70

    * - Section
      - Use it when you want to …
    * - :ref:`Add JSQLParser to your Project`
      - pull in the dependency, and pick between the Manticore and upstream builds
    * - :ref:`Parse a SQL Statement`
      - turn SQL text into Java objects
    * - :ref:`Explore the Parsed Tree`
      - find your way around the object model
    * - :ref:`Classify a Statement`
      - know whether SQL reads, writes or returns rows — before you run it
    * - :ref:`Find Table Names`
      - list every table a statement touches
    * - :ref:`Use the Visitor Patterns`
      - walk the whole tree and react to specific nodes
    * - :ref:`Build a SQL Statement`
      - construct SQL from Java instead of from text
    * - :ref:`Handle Parse Errors`
      - keep going when one statement in a script is broken
    * - :ref:`Choose a Dialect`
      - parse T-SQL brackets, MySQL escapes, BigQuery quoting …
    * - :ref:`Compile from Source Code`
      - build JSQLParser yourself or contribute


Add JSQLParser to your Project
==============================

There are two sets of artifacts on Maven Central, built from the same source under the same dual licence:

.. list-table::
    :header-rows: 1
    :widths: 30 30 40

    * - Artifact
      - ``groupId``
      - Cut from
    * - **Manticore build** (recommended)
      - ``com.manticore-projects.jsqlformatter``
      - the current development line, released continuously
    * - Upstream release
      - ``com.github.jsqlparser``
      - the official release cadence
    * - Upstream snapshot
      - ``com.github.jsqlparser``
      - the latest commit, overwritten in place

Upstream releases are cut infrequently. Between two of them a lot of grammar and performance work lands — the 11× parse speed-up, JavaCC 8 support, new dialect syntax — and waiting for the next official version to catch up can mean months on a build that already has the fix you need.

Snapshots are not the answer either: a ``-SNAPSHOT`` coordinate is mutable, so the same version string can resolve to different bytes tomorrow. That is fine for trying something out and wrong for a reproducible build.

The **Manticore builds** fill that gap. Each one is an immutable, versioned release published to Maven Central from the current development line, so you get the fixes early *and* a build that stays reproducible. Use them unless you have a reason to pin to the official release — and note the different ``groupId``, the artifact name is the same.

.. tab:: Maven — Manticore

    .. code-block:: xml

        <dependency>
            <groupId>com.manticore-projects.jsqlformatter</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>[5.3.218,)</version>
        </dependency>

    The range ``[5.3.218,)`` takes the newest available build. Pin an exact version instead once you ship.

.. tab:: Gradle — Manticore

    .. code-block:: groovy

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'com.manticore-projects.jsqlformatter:jsqlparser:+'
        }

    ``+`` takes the newest available build. Pin an exact version instead once you ship.

.. tab:: Maven Release

    .. code-block:: xml
        :substitutions:

        <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>|JSQLPARSER_VERSION|</version>
        </dependency>

.. tab:: Maven Snapshot

    .. code-block:: xml
        :substitutions:

        <repositories>
            <repository>
                <id>jsqlparser-snapshots</id>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
                <url>https://oss.sonatype.org/content/groups/public/</url>
            </repository>
        </repositories>
        <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>|JSQLPARSER_SNAPSHOT_VERSION|</version>
        </dependency>

.. tab:: Gradle Stable

    .. code-block:: groovy
        :substitutions:

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'com.github.jsqlparser:jsqlparser:|JSQLPARSER_VERSION|'
        }

.. tab:: Gradle Snapshot

    .. code-block:: groovy
        :substitutions:

        repositories {
            maven {
                url = uri('https://oss.sonatype.org/content/groups/public/')
            }
        }

        dependencies {
            implementation 'com.github.jsqlparser:jsqlparser:|JSQLPARSER_SNAPSHOT_VERSION|'
        }

.. note::

    Features documented here may reach the Manticore builds before the next upstream release. If a class or method on this page is missing, check which of the two you are resolving.


Parse a SQL Statement
==============================

``CCJSqlParserUtil.parse()`` is the entry point. It returns a ``Statement``, which you cast to the concrete type you expect.

.. code-block:: java

    String sqlStr = "select 1 from dual where a=b";

    PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

    SelectItem selectItem =
            select.getSelectItems().get(0);
    Assertions.assertEquals(
            new LongValue(1)
            , selectItem.getExpression());

    Table table = (Table) select.getFromItem();
    Assertions.assertEquals("dual", table.getName());

    EqualsTo equalsTo = (EqualsTo) select.getWhere();
    Column a = (Column) equalsTo.getLeftExpression();
    Column b = (Column) equalsTo.getRightExpression();
    Assertions.assertEquals("a", a.getColumnName());
    Assertions.assertEquals("b", b.getColumnName());

For several statements at once, use ``CCJSqlParserUtil.parseStatements()``, which returns a ``Statements`` — an ``ArrayList<Statement>``.

.. code-block:: java

    Statements script = CCJSqlParserUtil.parseStatements(
            "UPDATE t SET a = 1; SELECT a FROM t;");

    assertEquals(2, script.size());

.. note::

    Supported statement separators are semicolon ``;``, ``GO``, slash ``/`` and two empty lines ``\n\n\n``.

If parsing fails on syntax JSQLParser does not know, see :ref:`Handle Parse Errors` — and please `open an issue <https://github.com/JSQLParser/JSqlParser/issues>`_, missing syntax gets added on demand.


Explore the Parsed Tree
==============================

The fastest way to learn the object model is to look at it. Paste your SQL into `JSQLFormatter <http://jsqlformatter.manticore-projects.com>`_ and it will draw the tree, with the Java class of every node:

.. raw:: html

    <div class="highlight">
    <pre>
    SQL Text
          └─Statements: net.sf.jsqlparser.statement.select.Select
              ├─selectItems -> Collection&lt;SelectItem&gt;
              │  └─LongValue: 1
              ├─Table: dual
              └─where: net.sf.jsqlparser.expression.operators.relational.EqualsTo
                 ├─Column: a
                 └─Column: b
   </pre>
   </div>

Read that as a map: each line is a getter away. ``select.getSelectItems()``, ``select.getFromItem()``, ``select.getWhere()``. Once the tree gets deeper than a couple of levels, stop casting by hand and use :ref:`Use the Visitor Patterns`.


PostgreSQL CHECK inheritance
----------------------------

With ``Dialect.POSTGRESQL``, CREATE and ALTER CHECK constraints support
``NO INHERIT``. ``CheckConstraint.isNoInherit()`` reports this flag; use
``setNoInherit`` or ``withNoInherit`` to change it. The check expression remains
an editable ``Expression`` visited by the existing table traversal and deparsers.

For ``ALTER TABLE t ADD CHECK (id > 0) NO INHERIT NOT VALID``, the two options
are independent: ``isNoInherit()`` is true and
``getConstraintAttributes().isNotValid()`` is true. Clearing one flag preserves
the other when the statement is rendered.

DROP INDEX owners
-----------------

``DROP INDEX ix ON app.t`` exposes ``app.t`` through ``Drop.getTable()``;
``getName()`` continues to identify the index. The owner is populated for the
single-index ON form used by MySQL and SQL Server. A DROP INDEX without ON,
as in PostgreSQL, has a null owner: resolving the index's table requires a catalog.
SQL Server's multi-owner list and WITH options are outside this extension.

``getParameters()`` includes ON and the rendered table as a legacy token snapshot
when a structured owner exists. Mutate ``getTable()`` or use ``setTable()`` to
update the owner; ``addParameters()`` appends options without losing it.
``setTable(null)`` removes ON, while ``setParameters()`` replaces the entire
parameter clause with raw tokens and clears the structured owner. Raw parameter
setters do not parse SQL or infer a table.

Table discovery and statement visitors traverse DROP TABLE/VIEW targets and
explicit index owners, without reporting catalog-only object names as tables.
The statement deparser delegates real tables to its configured select deparser.
MySQL ALGORITHM/LOCK tokens retain their order and optional equals signs.

MySQL named constraint drops
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

With ``Dialect.MYSQL``, ``DROP FOREIGN KEY name`` and ``DROP CHECK name``
expose the target through ``AlterExpression.getConstraintName()``. Inspect
``getOperation()`` for ``DROP_FOREIGN_KEY`` or ``DROP_CHECK``. Identifier
quotes are preserved, and changing the name changes both SQL renderers.
These named targets do not populate the legacy ``getPkColumns()`` list.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE child DROP FOREIGN KEY fk_parent",
        parser -> parser.withDialect(Dialect.MYSQL));
    AlterExpression drop = alter.getAlterExpressions().get(0);
    drop.setConstraintName("fk_customer");
    // ALTER TABLE child DROP FOREIGN KEY fk_customer
    String sql = alter.toString();

    // The same API also supports constructing an action without parsing SQL.
    AlterExpression checkDrop = new AlterExpression()
        .withOperation(AlterOperation.DROP_CHECK).withConstraintName("positive_id");

Inspect PostgreSQL schema statements
------------------------------------

PostgreSQL schema clauses extend the existing ``CreateView``, ``CreateTable``, ``Alter`` and ``Sequence`` models. Use their typed properties to inspect the clauses and preserve distinctions between omitted options and explicit values.

.. code-block:: java

    CreateView view = (CreateView) CCJSqlParserUtil.parse(
            "CREATE MATERIALIZED VIEW IF NOT EXISTS account_totals "
            + "AS SELECT id FROM accounts WITH NO DATA");
    view.isMaterialized();       // true
    view.isIfNotExists();        // true
    view.getWithData();          // Boolean.FALSE; null means the clause was omitted

Ordinary views expose ordered ``ViewOption`` values for ``security_barrier``, ``security_invoker`` and ``check_option``. ``getCheckOption()`` describes the trailing ``WITH CHECK OPTION`` clause: ``null`` means absent, ``DEFAULT`` preserves the bare clause, and ``LOCAL`` and ``CASCADED`` preserve explicit keywords. ``getEffectiveCheckOption()`` resolves the bare clause to ``CASCADED``. Materialized views expose their access method, storage parameters and tablespace separately. See the PostgreSQL documentation for `CREATE VIEW <https://www.postgresql.org/docs/18/sql-createview.html>`_ and `CREATE MATERIALIZED VIEW <https://www.postgresql.org/docs/18/sql-creatematerializedview.html>`_.

.. code-block:: java

    CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
            "CREATE TABLE accounts_copy (LIKE accounts INCLUDING ALL EXCLUDING INDEXES)");
    LikeClause like = table.getTableElements(LikeClause.class).get(0);
    like.getOptions();                                  // ordered INCLUDING/EXCLUDING clauses
    like.isIncluding(LikeClause.OptionKind.DEFAULTS);   // true, inherited from ALL
    like.isIncluding(LikeClause.OptionKind.INDEXES);    // false, overridden by EXCLUDING

``getTableElements()`` preserves the order of columns, constraints and ``LIKE`` clauses, including multiple source tables. The three legacy nullable ``LikeClause`` getters report explicit options of that kind; ``isIncluding(OptionKind)`` also resolves ``ALL`` in declaration order. A typed table's ``OF`` type is available through ``CreateTable.getOfType()``.

Table constraints expose ``Index.getNullsDistinct()``, ``getIncludeColumns()``, storage parameters and ``ConstraintAttributes``. ``ExcludeConstraint`` reuses ``Index.ColumnParams`` for its keys; each key exposes its expression and exclusion operator. Column identity clauses are represented by ``ColumnOption.Kind.IDENTITY`` and ``IdentityDefinition``, with a generation mode and ordered ``Sequence.Parameter`` values. These APIs cover the schema clauses described in `CREATE TABLE <https://www.postgresql.org/docs/18/sql-createtable.html>`_.

Exclusion keys expose ``getExclusionOperatorReference()`` for structured access
to an operator's ``schemaName``, ``name`` and ``useOperatorKeyword`` flag. In
``Dialect.POSTGRESQL``, both CREATE and ALTER accept ``WITH OPERATOR(pg_catalog.&&)``.
Editing the reference updates both statement renderers. The existing string
``getExclusionOperator()`` returns its complete SQL spelling; its setter remains
available for replacing opaque operator text. New code can construct a reference
with ``new ExclusionOperator().withSchemaName("pg_catalog").withName("&&")
.withUseOperatorKeyword(true)``.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
            "ALTER TABLE accounts ALTER COLUMN id TYPE bigint USING id + 1");
    AlterExpression.ColumnDataType column = alter.getAlterExpressions().get(0)
            .getColDataTypeList().get(0);
    Expression conversion = column.getUsingExpression();
    column.setUsingExpression(CCJSqlParserUtil.parseExpression("id * 10"));
    String updatedSql = alter.toString();

Identity alterations are available as ``ColumnDataType.getIdentityAlterations()``. Sequence ownership is shared by ``CreateSequence`` and ``AlterSequence`` through ``Sequence.getOwnership()``: ``null`` means omitted, ``isNone()`` means explicit ``OWNED BY NONE``, and ``getColumn()`` identifies an owner. ``TablesNamesFinder`` includes ``LIKE`` sources and sequence owners without treating sequence or type names as tables. See `ALTER TABLE <https://www.postgresql.org/docs/18/sql-altertable.html>`_ and `ALTER SEQUENCE <https://www.postgresql.org/docs/18/sql-altersequence.html>`_.

For identity columns, ``RESTART 20`` and ``RESTART WITH 20`` produce the same
``IdentityAlteration`` with kind ``RESTART`` and ``getRestartWith() == 20L``.
The renderer consistently uses ``RESTART WITH 20``. A null restart value means
bare ``RESTART``, which uses the sequence's configured start value. Call
``setRestartWith`` to edit a parsed action or construct one with
``new IdentityAlteration(Kind.RESTART).withRestartWith(20L)``.


Structured column attributes
============================

``ColumnDefinition.getColumnOptions()`` exposes column nullability, ``COLLATE``,
``COMMENT``, ``ON UPDATE``, ``AUTO_INCREMENT``, visibility, inline ``PRIMARY KEY``
and parenthesized generated expressions alongside existing defaults, references
and identity declarations. CREATE and ALTER column definitions use the same model.

``ColumnOption.Kind`` selects the relevant accessor: ``getNullable()``,
``getCollation()``, ``getComment()``, ``getOnUpdateExpression()``,
``getVisible()``, ``getConstraint()`` or ``getGeneratedDefinition()``.
The AUTO_INCREMENT kind has no additional payload. A missing option does not
imply a server default. Option order and unknown raw extensions are preserved.

``GeneratedColumnDefinition`` contains an ``Expression``, an explicit
``GENERATED ALWAYS`` flag, and nullable ``Storage`` (STORED/VIRTUAL). Identity
columns continue to use ``IdentityDefinition``. Visitors, validators and custom
expression deparsers traverse generation and ON UPDATE expressions and comment
literals. This is syntax modeling; server restrictions on permissible generation
expressions are not evaluated.

Charset remains in ``ColDataType.getCharacterSet()``; column collation is in its
COLLATE option. Consumers can map both directly without reconstructing tokens.
StringValue comment bodies retain their SQL escape representation.

The legacy ``getColumnSpecs()`` returns a token snapshot when options are
structured; expression fragments may occupy one token and structured keywords
use canonical capitalization. Mutate the option objects to change the AST.
``addColumnSpecs`` preserves existing options; ``setColumnSpecs`` explicitly
replaces them with raw specifications.

Inspect logical replication statements
======================================

PostgreSQL publications and subscriptions have separate statement and option models. No database connection is opened when these statements are parsed.

.. code-block:: java

    CreatePublication publication = (CreatePublication) CCJSqlParserUtil.parse(
            "CREATE PUBLICATION changes FOR TABLE accounts (id) WHERE (active = true)");
    PublicationTable target = publication.getTargets().get(0).getTables().get(0);
    Table table = target.getTable();
    List<Column> columns = target.getColumns();
    Expression filter = target.getWhere();

A ``PublicationTarget`` distinguishes a group of explicit tables from ``TABLES IN SCHEMA``. Target order, repeated ``TABLE`` groups, ``ONLY`` and an explicit descendant ``*`` are preserved. ``CreatePublication.isAllTables()`` represents ``FOR ALL TABLES``; an empty target list without that flag means no target clause was specified. ``AlterPublication.getAction()`` distinguishes adding, replacing or removing targets from option, owner and name changes.

``PublicationOption`` exposes typed operation sets, partition-root booleans and generated-column modes. ``SubscriptionOption`` has a separate key enum and typed streaming, origin, synchronous-commit and boolean accessors. The ordered option lists contain only explicitly written options; server defaults, which can differ across PostgreSQL versions, are not injected into the AST. A missing value on a boolean option represents its explicit short form, equivalent to ``= true``.

.. code-block:: java

    CreateSubscription subscription = (CreateSubscription) CCJSqlParserUtil.parse(
            "CREATE SUBSCRIPTION changes_sub CONNECTION 'dbname=app' "
            + "PUBLICATION changes WITH (connect = false)");
    StringValue connection = subscription.getConnection();
    List<String> publications = subscription.getPublications();
    Boolean connect = subscription.getOptions().get(0).getBooleanValue();

Connection strings remain string literals for lossless SQL regeneration. They can contain credentials and should not be logged without redaction. ``SubscriptionOption.isSlotNameNone()`` distinguishes the unquoted ``NONE`` keyword from a literal slot named ``'NONE'``. ``AlterSubscription`` covers connection changes, publication lists and refresh, enable/disable, options, skip LSN, ownership and renaming.

Publication table columns and row filters participate in visitors and custom expression deparsers. ``TablesNamesFinder`` reports explicitly named publication tables, but cannot enumerate ``ALL TABLES`` or schema-wide targets without a catalog. Publication and subscription names are not table names. Feature classification reports schema modification; a subscription that may start asynchronous replication can additionally report possible data modification.

See `CREATE PUBLICATION <https://www.postgresql.org/docs/18/sql-createpublication.html>`_, `ALTER PUBLICATION <https://www.postgresql.org/docs/18/sql-alterpublication.html>`_, `CREATE SUBSCRIPTION <https://www.postgresql.org/docs/18/sql-createsubscription.html>`_ and `ALTER SUBSCRIPTION <https://www.postgresql.org/docs/18/sql-altersubscription.html>`_.

Inspect type, domain and extension statements
=============================================

PostgreSQL type DDL uses the existing column data-type model. A ``CreateType`` exposes its qualified name and a ``TypeDefinition``: ``EnumTypeDefinition``, ``CompositeTypeDefinition`` or ``RangeTypeDefinition``. A null definition denotes a shell type. Enum labels are ordered ``StringValue`` nodes, composite attributes carry a name, ``ColDataType`` and optional collation, and range options distinguish the subtype from names of support functions, collations and operator classes.

.. code-block:: java

    CreateType type = (CreateType) CCJSqlParserUtil.parse(
            "CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy')");
    EnumTypeDefinition definition = (EnumTypeDefinition) type.getDefinition();
    String firstLabel = definition.getLabels().get(0).getValue();

    AlterType alteration = (AlterType) CCJSqlParserUtil.parse(
            "ALTER TYPE mood ADD VALUE IF NOT EXISTS 'fine' AFTER 'ok'");
    alteration.getPosition();       // AlterType.Position.AFTER
    alteration.getNeighborValue(); // StringValue containing 'ok'

``AlterType`` also models type ownership, renaming, schema moves and ordered composite-attribute changes. Base-type I/O definitions and base-type ``SET (...)`` alterations are not part of this support.

``CreateDomain`` stores a ``ColDataType``, default expression and ordered ``DomainConstraint`` nodes. Each constraint distinguishes nullability from a check expression and preserves its optional name. ``AlterDomain`` models default/nullability changes, constraint actions, ownership, renaming and schema moves. Its ``isNotValid()`` flag describes the newly added check constraint. Domain expressions participate in statement visitors, expression deparsers and validation.

``CreateExtension`` preserves ``IF NOT EXISTS``, the optional ``WITH``, and ordered schema/version/cascade options. Version identifiers and quoted versions are represented as ``Column`` and ``StringValue``, respectively. ``AlterExtension`` distinguishes update, schema change, and member addition/removal. An ``ExtensionObject`` identifies the object kind and target; function/procedure/aggregate members expose a ``RoutineReference`` with typed signature arguments, not call expressions. A null argument list means the signature was omitted, whereas an empty list means explicit ``()``.

Type names, domain names and routine signatures are not reported as tables by ``TablesNamesFinder``; relation members of an extension are included. Extension scripts themselves are not inspected or executed. The obsolete ``CREATE EXTENSION ... FROM`` form is not supported.

See PostgreSQL's documentation for `CREATE TYPE <https://www.postgresql.org/docs/18/sql-createtype.html>`_, `ALTER TYPE <https://www.postgresql.org/docs/18/sql-altertype.html>`_, `CREATE DOMAIN <https://www.postgresql.org/docs/18/sql-createdomain.html>`_ and `ALTER EXTENSION <https://www.postgresql.org/docs/18/sql-alterextension.html>`_.

Classify a Statement
==============================

Every ``Statement`` can tell you **what it does** — whether it reads, writes, changes the schema, or sends rows back — without a second parse and without writing a visitor:

.. code-block:: java

    StatementFeatures features = CCJSqlParserUtil.parse(sqlStr).getFeatures();

    if (features.returnsResultSet()) {
        statement.executeQuery(sqlStr);
    } else {
        statement.executeUpdate(sqlStr);
    }

Why bother
------------------------------

Two jobs come up constantly, and both are traps if you approach them with string matching:

**Safeguarding a read-only client.** Reporting tools, BI front-ends, LLM-generated SQL, user-supplied filters — plenty of code paths need to reject anything that writes, *before* the statement reaches the database. Checking whether the text starts with ``SELECT`` is not a safeguard.

**Dispatching correctly.** JDBC wants ``executeQuery()`` for row-returning statements and ``executeUpdate()`` for the rest. Get it backwards and you get an exception, not a wrong answer, but you still have to decide.

The reason a keyword check fails is that SQL is not organised into tidy Query/DML/DDL buckets. ``RETURNING`` turns a ``DELETE`` into a row source. A data-modifying CTE hides that ``DELETE`` inside something that begins with ``WITH``. An ``INSERT`` can contain a whole ``SELECT`` and still return nothing:

.. list-table::
    :header-rows: 1
    :widths: 55 15 15 15

    * - SQL
      - returns rows
      - reads
      - writes
    * - ``SELECT * FROM t``
      - yes
      - yes
      - no
    * - ``INSERT INTO x SELECT * FROM t``
      - **no**
      - yes
      - yes
    * - ``DELETE FROM t RETURNING *``
      - **yes**
      - no
      - yes
    * - ``WITH c AS (DELETE FROM t RETURNING *) SELECT * FROM c``
      - yes
      - no
      - **yes**
    * - ``INSERT INTO x WITH c AS (DELETE FROM t RETURNING *) SELECT * FROM c``
      - **no**
      - no
      - yes
    * - ``CREATE TABLE t AS SELECT a FROM u``
      - no
      - yes
      - no (schema)
    * - ``SELECT a INTO new_table FROM t``
      - **no**
      - yes
      - **yes**

Note the last two rows of the fourth and fifth entries: ``RETURNING`` appearing *somewhere* in the statement is not the question. What matters is whether rows reach the client, and that is a property of the statement's own result position, not of any nested one.

The features
------------------------------

.. list-table::
    :header-rows: 1
    :widths: 25 40 35

    * - ``StmtFeature``
      - Meaning
      - Typical statements
    * - ``READS_DATA``
      - reads persistent rows
      - ``SELECT .. FROM t``, ``MERGE``, ``CREATE TABLE .. AS SELECT``
    * - ``RETURNS_RESULT_SET``
      - rows are sent back to the client
      - ``SELECT``, ``DELETE .. RETURNING``, ``SHOW``, ``DESCRIBE``, ``EXPLAIN``
    * - ``MODIFIES_DATA``
      - rows are written or destroyed
      - ``INSERT``, ``UPDATE``, ``DELETE``, ``MERGE``, ``UPSERT``, ``TRUNCATE``, ``DROP``
    * - ``MODIFIES_SCHEMA``
      - the catalogue changes
      - ``CREATE``, ``ALTER``, ``DROP``, ``TRUNCATE``, ``GRANT``, ``COMMENT``
    * - ``MODIFIES_SESSION``
      - session state changes
      - ``SET``, ``RESET``, ``USE``, ``DECLARE``, ``ALTER SESSION``
    * - ``MODIFIES_TRANSACTION``
      - transaction state or locks change
      - ``COMMIT``, ``ROLLBACK``, ``SAVEPOINT``, ``LOCK``, ``SELECT .. FOR UPDATE``
    * - ``OPAQUE``
      - nothing further can be known statically
      - ``CALL``, ``EXECUTE``, dynamic SQL, unsupported statements

They are **not mutually exclusive**. ``INSERT .. RETURNING *`` carries ``MODIFIES_DATA`` *and* ``RETURNS_RESULT_SET``; ``TRUNCATE`` carries ``MODIFIES_SCHEMA`` *and* ``MODIFIES_DATA``, so that a guard looking only for data changes still stops it.

Proven, possible, excluded
------------------------------

Each feature is three-valued, because some questions cannot be answered from syntax alone. ``SELECT nextval('s')`` writes; ``SELECT upper(name)`` does not; the parser cannot tell them apart, because volatility lives in the database catalogue, not in the SQL text.

So a feature is either **proven**, **not excludable**, or **ruled out**, and you pick which side you want to be wrong on:

.. code-block:: java

    StatementFeatures features = statement.getFeatures();

    features.is(StmtFeature.MODIFIES_DATA);   // the grammar proves it
    features.may(StmtFeature.MODIFIES_DATA);  // proven, or could not be excluded

.. list-table::
    :header-rows: 1
    :widths: 25 20 55

    * - Caller
      - Uses
      - Because
    * - read-only guard
      - ``may(..)``
      - a false negative lets a write through
    * - JDBC dispatcher
      - ``is(..)``
      - a false positive picks ``executeQuery`` for ``CREATE INDEX``

Convenience methods wrap the common combinations:

.. code-block:: java

    features.returnsResultSet();   // is(RETURNS_RESULT_SET)
    features.modifiesData();       // is(MODIFIES_DATA)
    features.mayModifyData();      // may(MODIFIES_DATA)
    features.modifiesSchema();     // is(MODIFIES_SCHEMA)
    features.isOpaque();           // CALL, EXECUTE, dynamic SQL

When something is merely *possible*, the analysis tells you **why**, so you can resolve it against your own catalogue or allow-list rather than guessing:

.. code-block:: java
    :caption: Safeguarding a read-only connection

    StatementFeatures features = CCJSqlParserUtil.parse(sqlStr).getFeatures();

    if (connection.isReadOnly() && features.mayModifyData()) {
        throw new SQLException(
                "rejected, unresolved: " + features.getUnresolvedReferences());
        // e.g. [nextval]
    }

If you can prove some functions side-effect free, hand in a predicate and the uncertainty collapses:

.. code-block:: java

    Set<String> pure = Set.of("upper", "lower", "coalesce");

    StatementFeatures features = statement.getFeatures(pure::contains);

    // SELECT upper(name) FROM t
    features.mayModifyData();              // false
    features.getUnresolvedReferences();    // empty

.. warning::

    The verdict is a **syntactic claim, not a semantic guarantee**. A user-defined function, a trigger on the target table or a ``CALL`` can do anything. Use this to reject obviously dangerous SQL early; it does not replace database-side permissions.

Scripts
------------------------------

``Statements`` is an ``ArrayList<Statement>`` and not a ``Statement``, so it has no ``getFeatures()`` of its own. Two entry points, for two different questions:

.. code-block:: java

    Statements script = CCJSqlParserUtil.parseStatements(
            "UPDATE t SET a = 1; SELECT a FROM t;");

    // one union verdict — for guards
    StatementFeatures all = StatementFeatureVisitor.analyse(script);
    all.modifiesData();        // true
    all.returnsResultSet();    // true

    // one verdict per statement, in order — for dispatchers
    List<StatementFeatures> each = StatementFeatureVisitor.analyseEach(script);
    each.get(0).returnsResultSet();   // false, the UPDATE
    each.get(1).returnsResultSet();   // true, the SELECT

The union answers *"may this script write anything?"*. It cannot answer *"executeQuery or executeUpdate?"*, because it never says which statement returns the rows.

.. note::

    Nothing is cached. The tree is mutable and you may build statements by hand, so the verdict is recomputed on every call — microseconds against a millisecond-scale parse.


Find Table Names
==============================

``net.sf.jsqlparser.util.TablesNamesFinder`` returns every table name in a statement or an expression, including the ones buried in sub-selects.

.. code-block:: java

     // find in Statements
     String sqlStr = "select * from A left join B on A.id=B.id and A.age = (select age from C)";
     Set<String> tableNames = TablesNamesFinder.findTables(sqlStr);
     assertThat( tableNames ).containsExactlyInAnyOrder("A", "B", "C");

     // find in Expressions
     String exprStr = "A.id=B.id and A.age = (select age from C)";
     tableNames = TablesNamesFinder.findTablesInExpression(exprStr);
     assertThat( tableNames ).containsExactlyInAnyOrder("A", "B", "C");


Use the Visitor Patterns
==============================

Casting your way down the tree works for one known shape. For anything general — every column in a query, every table in a script — use a visitor: you override only the node types you care about and the adapters walk the rest.

There is one visitor interface per layer of the model, and an ``..Adapter`` base class for each that already implements the full traversal:

.. list-table::
    :header-rows: 1
    :widths: 35 65

    * - Adapter
      - Reacts to
    * - ``StatementVisitorAdapter``
      - statements: ``Select``, ``Insert``, ``CreateTable``, …
    * - ``SelectVisitorAdapter``
      - query bodies: ``PlainSelect``, ``SetOperationList``, ``WithItem``, …
    * - ``ExpressionVisitorAdapter``
      - expressions: ``Column``, ``Function``, ``EqualsTo``, …
    * - ``FromItemVisitorAdapter``
      - FROM items: ``Table``, ``ParenthesedSelect``, ``TableFunction``, …

.. code-block:: java

    // Define an Expression Visitor reacting on any Expression
    // Overwrite the visit() methods for each Expression Class
    ExpressionVisitorAdapter<Void> expressionVisitorAdapter = new ExpressionVisitorAdapter<>() {
        public <S> Void visit(EqualsTo equalsTo, S context) {
            equalsTo.getLeftExpression().accept(this, context);
            equalsTo.getRightExpression().accept(this, context);
            return null;
        }
        public <S> Void visit(Column column, S context) {
            System.out.println("Found a Column " + column.getColumnName());
            return null;
        }
    };

    // Define a Select Visitor reacting on a Plain Select invoking the Expression Visitor on the Where Clause
    SelectVisitorAdapter<Void> selectVisitorAdapter = new SelectVisitorAdapter<>() {
        @Override
        public <S> Void visit(PlainSelect plainSelect, S context) {
            return plainSelect.getWhere().accept(expressionVisitorAdapter, context);
        }
    };

    // Define a Statement Visitor for dispatching the Statements
    StatementVisitorAdapter<Void> statementVisitor = new StatementVisitorAdapter<>() {
        public <S> Void visit(Select select, S context) {
            return select.getSelectBody().accept(selectVisitorAdapter, context);
        }
    };

    String sqlStr="select 1 from dual where a=b";
    Statement stmt = CCJSqlParserUtil.parse(sqlStr);

    // Invoke the Statement Visitor without a context
    stmt.accept(statementVisitor, null);

.. tip::

    The second parameter of every ``visit()`` is a free-form **context** object of your choosing, threaded through the traversal. Pass ``null`` when you do not need it.


Build a SQL Statement
==============================

The object model works in both directions. Build the tree from Java and print it as SQL:

.. code-block:: java

    String expectedSQLStr = "SELECT 1 FROM dual t WHERE a = b";

    // Step 1: generate the Java Object Hierarchy for
    Table table = new Table().withName("dual").withAlias(new Alias("t", false));

    Column columnA = new Column().withColumnName("a");
    Column columnB = new Column().withColumnName("b");
    Expression whereExpression =
            new EqualsTo().withLeftExpression(columnA).withRightExpression(columnB);

    PlainSelect select = new PlainSelect().addSelectItem(new LongValue(1))
            .withFromItem(table).withWhere(whereExpression);

    // Step 2a: Print into a SQL Statement
    Assertions.assertEquals(expectedSQLStr, select.toString());

    // Step 2b: De-Parse into a SQL Statement
    StringBuilder builder = new StringBuilder();
    StatementDeParser deParser = new StatementDeParser(builder);
    deParser.visit(select);

    Assertions.assertEquals(expectedSQLStr, builder.toString());

The same visitor can render an entire statement list:

.. code-block:: java

    Statements statements = CCJSqlParserUtil.parseStatements("SELECT 1; SELECT 2;");
    StringBuilder script = new StringBuilder();
    statements.accept(new StatementDeParser(script), null);
    Assertions.assertEquals("SELECT 1;\nSELECT 2;\n", script.toString());

Statement lists and nested blocks share the separator policy used by
``Statements.toString()``. Blocks and ``IF/ELSE`` statements retain their own
semicolon settings. Custom deparsers receive each child statement and the
``IF/ELSE`` condition through the visitor API, with the supplied context.


ODBC timestamp intervals
==============================

In ODBC escapes such as ``{fn TIMESTAMPADD(SQL_TSI_YEAR, 2, travel_date)}`` and
``{fn TIMESTAMPDIFF(SQL_TSI_DAY, start_date, end_date)}``, the first argument is a
``DateUnitExpression`` for the nine standard ``SQL_TSI_*`` interval keywords.
The original ODBC keyword is preserved on output and is not visited as a column.
This applies only to unqualified, escaped calls with three arguments and a bare
interval keyword. Ordinary calls, qualified names, quoted identifiers and other
arguments keep their existing expression interpretation.

Handle Parse Errors
==============================

``CCJSqlParserUtil.parse(String, ...)`` requires a statement: null and empty string
inputs throw ``JSQLParserException``, matching the default behavior for whitespace-only
and comment-only input. ``CCJSqlParserUtil.parseStatements(String, ...)`` returns a new,
mutable empty ``Statements`` list for null or empty input, as it already does for
whitespace-only and comment-only input. This applies to the overloads with parser
configuration callbacks and caller-provided executors; caller-provided executors remain
open. These empty-input results replace the previous null returns of these methods.

By default a syntax error aborts the whole parse. Two features let a script survive one bad statement:

- ``parser.withErrorRecovery(true)`` skips to the next statement separator and returns an empty statement.
- ``parser.withUnsupportedStatements(true)`` returns an ``UnsupportedStatement`` holding the raw text instead — though the **first** statement must be a regular one.

.. code-block:: java
    :caption: Error Recovery

    CCJSqlParser parser = new CCJSqlParser(
            "select * from mytable; select from; select * from mytable2" );
    Statements statements = parser.withErrorRecovery().Statements();

    // 3 statements, the failing one set to NULL
    assertEquals(3, statements.size());
    assertNull(statements.get(1));

    // errors are recorded
    assertEquals(1, parser.getParseErrors().size());

.. code-block:: java
    :caption: Unsupported Statement

    Statements statements = CCJSqlParserUtil.parseStatements(
            "select * from mytable; select from; select * from mytable2; select 4;"
            , parser -> parser.withUnsupportedStatements() );

    // 4 statements with one Unsupported Statement holding the content
    assertEquals(4, statements.size());
    assertInstanceOf(UnsupportedStatement.class, statements.get(1));
    assertEquals("select from", statements.get(1).toString());

    // no errors records, because a statement has been returned
    assertEquals(0, parser.getParseErrors().size());

.. note::

    An ``UnsupportedStatement`` is reported as ``OPAQUE`` by :ref:`Classify a Statement` — nothing about its effects is knowable.


Choose a Dialect
==============================

One grammar covers every supported RDBMS, but a few pieces of syntax mean different things in different products. Those are switched with parser features, and a ``Dialect`` preset turns on the right set for you.

.. code-block:: java

    // MySQL: backslash escapes, hash line comments, double-quoted strings
    Statement stmt = CCJSqlParserUtil.parse(
            "SELECT `col` FROM t WHERE a = 'x\\'yz' AND b = 42#24"
            , parser -> parser.withDialect(Dialect.MYSQL) );

.. list-table::
    :header-rows: 1
    :widths: 30 70

    * - ``Dialect``
      - Turns on
    * - ``MYSQL``
      - ``withBackslashEscapeCharacter``, ``withHashLineComments``, ``withDoubleQuotedStrings`` (MySQL and MariaDB, the last for the default ``sql_mode``)
    * - ``SQLSERVER``
      - ``withSquareBracketQuotation`` and ``CLUSTERED`` / ``NONCLUSTERED`` options on table-level primary key and unique constraints and ``CREATE INDEX``
    * - ``POSTGRESQL``
      - tagged dollar strings, the newline rule for ordinary string literals, literal-local ``E'...'`` escapes, and preservation of dots inside quoted names
    * - ``ANSI_SQL``
      - the newline rule for adjacent string literals
    * - ``BIGQUERY``
      - ``withDoubleQuotedStrings``, ``withBackslashEscapeCharacter``, ``withHashLineComments``, any-whitespace rule for adjacent string literals
    * - ``DATABRICKS``
      - ``withDoubleQuotedStrings``, ``withBackslashEscapeCharacter``, any-whitespace rule for adjacent string literals
    * - ``SNOWFLAKE``
      - ``withBackslashEscapeCharacter`` only, double quotes stay quoted identifiers
    * - ``INFORMIX``
      - Informix ``ALTER TABLE ... ADD CONSTRAINT`` definitions with optional trailing constraint names
    * - ``SPANNER``
      - GoogleSQL ``CREATE [UNIQUE] NULL_FILTERED INDEX`` with a separate null-filtering flag
    * - ``DORIS``
      - ``JOIN [shuffle]`` and ``JOIN [broadcast]`` distribution hints
    * - ``COCKROACHDB``
      - ``ALTER TABLE ... ALTER PRIMARY KEY USING COLUMNS (...)`` with optional hash sharding and storage parameters
    * - ``TERADATA``
      - ``UPDATE target FROM sources SET ...`` with the FROM clause before SET

Features set explicitly *after* the preset win over it.

ALTER column names
~~~~~~~~~~~~~~~~~~

Non-reserved names such as ``comment`` work unquoted in ``ADD``, ``MODIFY``,
``CHANGE``, ``DROP`` and ``RENAME`` column actions. The ``COLUMN`` keyword does
not change how the name is interpreted. Column definitions remain editable:

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE t MODIFY COLUMN comment TEXT",
        parser -> parser.withDialect(Dialect.MYSQL));
    AlterExpression.ColumnDataType column = alter.getAlterExpressions().get(0)
        .getColDataTypeList().get(0);
    column.setColumnName("notes");
    column.getColDataType().setDataType("LONGTEXT");
    // ALTER TABLE t MODIFY COLUMN notes LONGTEXT
    String sql = alter.toString();

PostgreSQL names and literals
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Select ``Dialect.POSTGRESQL`` when parsing PostgreSQL SQL. For example,
``DROP INDEX "a.b"`` has the single name ``"a.b"`` and no schema; the dot
inside the quotes is not a separator. This applies to table and column
references throughout DDL and DML. The default configuration retains its
historical name-splitting behavior for compatibility with BigQuery names.

``COMMENT ON TABLE t IS $tag$body$tag$`` accepts tagged dollar strings with
the PostgreSQL preset, or with ``withDollarQuotedStringTags(true)``. Tagged
strings remain disabled by default. Table, column and view comments preserve
their dollar delimiter and literal body. Ordinary single-quoted strings
separated by a newline concatenate; dollar-quoted strings do not.
``E'...'`` enables backslash escapes for that literal without changing
the treatment of ordinary strings elsewhere in the statement.

The shared type grammar accepts negative scales, including
``numeric(2, -3)``, in type fragments, DDL and casts. ``getPrecision()``
returns ``2`` and ``getScale()`` returns ``-3``; an omitted scale returns
``null``. PostgreSQL's precision and scale range checks remain the database's
responsibility. When constructing a ``ColDataType`` directly,
``ColDataType.fromNumericParameters("numeric", 2, -3)`` accepts negative scales;
use ``null`` for omitted parameters. The legacy ``int``
constructor continues to treat negative arguments as omitted parameters.

Other dialect-specific syntax
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

MySQL user-variable targets in ``SELECT ... INTO @variable`` require
``Dialect.MYSQL`` or ``Dialect.MARIADB``. They are stored in
``PlainSelect.getMySqlSelectIntoClause().getVariables()`` as ``UserVariable``
expressions, with the clause position preserved before ``FROM`` or at the end
of the query. They are not table targets in ``getIntoTables()``.

Doris distribution hints require ``parser.withDialect(Dialect.DORIS)``.
``Join.getJoinHint()`` exposes the keyword and ``Position.AFTER_JOIN``;
the existing SQL Server hints use ``Position.BEFORE_JOIN``. Rendering preserves
both the position and the brackets around a Doris hint.

CockroachDB primary-key changes require ``parser.withDialect(Dialect.COCKROACHDB)``.
Their action is an ``AlterExpressionPrimaryKey`` with key elements and storage
parameters in ``getIndex()``. ``isUsingHash()`` preserves ``USING HASH``, while
``getBucketCount()`` holds the legacy ``WITH BUCKET_COUNT = expression`` value.
The newer ``WITH (bucket_count = expression)`` form uses the index storage parameters.

With ``Dialect.TERADATA``, ``UPDATE a FROM target a, source b SET a.id = b.id``
uses the existing ``Update`` model's ``fromItem`` and ``joins`` properties.
``isFromBeforeSet()`` preserves the clause position in both SQL renderers.
Table discovery and metadata validation recognize a target alias declared in
that FROM clause. Other dialects retain the existing FROM-after-SET syntax.

``Dialect.SQLSERVER`` supports methods on expression results, including
``(SELECT ... FOR XML PATH(''), TYPE).value('.', 'varchar(max)')``.
``MethodCallExpression`` exposes the receiver expression and a ``Function``
containing the method name and arguments. Field access and method calls share
the navigation grammar; expression visitors and deparsers traverse both the
receiver and method arguments. XQuery strings remain string literals.

``Dialect.POSTGRESQL`` enables ``DO [LANGUAGE name] code [LANGUAGE name]``,
with the language clause allowed once, before or after the body.
``DoStatement.getCode()`` is a ``StringValue`` that preserves the literal's
quotes, dollar tag and body text. The optional language and its position have
separate properties; an omitted language remains unspecified in the AST.
The body is language-specific source, not a parsed PL/pgSQL statement tree.
Expression visitors can inspect or replace the body literal. Feature analysis
reports ``OPAQUE``; table discovery rejects this statement because the body's
table accesses are unknown. Validation checks the ``doStatement`` capability,
without validating the procedural language inside the literal.

Enable the PostgreSQL dialect when parsing a script containing a ``DO`` block:

.. code-block:: java

    Statements statements = CCJSqlParserUtil.parseStatements(
            "DO $$BEGIN RAISE NOTICE 'hello'; END$$; SELECT 1;",
            parser -> parser.withDialect(Dialect.POSTGRESQL));
    DoStatement block = (DoStatement) statements.get(0);
    String body = block.getCode().getValue();
    // body: BEGIN RAISE NOTICE 'hello'; END
    // statements.get(1) is the following SELECT.

Semicolons and SQL statements inside the body remain part of its string literal;
they do not split the surrounding script into additional statements.

PostgreSQL ``CREATE FUNCTION`` and ``CREATE PROCEDURE`` declarations also preserve
``AS '...'``, ``AS E'...'`` and dollar-quoted bodies as opaque text. Their
``getFunctionDeclarationParts()`` list retains the body and trailing options,
while a following statement is parsed separately. Newline-separated body string
continuations retain the newline needed when the declaration is rendered again.
Use ``Dialect.POSTGRESQL`` for tagged dollar quotes and PostgreSQL-specific DDL
such as schema-qualified index collations and operator classes. This support
does not validate PL/pgSQL source or build an AST for statements inside the body.

With ``Dialect.POSTGRESQL``, ``#`` terminates an unquoted identifier, so JSON
operators such as ``js#>>'{a}'`` and ``js#>'{a}'`` work without surrounding
spaces. Quote identifiers containing ``#``, for example ``"js#"``. Other
dialects retain their existing identifier and hash-comment rules.

With ``Dialect.SQLSERVER``, ``SET NOCOUNT ON`` and grouped boolean options such as
``SET QUOTED_IDENTIFIER, ANSI_NULLS OFF`` use ``SetStatement.getOnOffOptions()``.
The ordered ``OnOffOption`` list and shared ``isOn()`` value are editable;
``setOnOffOptions()`` replaces generic assignments and their scope. Both SQL
renderers share statement punctuation while generic assignments retain expression
visitor support. Parsing a SET directive records it without changing lexer settings.
``Dialect.SQLSERVER`` enables ``INSERT BULK table (name type, ...) WITH (...)``.
``InsertBulk`` exposes the target table, existing ``ColumnDefinition`` models,
and ordered typed options, including ``ROWS_PER_BATCH`` and ``ORDER`` keys.
The SQL declaration is preserved; the following binary bulk-load data stream
is outside the SQL parser. Visitors and deparsers traverse option values and
ordering expressions. Validation uses the ``insertBulk`` capability.

With ``Dialect.SQLSERVER``, ``PRIMARY KEY NONCLUSTERED (id)`` and
``UNIQUE CLUSTERED (id)`` store their clustering option in ``Index.getClustering()``
for both ``CREATE TABLE`` and ``ALTER TABLE``. Without that dialect, these words
retain their existing interpretation as optional index names.
SQL Server ``CREATE TABLE`` also accepts a trailing comma after the final column
or table constraint. SQL output normalizes the definition by omitting that comma.

``CREATE UNIQUE NONCLUSTERED INDEX ix ON t (id)`` also requires
``Dialect.SQLSERVER``. Uniqueness remains in ``Index.getType()`` and clustering
is stored separately in ``Index.getClustering()``. With ``Dialect.SPANNER``,
``CREATE UNIQUE NULL_FILTERED INDEX ix ON t (id)`` stores null filtering in
``CreateIndex.isNullFiltered()``. An omitted clustering or null-filtering option
is not supplied from database defaults. The Spanner preset currently selects
this index syntax; it does not configure GoogleSQL string-literal rules.

With ``Dialect.POSTGRESQL``, index keys accept schema-qualified collation and
operator-class names, for example ``name COLLATE pg_catalog."C"
pg_catalog.text_ops ASC NULLS LAST``. Function keys such as ``lower(name)`` are
stored as expressions. Key attributes are available through ``getCollation()``,
``getOperatorClass()``, ``getOperatorClassParameters()``, ``getSortOrder()`` and
``getNullOrdering()`` on ``Index.ColumnParams``. Under this dialect these
attributes are not duplicated in the legacy ``getParams()`` list, so changing
or removing them is reflected when rendering SQL. Other dialects retain the
legacy parameter representation, including MySQL prefix lengths.

``CreateIndexDeParser`` and ``StatementDeParser`` pass key expressions,
structured option values and the partial-index predicate to their expression
visitor. ``StatementVisitorAdapter``, ``TablesNamesFinder`` and index validation
traverse the same structured expressions.

Informix's constraint form requires an explicit dialect selection:

.. code-block:: java

    Statement stmt = CCJSqlParserUtil.parse(
            "ALTER TABLE child ADD CONSTRAINT FOREIGN KEY (id) "
                    + "REFERENCES parent(id) CONSTRAINT fk_child",
            parser -> parser.withDialect(Dialect.INFORMIX));

Row pattern matching (MATCH_RECOGNIZE)
--------------------------------------

``MatchRecognize`` is a ``FromItem`` wrapping the input relation. Its input
alias and output alias are independent. ``getMeasures()`` and
``getDefinitions()`` expose ordinary SQL expressions; ``getPattern()`` exposes
an editable ``RowPattern`` tree with variables, groups, ordered alternatives,
sequences, anchors and quantifiers. Oracle and Snowflake additionally support
``PERMUTE`` and exclusion nodes. Permutations remain compact in the AST.

.. code-block:: java

    PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
        "SELECT * FROM events MATCH_RECOGNIZE ("
            + "ORDER BY seq MEASURES SUM(A.price) AS total "
            + "PATTERN (A+) DEFINE A AS price > 0 "
            + "OPTIONS (use_longest_match = TRUE))",
        parser -> parser.withDialect(Dialect.BIGQUERY));
    MatchRecognize match = (MatchRecognize) select.getFromItem();
    RowPattern.Quantified repetition = (RowPattern.Quantified) match.getPattern();
    repetition.setReluctant(true);
    System.out.println(select); // PATTERN (A+?)

``FromItemVisitorAdapter`` traverses the input and clause expressions.
``RowPatternVisitorAdapter`` traverses pattern nodes and optionally visits
expressions in quantifier bounds. ``RowPatternFunction`` represents an explicit
``RUNNING`` or ``FINAL`` prefix while exposing the underlying ``Function`` to
existing expression visitors. Both ``toString()`` and the deparsers render the
current AST, including edited nested ``STRUCT`` arguments.

Select the source dialect explicitly:

* ``BIGQUERY`` requires ``ORDER BY``, ``MEASURES``, ``PATTERN`` and ``DEFINE``.
  It supports empty alternatives, literal or query-parameter bounds, adjacent
  anchors such as ``^A+$``, and ``OPTIONS (use_longest_match = TRUE/FALSE)``.
* ``ORACLE`` supports optional ordering/measures, row output modes, targeted
  ``AFTER MATCH SKIP``, ``SUBSET``, permutations, exclusions and function modes.
* ``SNOWFLAKE`` supports the corresponding documented forms except ``SUBSET``
  and BigQuery options. Its documented alternation-before-concatenation
  precedence is used for the pattern AST. Other presets use
  concatenation-before-alternation. Rendering adds explicit parentheses at
  mixed operator boundaries; it does not translate expression syntax between DBs.

Validation checks the ``matchRecognize`` and ``matchRecognizeOptions`` features.
With an explicit dialect, it also checks selected static rules: pattern variable
names, bounds, subset/skip targets and incompatible clause options. Table
finding does not treat pattern qualifiers such as ``A.price`` as table names.
Metadata validation does not bind these columns to the input relation.
Function eligibility, aggregation/type rules, data-dependent skip failures and
query execution remain the database's responsibility.

The test fixtures were compared before and after deparsing using GoogleSQL's
reference evaluator and Oracle 26ai Free. The reference evaluator is not the
BigQuery service. Snowflake coverage follows its documentation and parser
round-trip tests; it has not been executed against a Snowflake account.

See the official `BigQuery MATCH_RECOGNIZE syntax
<https://docs.cloud.google.com/bigquery/docs/reference/standard-sql/query-syntax#match_recognize_clause>`_,
`Oracle row pattern matching guide
<https://docs.oracle.com/en/database/oracle/oracle-database/26/dwhsg/sql-pattern-matching-data-warehouses.html>`_
and `Snowflake MATCH_RECOGNIZE reference
<https://docs.snowflake.com/en/sql-reference/constructs/match_recognize>`_.

The individual features
------------------------------

.. list-table::
    :header-rows: 1
    :widths: 35 65

    * - Feature
      - What it changes
    * - ``withSquareBracketQuotation``
      - ``[..]`` reads as a quoted identifier instead of an array — needed for T-SQL on MS SQL Server and Sybase
    * - ``withBackslashEscapeCharacter``
      - ``\\..`` escaping inside string literals, in addition to the standard ``'..`` doubling
    * - ``withDoubleQuotedStrings``
      - ``".."`` reads as a string literal instead of a quoted identifier (BigQuery, Spark/Databricks, MySQL default ``sql_mode``)
    * - ``withHashLineComments``
      - ``#`` starts a line comment
    * - ``withAdjacentStringLiterals``
      - adjacent string literals concatenate: ``NEWLINE`` (SQL standard, PostgreSQL) or ``WHITESPACE`` (GoogleSQL, Spark/Databricks); ``true`` selects ``NEWLINE``, ``false`` switches it off
    * - ``withAllowComplexParsing``
      - permits deeply nested expressions, at a significant performance cost
    * - ``withTimeOut``
      - aborts parsing after N milliseconds

.. code-block:: java

    String sqlStr="select 1 from [sample_table] where [a]=[b]";

    // T-SQL Square Bracket Quotation
    Statement stmt = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withSquareBracketQuotation(true)
    );

    // Set Parser Timeout to 6000 ms
    Statement stmt1 = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withSquareBracketQuotation(true)
                .withTimeOut(6000)
    );

    // Allow Complex Parsing (which allows nested Expressions, but is much slower)
    Statement stmt2 = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withSquareBracketQuotation(true)
                .withAllowComplexParsing(true)
                .withTimeOut(6000)
    );

    // Allow Back-slash escaping
    sqlStr="SELECT ('\\'Clark\\'', 'Kent')";
    Statement stmt2 = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withBackslashEscapeCharacter(true)
    );

Things that trip people up
------------------------------

.. hint::

    1) **Quoting:** Double quotes ``".."`` quote identifiers. Square brackets ``[..]`` are arrays unless you turn on ``withSquareBracketQuotation``.

    2) **Reserved keywords:** JSQLParser uses a more restrictive list than most databases, and such keywords **need to be quoted**.

    3) **Escaping:** standard single-quote ``'..`` escaping is always on. Backslash escaping is not — set ``withBackslashEscapeCharacter``.

    4) **Oracle alternative quoting** is partially supported, for common brackets: ``q'{...}'``, ``q'[...]'``, ``q'(...)'`` and ``q''...''``.


Compile from Source Code
==============================

You need ``JDK 8`` or ``JDK 11``. JSQLParser-4.9 is the last ``JDK 8`` compatible release; everything after depends on ``JDK 11``. Building JSQLParser-5.1 and newer with Gradle needs a JDK 17 toolchain, because of the plugins used.

.. tab:: Maven

  .. code-block:: shell

    git clone --depth 1 https://github.com/JSQLParser/JSqlParser.git
    cd JSqlParser
    mvn install

.. tab:: Gradle

  .. code-block:: shell

    git clone --depth 1 https://github.com/JSQLParser/JSqlParser.git
    cd JSqlParser
    gradle publishToMavenLocal

PostgreSQL roles, privileges and triggers
-----------------------------------------

``CreateRole`` and ``AlterRole`` model role attributes and configuration changes,
including PostgreSQL's USER/GROUP aliases. Role options are ordered and typed;
passwords and configuration values are expressions. Omitted options remain
omitted. ``CREATE USER name`` without attributes retains the existing MySQL
``CreateUser`` AST by default. Select ``Dialect.POSTGRESQL`` explicitly to obtain
the PostgreSQL ``CreateRole`` AST for this ambiguous form::

    CreateRole user = (CreateRole) CCJSqlParserUtil.parse(
        "CREATE USER app",
        parser -> parser.withDialect(AbstractJSqlParser.Dialect.POSTGRESQL));

``Grant`` retains its string-based getters, setters and fluent methods. Its
``PrivilegeClause`` exposes typed ``Privilege`` items, column lists,
``PrivilegeTarget`` kinds, multiple role memberships and grantor information.
``getPrivileges()`` is a mutable string view of the typed privilege list.
``getRole()`` and ``getObjectName()`` expose the first role or named target for
legacy callers; use ``getRoles()`` and ``getTarget()`` for the complete lists.
Names in ``PrivilegeTarget`` are multipart identifiers, not SQL clause text.
Routine targets use ``RoutineReference`` data-type signatures, with null
arguments for an omitted signature and an empty list for explicit ``()``.

``Revoke`` shares the privilege payload and adds the revoked option and
CASCADE/RESTRICT behavior. ``AlterDefaultPrivileges`` has separate role/schema
scope and one nested GRANT or REVOKE. Table-name discovery reports explicit
table targets, not schema-wide targets, routine/type names or role names.

``CreateTrigger`` supports both its existing MySQL statement body and a distinct
PostgreSQL routine invocation. PostgreSQL fields include multiple events,
UPDATE OF columns, constraint attributes, transition relations, row/statement
orientation and a WHEN expression. The absence of FOR ROW/STATEMENT is retained.
Expression visitors and ``StatementDeParser`` traverse privilege columns,
role values, trigger conditions and invocation arguments. Declaring a trigger
is classified as a schema change, not execution of its body.

Capability validation covers these statement families, not every server-version
or catalog-dependent restriction. EVENT TRIGGER is outside this support.
Serialized role SQL can contain passwords; avoid logging real credentials.

References: `CREATE ROLE <https://www.postgresql.org/docs/18/sql-createrole.html>`_,
`ALTER ROLE <https://www.postgresql.org/docs/18/sql-alterrole.html>`_,
`GRANT <https://www.postgresql.org/docs/18/sql-grant.html>`_,
`REVOKE <https://www.postgresql.org/docs/18/sql-revoke.html>`_,
`ALTER DEFAULT PRIVILEGES <https://www.postgresql.org/docs/18/sql-alterdefaultprivileges.html>`_,
`CREATE TRIGGER <https://www.postgresql.org/docs/18/sql-createtrigger.html>`_.

Oracle anonymous blocks
-----------------------

With ``Dialect.ORACLE``, ``OracleBlock`` extends ``Block`` with variable declarations
and exception handlers. Initializers and ``OracleAssignment`` values are expressions;
nested blocks and handler bodies contain statements. Calls without ``CALL`` use
``Execute.ExecType.IMPLICIT``, preserving qualified names, parentheses and bind arguments.
``OracleNullStatement`` represents the PL/SQL ``NULL`` statement. Implicit calls are
recognized inside Oracle blocks, so application procedure names need no keyword registration.

Shared traversal and rendering include declaration initializers, assignments and exception
handler bodies. Procedure side effects remain unknown; table discovery reports unsupported
procedure calls, and feature analysis remains conservative. This covers anonymous blocks
with variable declarations, SQL statements, assignments, calls, nesting and handlers, not
all PL/SQL declarations, loops, packages or procedure definitions.

SQL Server routine declarations
-------------------------------

``Dialect.SQLSERVER`` uses a shared declaration path for ``CREATE``, ``ALTER`` and
``CREATE OR ALTER FUNCTION/PROCEDURE``. ``CreateFunctionalStatement.getOperation()``
identifies the operation. For functions, ``getReturnType()`` exposes scalar types,
inline ``RETURNS TABLE``, and a return variable with ordered ``TableElement`` column
and constraint definitions. Table elements reuse the existing definition traversal
and deparser, including custom expression visitors.

With a structured return type, ``getFunctionDeclarationParts()`` contains the name
and parameter tokens; ``getRoutineBodyParts()`` contains the following options and
body. These remain opaque tokens, so this does not implement a T-SQL body AST or
resolve tables used inside a routine. Other dialects retain the existing token-list
representation. New operations have separate validation capabilities.

Parse procedure definitions one SQL Server batch at a time: a procedure consumes the
remaining batch, including SQL after an ``END``. Client-side ``GO`` batch splitting is
not performed by this routine declaration parser.

PostgreSQL table-level NOT NULL constraints
------------------------------------------

``Dialect.POSTGRESQL`` supports PostgreSQL 18's table-level
``CONSTRAINT nn NOT NULL id``, in both CREATE TABLE and ALTER TABLE ADD.
``NotNullConstraint`` exposes the constraint name, target ``Column`` and
``noInherit`` flag; ``getConstraintAttributes().isNotValid()`` represents an
ALTER ``NOT VALID`` clause when present.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE t ADD CONSTRAINT nn NOT NULL id NOT VALID",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    NotNullConstraint constraint = (NotNullConstraint)
        alter.getAlterExpressions().get(0).getIndex();
    constraint.getColumn().setColumnName("other_id");
    constraint.setName("other_nn");

The target column participates in expression visitors and deparsers. New nodes
can be built with ``new NotNullConstraint().withName("nn")
.withColumn(new Column("id"))``. This is distinct from column definitions and
``ALTER COLUMN ... SET NOT NULL``; those retain their existing APIs.

SQL Server identity inserts
---------------------------

With ``Dialect.SQLSERVER``, ``SET IDENTITY_INSERT dbo.actor ON`` uses
``SetIdentityInsertStatement``. ``getTable()`` reuses the qualified ``Table`` AST;
``isOn()`` and ``setOn()`` expose the session setting. Table names may include a
database and schema, including SQL Server bracket-quoted identifiers. Table
visitors, both SQL renderers and metadata validation use this structured target.
Feature analysis reports ``MODIFIES_SESSION``; the directive itself inserts no rows.
The dedicated validation capability is ``setIdentityInsert``.

Legacy MySQL GROUP BY ordering
==============================

MySQL before 8.0.13 accepted ``ASC`` and ``DESC`` on individual ``GROUP BY`` items.
Select the existing ``MYSQL`` dialect and explicitly enable this legacy syntax:

.. code-block:: java

    Statement statement = CCJSqlParserUtil.parse(
        "SELECT a FROM t GROUP BY a DESC",
        parser -> parser.withDialect(Dialect.MYSQL).withLegacyMySqlGroupBy(true));

The option is disabled by default and does not enable this syntax in other dialects.
``GroupByElement`` keeps its existing expression list; ``getGroupBySortDirection(index)``
returns each explicit direction, or null when omitted. Directions follow list positions;
replacing the expression list clears them. Validators report the separate
``selectGroupByOrdering`` feature, which is not enabled in the MySQL 8.0 capability.

PostgreSQL COMMENT targets
=========================

``COMMENT ON`` supports ``INDEX``, ``SCHEMA``, ``SEQUENCE``, ``DOMAIN``, ``TYPE``,
``MATERIALIZED VIEW``, ``FUNCTION`` and ``CONSTRAINT`` in addition to the existing
``TABLE``, ``COLUMN`` and ``VIEW`` forms. These unambiguous target forms also parse
without a dialect preset. Tagged dollar strings still require
``Dialect.POSTGRESQL`` or ``withDollarQuotedStringTags(true)``.

The additional targets are exposed through ``Comment.getTarget()`` as a
``CommentTarget``. Its ``Kind`` identifies the object, and ``getName()`` preserves
the individual identifier components. ``Table`` is used as the name container;
an index or type name does not thereby represent a table dependency.
The original ``Comment.getTable()``, ``getColumn()`` and ``getView()`` accessors
continue to describe their respective existing forms.

``COMMENT ON FUNCTION app.f(IN value integer) IS 'description'`` uses the shared
``RoutineReference`` in ``getTarget().getRoutine()``. Argument mode, optional name
and ``ColDataType`` are preserved. An omitted signature has null arguments;
``f()`` has an empty argument list. The signature identifies a function and is not
a function call.

For ``COMMENT ON CONSTRAINT ck ON app.t IS NULL``, ``getName()`` is the constraint
name and ``getRelation()`` is its owning table. ``ON DOMAIN app.d`` sets
``isOnDomain()`` and stores the domain as the owner instead. ``NULL`` removes the
comment and remains represented by a null ``Comment.getComment()``.

Table discovery visits tables, column owners, views, materialized views and
table-owned constraints. It does not infer an index's table or treat domains,
sequences, types or functions as tables. Statement visitors visit comment
literals, and custom SQL deparsers can replace the explicit relation or literal.
Feature analysis reports a schema modification. Validation exposes a separate
``commentOn...`` capability for each additional target kind.

Current date/time expression metadata
=====================================

``TemporalExpressionInfo.from(expression, dialect)`` provides a common read-only
view of MySQL and PostgreSQL current-date/time expressions. It recognizes the
existing ``TimeKeyExpression``, ``Function`` and ``Column`` representations without
replacing nodes or changing their SQL rendering:

.. code-block:: java

    PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
            "SELECT CURRENT_TIMESTAMP(6)", p -> p.withDialect(Dialect.POSTGRESQL));
    TemporalExpressionInfo info = TemporalExpressionInfo.from(
            select.getSelectItem(0).getExpression(), Dialect.POSTGRESQL).orElseThrow();
    // info.getKind() == TemporalExpressionInfo.Kind.CURRENT_TIMESTAMP
    // info.getPrecision() == 6

Import ``TemporalExpressionInfo`` from ``net.sf.jsqlparser.util``. Precision is
``null`` when omitted, and explicit zero is preserved. ``getName()`` retains the
original keyword or function name; ``hasParentheses()`` distinguishes bare and
call forms. Results are snapshots: call ``from`` again after editing an AST.

The dialect is significant. MySQL ``LOCALTIME`` and ``LOCALTIMESTAMP`` are aliases
of ``CURRENT_TIMESTAMP``; PostgreSQL exposes ``LOCAL_TIME`` and
``LOCAL_TIMESTAMP`` separately. MySQL ``NOW``, ``CURDATE`` and ``CURTIME`` aliases
are recognized in their function-call forms. PostgreSQL ``now()`` is recognized
without precision arguments. Quoted or qualified identifiers, unrelated
expressions and unsupported dialects return ``Optional.empty()``.

This API identifies expression metadata rather than performing database
validation. Precision reports the requested value, without applying defaults,
server range checks or clamping. For example, PostgreSQL accepts precision 7 with
a warning and clamps it to 6, whereas MySQL rejects it.

Attach a PostgreSQL constraint to an existing index
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

With ``Dialect.POSTGRESQL``, ``ALTER TABLE ... ADD UNIQUE USING INDEX`` and
``ADD PRIMARY KEY USING INDEX`` expose a ``ConstraintUsingIndex`` through
``AlterExpression.getIndex()``. Its ``getName()`` is the optional new constraint
name, while ``getExistingIndexName()`` identifies the existing index. This is
separate from an index declaration's name, columns and access method.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE t ADD CONSTRAINT uq UNIQUE USING INDEX i",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    ConstraintUsingIndex constraint = (ConstraintUsingIndex)
        alter.getAlterExpressions().get(0).getIndex();
    constraint.setExistingIndexName("replacement_index");
    constraint.setName("replacement_constraint");

``getConstraintAttributes()`` exposes deferrability and initial timing when
present. The same AST can be constructed with ``new ConstraintUsingIndex()``
and its fluent ``withName``, ``withType`` and ``withExistingIndexName`` methods.
