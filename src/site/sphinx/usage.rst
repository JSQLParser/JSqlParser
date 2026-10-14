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
    Use ``withBlankLinesAsSeparator(false)`` to treat empty lines as plain whitespace instead.

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

DROP targets
------------

``Drop`` keeps the parts of a DROP statement that are not plain object names in
typed properties:

* FUNCTION, PROCEDURE, ROUTINE and AGGREGATE targets are ``RoutineReference``
  items in ``getRoutines()``, the model that GRANT and COMMENT use. Every target
  keeps its own signature, so ``DROP PROCEDURE p(int)`` and
  ``DROP FUNCTION f(int), g(text)`` render as written. Null arguments mean an
  omitted signature; an empty list is an explicit ``()``.
* USER, ROLE and GROUP targets and the roles of PostgreSQL ``DROP OWNED BY`` are
  ``UserAccount`` items in ``getAccounts()``, read with the MySQL account-name rule
  that CREATE USER uses. MySQL accounts keep their host, as in ``'app'@'%'``,
  and a quoted PostgreSQL role name such as ``"Admin"`` keeps ``"`` in
  ``getQuoteStr()``.
* ``DROP TRIGGER t ON tbl`` and ``DROP RULE r ON tbl`` expose ``tbl`` through
  ``getTable()``, like ``DROP INDEX i ON tbl``, and table discovery reports it.
  SQL Server's ``ON DATABASE`` and ``ON ALL SERVER`` trigger scopes stay
  parameters.
* ``getBehavior()`` holds an explicit CASCADE or RESTRICT as a
  ``CascadeBehavior``. It is rendered last.
* ``isUndo()``, ``getEngine()`` and ``isEngineUseEquals()`` cover MySQL
  ``DROP [UNDO] TABLESPACE name [ENGINE [=] engine]``. ``isForce()`` covers
  PostgreSQL ``DROP DATABASE name [WITH] (FORCE)``; the optional WITH is
  always rendered.

TYPE, DOMAIN, EVENT, EVENT TRIGGER, TABLESPACE, USER and GROUP are recognized
object types. PostgreSQL ``REASSIGN OWNED BY old_role [, ...] TO new_role`` is
parsed into ``ReassignOwned``, whose roles use the same ``UserAccount`` model.

.. code-block:: java

    Drop drop = (Drop) CCJSqlParserUtil.parse("DROP FUNCTION f(int), g(text) CASCADE",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    RoutineReference g = drop.getRoutines().get(1);
    g.getArguments().get(0).setDataType(new ColDataType("varchar"));
    // DROP FUNCTION f(int), g(varchar) CASCADE
    String sql = drop.toString();

The legacy accessors remain and are derived from these properties:

* ``getName()`` and ``getNames()`` return read-only snapshots of routine and
  account names. Edit ``getRoutines()`` or ``getAccounts()`` to change them.
* ``getTypeToParameters()`` and ``getParamsByType()`` return a read-only map with
  the argument declarations of the last routine, where the legacy
  single-signature form kept them. The map now also covers PROCEDURE, ROUTINE
  and AGGREGATE.
* ``getParameters()`` is a snapshot that includes ON with the owner table, the
  MySQL ALGORITHM and LOCK clauses and the CASCADE or RESTRICT keyword, in
  source order. ``setParameters()`` replaces the clause with raw tokens and
  clears all three; ``addParameters()`` keeps them. Raw tokens are not parsed:
  ``addParameters("CASCADE")`` leaves ``getBehavior()`` null.
* ``setName()``, ``setNames()``, ``addNames()`` and ``setTypeToParameters()``
  first convert routine and account targets to the legacy names and single
  signature, so ``setName()`` on ``DROP FUNCTION f(int)`` still renders
  ``DROP FUNCTION g(int)``. Use ``getRoutines()`` to keep several signatures.

Migration: mutating the map returned by ``getTypeToParameters()`` for a parsed
routine now throws ``UnsupportedOperationException``; edit its
``RoutineReference`` instead. ``getObjectType()`` reports the new ``AGGREGATE``,
``DOMAIN``, ``EVENT_TRIGGER``, ``GROUP``, ``OWNED``, ``ROLE``, ``ROUTINE``,
``RULE``, ``TYPE`` and ``USER`` kinds where it reported ``OTHER``.
``DROP OWNED BY r`` no longer returns the name ``BY``.

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
MySQL ALGORITHM and LOCK clauses retain their order and optional equals signs as
typed nodes; see :ref:`Share MySQL index options`.

MySQL named constraint drops
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Without a dialect and with ``Dialect.MYSQL``, ``DROP FOREIGN KEY name`` and
``DROP CHECK name`` expose the target through ``AlterExpression.getConstraintName()``.
``Dialect.MARIADB`` accepts the named FOREIGN KEY form; MariaDB has no ``DROP CHECK``.
Other dialects drop constraints with ``DROP CONSTRAINT``. A parenthesized
``DROP FOREIGN KEY (...)`` keeps the legacy column-list form. Inspect
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

Edit table and view column renames
----------------------------------

``AlterExpression.getColumnRename()`` exposes a ``ColumnRenameAction`` with
``getSourceName()`` and ``getTargetName()``. Their setters update the existing
AST directly, preserving the optional ``COLUMN`` keyword. Names include their
written identifier quotes; include quotes in replacement names when needed.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE accounts RENAME COLUMN old_id TO new_id",
        parser -> parser.withDialect(Dialect.MYSQL));
    ColumnRenameAction rename = alter.getAlterExpressions().get(0).getColumnRename();
    rename.getSourceName(); // old_id
    rename.setTargetName("customer_id");
    // ALTER TABLE accounts RENAME COLUMN old_id TO customer_id
    String sql = alter.toString();

Import ``ColumnRenameAction`` from ``net.sf.jsqlparser.statement.alter``.
PostgreSQL table renames of columns use the same API. With ``Dialect.POSTGRESQL``,
column renames on views and materialized views expose it through the entries in
``AlterRelation.getActions()``. The lookup returns ``null`` for other operations,
including renaming the table, view, index or constraint itself. Existing
action-specific getters and setters remain available and see the same changes.

Shared CASCADE and RESTRICT behavior
------------------------------------

``AlterDomain``, ``AlterType`` (including ``AttributeChange``), ``DropPolicy``,
``Truncate`` and ``Revoke`` use ``net.sf.jsqlparser.statement.CascadeBehavior``
for their explicit ``CASCADE`` and ``RESTRICT`` clauses. A null value preserves
an omitted clause; it does not insert an explicit ``RESTRICT``.

.. code-block:: java

    import net.sf.jsqlparser.statement.CascadeBehavior;

    truncate.setDropBehavior(CascadeBehavior.RESTRICT);
    revoke.setBehavior(truncate.getDropBehavior());
    revoke.setBehavior(null); // Omit the clause and leave the server default unchanged.

Migration: the nested enums ``AlterDomain.Behavior``, ``AlterType.Behavior``,
``DropPolicy.Behavior``, ``Truncate.DropBehavior`` and ``Revoke.Behavior`` have
been removed. Replace their imports and enum references with ``CascadeBehavior``.
The existing getter and setter names remain, but their return and parameter types
have changed, so clients must recompile. ``Truncate.setCascade(false)`` still
clears the clause. Foreign-key referential actions retain their separate enum.

``ALTER TABLE`` drop actions expose the same enum through
``AlterExpressionDrop.getBehavior()``, for example in
``ALTER TABLE t DROP COLUMN IF EXISTS c RESTRICT`` and
``ALTER TABLE t DROP CONSTRAINT c CASCADE``. Oracle's ``CASCADE CONSTRAINTS`` and
``INVALIDATE`` remain raw parameters. ``getParameters()`` still ends with the
behavior keyword; when a behavior is set, it returns a read-only snapshot, so use
``setBehavior`` to change the clause.

Edit shared table options
-------------------------

``CreateTable.getTableOptions()`` and
``AlterExpressionTableOption.getStructuredTableOption()`` use the same mutable
``TableOption`` model. Structured values and opaque tokens are alternative
representations. ``setTokens(nonNullList)`` discards the previous kind, name,
value and child nodes: the option becomes ``Kind.OTHER`` without parsing the
tokens. Raw token lists remain mutable; ``getTokens()`` on a structured option
returns an unmodifiable snapshot.

.. code-block:: java

    CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
            "CREATE TABLE t (id INT) ENGINE=InnoDB");
    TableOption option = table.getTableOption(TableOption.Kind.ENGINE).orElseThrow();
    option.setTokens(List.of("ROW_FORMAT", "=", "DYNAMIC"));
    table.getTableOption(TableOption.Kind.ENGINE).isEmpty(); // true

    // Supply structured properties explicitly; the old tokens are discarded.
    option.withKind(TableOption.Kind.COMMENT).withName("COMMENT")
            .withValue("'new'").withUseEquals(true);
    // CREATE TABLE t (id INT) COMMENT = 'new'

Any structured setter leaves the opaque representation; it never recovers
previous values from those tokens. ``setTokens(null)`` clears opaque tokens
without restoring discarded properties, and leaves structured options alone.
``setUnionTables``, ``setStorageParameters``, ``setForeignTableOptions`` and
``setOnCommitAction`` replace the current contents with their respective
structured payloads. Their lists and child nodes remain editable. An empty
UNION list means ``UNION ()``;
a null payload omits its value. A non-null ``setTablespaceStorage`` also selects
TABLESPACE; supply its tablespace name with ``setValue``. Choose the new kind and spelling explicitly
when using the scalar setters. These methods edit the AST rather than checking
database-specific DDL validity.

A temporary table's ``ON COMMIT`` clause and a table access method are single
options. ``Kind.ON_COMMIT`` exposes ``getOnCommitAction()``: ``PRESERVE_ROWS``,
``DELETE_ROWS`` or ``DROP``, plus ``PRESERVE_DEFINITION`` and ``DROP_DEFINITION``
for Oracle private temporary tables. ``Kind.ACCESS_METHOD`` stores the method of
``USING method`` as its value, including ``CREATE TABLE … USING method AS query``.
Use ``TableOption.onCommit`` and ``TableOption.accessMethod`` to build them.
``getTableOptionsStrings()`` keeps the legacy word tokens, such as ``ON``,
``COMMIT``, ``DELETE`` and ``ROWS``.

.. code-block:: java

    CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
            "CREATE TEMP TABLE t (id INT) USING heap ON COMMIT DROP",
            parser -> parser.withDialect(Dialect.POSTGRESQL));
    table.getTableOption(TableOption.Kind.ACCESS_METHOD).orElseThrow().getValue(); // heap
    table.getTableOption(TableOption.Kind.ON_COMMIT).orElseThrow()
            .setOnCommitAction(TableOption.OnCommitAction.PRESERVE_ROWS);
    // CREATE TEMP TABLE t (id INT) USING heap ON COMMIT PRESERVE ROWS

Share MySQL index options
-------------------------

CREATE INDEX, the index and key definitions of CREATE TABLE, and ALTER TABLE
``ADD`` or ``ALTER INDEX`` parse MySQL index options with one grammar rule into
one model. ``IndexOptions.getSpecOptions()``, also available on ``Index`` and
``KeyConstraint``, returns the options written after the key list as mutable,
ordered ``IndexSpecOption`` nodes. ``getKind()`` selects the payload:

* ``KEY_BLOCK_SIZE``, ``ENGINE_ATTRIBUTE`` and ``SECONDARY_ENGINE_ATTRIBUTE``:
  ``getValue()`` and the optional ``=`` reported by ``isUseEquals()``
* ``INDEX_TYPE`` (``USING BTREE``) and ``PARSER`` (``WITH PARSER ngram``):
  ``getValue()``
* ``COMMENT``: ``getComment()`` returns the literal as a ``StringValue`` that
  records how the dialect escapes it, so ``getNotExcapedValue()`` returns the
  stored text, for example ``it's`` for ``'it''s'`` or, with MySQL backslash
  escapes, ``'it\'s'``. ``getValue()`` remains the literal's SQL text.
* ``VISIBILITY``: ``getVisible()`` for VISIBLE or INVISIBLE
* ``OTHER``: opaque tokens that CREATE TABLE and CREATE INDEX accept for other
  dialects

Values keep their SQL spelling, including the quotes of string literals. An
index type written before the key list, as in ``KEY k USING BTREE (a)``,
``PRIMARY KEY USING BTREE (id)`` or ``CREATE INDEX k USING HASH ON t (a)``,
remains ``getUsing()``; ``CreateIndex.isIndexTypeBeforeOn()`` still distinguishes
the two CREATE INDEX positions. After the key list it is an ``INDEX_TYPE`` option
at its source position. PostgreSQL's INCLUDE, NULLS DISTINCT, storage parameters
and tablespace keep their dedicated properties.

CREATE INDEX keeps these options on ``getIndex()``. Its ALGORITHM and LOCK
clauses are ``AlgorithmOrLockOption`` nodes in
``CreateIndex.getAlgorithmOrLockOptions()``; DROP INDEX exposes the same type
through ``Drop.getAlgorithmOrLockOptions()``. CREATE INDEX renders them after the
index options, as MySQL requires.

.. code-block:: java

    CreateIndex create = (CreateIndex) CCJSqlParserUtil.parse(
            "CREATE UNIQUE INDEX uk ON t (a) USING BTREE COMMENT 'x' ALGORITHM = INPLACE",
            parser -> parser.withDialect(Dialect.MYSQL));
    List<IndexSpecOption> options = create.getIndex().getSpecOptions();
    options.get(0).setValue("HASH");                    // INDEX_TYPE
    options.add(IndexSpecOption.visibility(false));
    create.getAlgorithmOrLockOptions().get(0).setValue("COPY");
    // CREATE UNIQUE INDEX uk ON t (a) USING HASH COMMENT 'x' INVISIBLE ALGORITHM = COPY

The kind of an option is fixed. Setters for a payload that the kind does not have,
and ``setTokens`` on a typed option, throw ``IllegalStateException``; replace the
node to change its kind. ``IndexSpecOption.raw`` copies its tokens and never
parses them.

The legacy accessors are derived from these nodes. ``getIndexSpec()`` returns an
unmodifiable token snapshot of ``getSpecOptions()``, or null when the options are
unset. ``CreateIndex.getTailParameters()`` returns the tokens of the index options
followed by ALGORITHM and LOCK, and ``Drop.getParameters()`` includes ALGORITHM
and LOCK at their source position. ``setIndexSpec`` and
``CreateIndex.setTailParameters`` replace these clauses with one opaque option;
``Drop.setParameters`` also clears ALGORITHM and LOCK.

Migration: edits of the ``getIndexSpec()`` and ``getTailParameters()`` snapshots
now throw ``UnsupportedOperationException``; edit the nodes or call the setters.
ALTER TABLE key definitions report the same tokens as CREATE TABLE, for example
``KEY_BLOCK_SIZE``, ``=`` and ``8`` instead of the single entry
``KEY_BLOCK_SIZE = 8``. For a constructed statement without trailing clauses,
``getTailParameters()`` returns an empty list instead of null. Typed option
keywords are rendered in upper case.

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

Table constraints expose their names and ``ConstraintAttributes`` independently from indexes. ``KeyConstraint`` and ``ExcludeConstraint`` expose ``IndexOptions`` for null treatment, INCLUDE columns, storage parameters and tablespace options. Their ``KeyElement`` nodes expose key expressions, ordering and exclusion operators. Column identity clauses are represented by ``ColumnOption.Kind.IDENTITY`` and ``IdentityDefinition``, with a generation mode and ordered ``Sequence.Parameter`` values. These APIs cover the schema clauses described in `CREATE TABLE <https://www.postgresql.org/docs/18/sql-createtable.html>`_.

Inspect indexes and constraints separately
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``Index`` represents index declarations. Constraints are independent
``NamedConstraint`` nodes: ``KeyConstraint`` for PRIMARY KEY/UNIQUE,
``ForeignKeyConstraint``, ``CheckConstraint``, ``ExcludeConstraint``,
``DefaultConstraint``, ``NotNullConstraint`` and ``ConstraintUsingIndex``.
Constraint nodes do not extend ``Index``. Their ``ConstraintKind`` is separate
from the physical index classification ``Index.Kind``.

``CreateTable.getIndexes()`` returns only indexes, while
``getTableConstraints()`` returns table-level constraints. Both are mutable
views of ``getTableElements()`` when ordered elements are present; removing a
constraint preserves indexes and their source positions. To inspect column
and table constraints together, use ``getConstraints()``. This returns
``ConstraintDeclaration`` views in declaration order.

.. code-block:: java

    CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
        "CREATE TABLE t (id INT PRIMARY KEY, label TEXT)");
    ConstraintDeclaration primary = table.getConstraints().stream()
        .filter(declaration -> declaration.getKind() == ConstraintKind.PRIMARY_KEY)
        .findFirst().orElseThrow();
    primary.getColumn().setColumnName("new_id");
    // CREATE TABLE t (new_id INT PRIMARY KEY, label TEXT)

``getColumn()`` retains the owner of an inline declaration and is null for a
table constraint. ``getColumnOption()`` exposes an inline option;
``getConstraint()`` exposes its original ``NamedConstraint`` when one exists.
Column REFERENCES and nullability options retain their own payloads instead
of manufacturing a table constraint or a key-column list. Edit the original
source nodes to change SQL. Explicit NOT NULL and REFERENCES are classified;
ordinary column DEFAULT values are options, while explicit
``DefaultConstraint`` nodes are classified as DEFAULT. NULL, raw tokens and implied constraints are not
inferred.

The declaration list is an unmodifiable membership snapshot. Its views read
live source nodes, so type edits change ``getKind()``. Request a new list after
adding, removing or changing column options. Structured constraint nodes
remain included when their kind is OTHER. Without ordered table elements,
manually constructed columns precede table constraints and indexes.

``ColumnOption.getConstraintKind()`` and
``AlterExpression.getConstraintKind()`` also return ``ConstraintKind``.
``getOperation()`` distinguishes addition from removal. ALTER ADD constraints
use ``getConstraint()``; ADD INDEX uses ``getIndex()``. These getters expose
stored definitions; after changing an action, ``getConstraintKind()`` describes
the active syntax rather than an inactive stored definition. Column actions
retain constraints on their column options. RENAME CONSTRAINT uses
``getConstraintName()`` for the source and ``getNewConstraintName()`` for the
target; it does not manufacture index nodes. A named ``DROP CONSTRAINT pk`` reports
``OTHER`` because SQL alone does not reveal what ``pk`` names. This query does
not validate a definition's completeness or consult the database catalogue.

MySQL table-level ``UNIQUE``, ``UNIQUE KEY`` and ``UNIQUE INDEX`` declarations
are ``KeyConstraint`` nodes. A standalone ``CREATE UNIQUE INDEX`` remains
``CreateIndex`` with an ``Index`` payload. The parser does not invent backing
index nodes for PRIMARY KEY/UNIQUE constraints. PostgreSQL ``USING INDEX``
retains the existing index name on ``ConstraintUsingIndex`` rather than
creating key elements or an index definition.

An ALTER foreign key owns one ``ForeignKeyConstraint`` and its mutable
``ForeignKeyReference``. The legacy ``getFk*``/``setFk*`` accessors and
referential-action helpers delegate to these same nodes, so edits through
either API affect both SQL renderers.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE child ADD FOREIGN KEY (parent_id) REFERENCES parent (id)");
    AlterExpression action = alter.getAlterExpressions().get(0);
    ForeignKeyConstraint foreignKey = (ForeignKeyConstraint) action.getConstraint();
    foreignKey.getReference().getTable().setSchemaName("archive");
    action.setFkSourceTable("parent_history");
    // Both APIs now refer to archive.parent_history.
    foreignKey.setReferentialAction(ReferentialAction.Type.DELETE,
        ReferentialAction.Action.CASCADE);

New code should use the structured constraint API. ``getFkColumns()`` returns
a rendered snapshot, like ``getPkColumns()`` and ``getUkColumns()``; use
``setFkColumns()``/``addFkColumns()`` to replace or append names, or edit
``foreignKey.getColumns()`` directly. Referenced-column lists and
referential-action objects are live objects on the shared reference.

A non-null legacy FK setter creates a foreign-key definition when none is
present. It rejects an existing non-FK constraint or index with
``IllegalStateException`` instead of retaining a second hidden definition;
replace the definition explicitly with ``setConstraint()`` first. Reading,
clearing or removing absent FK properties does not create a definition.
Replacing or clearing ``setConstraint()`` also replaces or clears what the
legacy FK accessors expose. These accessors inspect stored definitions; use
``getOperation()``/``getConstraintKind()`` to identify the active action.

Migration from the former combined model:

* Replace table-constraint reads from ``getIndexes()`` with
  ``getTableConstraints()`` and ALTER constraint reads from ``getIndex()``
  with ``getConstraint()``. ``ConstraintDeclaration.getIndex()`` is replaced
  by ``getConstraint()``.
* Replace ``ForeignKeyIndex`` with ``ForeignKeyConstraint`` and concrete
  ``new NamedConstraint()`` construction with the appropriate constraint
  class, usually ``KeyConstraint``. ``NamedConstraint`` is now abstract.
* Constraint enum values move from ``Index.Kind`` to ``ConstraintKind``;
  ``Index.Kind`` retains only UNIQUE, INDEX, FULLTEXT, SPATIAL and OTHER.
* Shared key and option values are now ``KeyElement`` (formerly
  ``Index.ColumnParams``), ``IndexOption`` (formerly ``Index.Option``) and
  ``IndexOptions.Clustering`` (formerly ``Index.Clustering``). These are
  standalone shared components, not aliases to nested index types. Replace
  ``getColumnWithParams()``/``setColumnNamesWithParams()`` with
  ``getColumns()``/``setColumns()`` using ``KeyElement`` values.

``getType()`` preserves the rendered spelling. ``setType()`` refreshes kind
metadata for indexes and key constraints; ``setKind()`` does not rewrite SQL.
Constraints with a fixed renderer, such as CHECK, always report their actual
constraint kind. Replacing an index with a table constraint requires replacing
the AST node; changing an index's type string cannot change its Java type.

ALTER key definitions use ``getKeyConstraint()`` for typed access to the same
object returned by ``getConstraint()``. PRIMARY KEY and UNIQUE builder setters
also create this canonical definition; they no longer keep separate column,
name or KEY/INDEX state:

.. code-block:: java

    Alter add = (Alter) CCJSqlParserUtil.parse(
            "ALTER TABLE t ADD UNIQUE KEY uq (id)",
            parser -> parser.withDialect(Dialect.MYSQL));
    KeyConstraint key = add.getAlterExpressions().get(0).getKeyConstraint();
    key.setColumns(List.of(new KeyElement("account_id")));
    key.setIndexName("uq_account");
    System.out.println(add); // ALTER TABLE t ADD UNIQUE KEY uq_account (account_id)

Parenthesized deletion targets have their own ``getDropColumns()`` and
``setDropColumns()`` API. It covers multi-column ``DROP (...)``, ``DROP UNIQUE
(...)`` and the parser's legacy parenthesized ``DROP FOREIGN KEY (...)`` form.
Named constraint drops continue to use ``getConstraintName()``. No key
definition is manufactured for a drop:

.. code-block:: java

    Alter drop = (Alter) CCJSqlParserUtil.parse("ALTER TABLE t DROP (old_a, old_b)");
    drop.getAlterExpressions().get(0).getDropColumns().set(0, "obsolete_a");
    System.out.println(drop); // ALTER TABLE t DROP (obsolete_a, old_b)

``setDropColumns`` copies its input; ``getDropColumns`` returns the owned mutable
list. ``withDropColumns`` and ``addDropColumns`` also work before selecting the
operation. Stored drop targets and key definitions remain independent when an
operation changes.

Migration of older ALTER builders:

* ``getPkColumns()`` and ``getUkColumns()`` now consistently return snapshots,
  including manually built keys. Use their setters/adders or edit
  ``getKeyConstraint().getColumns()`` to change SQL. The string setters replace
  elements without retaining expression or ordering attributes.
* The legacy PK accessor remains a DROP-target alias when DROP, DROP UNIQUE or
  DROP FOREIGN KEY is already selected. Replace builders that set PK columns
  before selecting DROP with ``withDropColumns(...)``; changing the operation
  does not transfer a key definition into deletion targets.
* ``getUkName`` accesses the UNIQUE definition's index name; the constraint
  symbol remains ``getKeyConstraint().getName()``. KEY/INDEX flags read that same
  definition. ``setUkTypeSpecified(false)`` removes the spelling; re-enabling it
  without a spelling selects INDEX.
* Non-null key setters create a definition only when none exists. They throw
  ``IllegalStateException`` for a competing constraint or index; use
  ``setConstraint`` for an explicit replacement. Reads and null clears never
  create a definition or restore values from a replaced one.

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

Identity alterations are directly accessible through ``AlterExpression.getColumnIdentityActions()``;
the existing ``ColumnDataType.getIdentityAlterations()`` API remains available.
Sequence ownership is shared by ``CreateSequence`` and ``AlterSequence`` through ``Sequence.getOwnership()``: ``null`` means omitted, ``isNone()`` means explicit ``OWNED BY NONE``, and ``getColumn()`` identifies an owner. ``TablesNamesFinder`` includes ``LIKE`` sources and sequence owners without treating sequence or type names as tables. See `ALTER TABLE <https://www.postgresql.org/docs/18/sql-altertable.html>`_ and `ALTER SEQUENCE <https://www.postgresql.org/docs/18/sql-altersequence.html>`_.

For identity columns, ``RESTART 20`` and ``RESTART WITH 20`` produce the same
``IdentityAlteration`` with kind ``RESTART`` and ``getRestartWith() == 20L``.
The renderer consistently uses ``RESTART WITH 20``. A null restart value means
bare ``RESTART``, which uses the sequence's configured start value. Call
``setRestartWith`` to edit a parsed action or construct one with
``new IdentityAlteration(Kind.RESTART).withRestartWith(20L)``.


Edit column identity actions
----------------------------

``getColumnIdentityActions()`` returns active ``ALTER COLUMN`` identity actions
as ``ColumnIdentityAction`` entries. Each entry exposes the editable column name
and its ordered ``IdentityAlteration`` list, without requiring a data-type cast:

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
            "ALTER TABLE accounts ALTER COLUMN id "
            + "ADD GENERATED BY DEFAULT AS IDENTITY (CACHE 10)",
            parser -> parser.withDialect(Dialect.POSTGRESQL));
    ColumnIdentityAction column = alter.getAlterExpressions().get(0)
            .getColumnIdentityActions().get(0);
    column.setColumnName("account_id");
    IdentityAlteration identity = column.getIdentityAlterations().get(0);
    identity.setGenerationMode(IdentityDefinition.GenerationMode.ALWAYS);
    identity.getParameters().get(0).setValue(20L);
    System.out.println(alter);
    // ALTER TABLE accounts ALTER COLUMN account_id
    // ADD GENERATED ALWAYS AS IDENTITY (CACHE 20)

The outer list has unmodifiable membership; its entries are the original AST
nodes. Request a new list after changing action membership or switching the
outer ALTER operation. The inner alteration list is mutable: replace an entry
to change its kind, or use ``setIdentityAlterations`` to replace the ordered
actions for a column. Unrelated actions and inactive inherited payloads are
excluded. An inline identity declaration in ``ADD COLUMN`` remains a
``ColumnOption`` rather than an identity alteration.

``getGenerationMode``/``setGenerationMode`` work for both ``ADD`` and
``SET_GENERATED``. ``getParameters``/``setParameters`` work for ``ADD`` and
``SET_PARAMETER``. ADD access delegates to its existing ``IdentityDefinition``;
replacing or clearing that definition immediately changes both views. Parameters
are ordered mutable ``Sequence.Parameter`` values, with null meaning omitted.
RESTART and DROP retain their distinct ``getRestartWith`` and ``isIfExists``
properties. A sequence such as ``SET CACHE 10 RESTART WITH 20`` remains two
ordered alterations.

For a newly constructed ADD action without a definition, mode and parameter
getters return null. Non-null setters create the definition; setting parameters
first uses BY DEFAULT. Null setters do not create one. This does not change the
SQL grammar or validate whether a column already has an identity in a database.


Column defaults on tables and views
-----------------------------------

``AlterExpression.getColumnDefaults()`` exposes active ``SET DEFAULT`` actions
through ``ColumnDefaultAction`` for both tables and views. The returned list is
read-only, but its entries are the original mutable AST objects:

.. code-block:: java

    for (String objectType : List.of("TABLE", "VIEW")) {
        Statement statement = CCJSqlParserUtil.parse(
                "ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT 1",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        List<? extends AlterExpression> actions = statement instanceof Alter
                ? ((Alter) statement).getAlterExpressions()
                : ((AlterRelation) statement).getActions();
        for (AlterExpression action : actions) {
            for (ColumnDefaultAction column : action.getColumnDefaults()) {
                column.setDefaultExpression(new LongValue(42));
            }
        }
        System.out.println(statement); // SET DEFAULT 42 for either object type
    }

``getColumnName()`` and ``setColumnName()`` access the affected column.
``getDefaultExpression()`` exposes parsed expressions; ``getDefaultValue()``
returns their SQL text. Existing ``ColumnSetDefault`` constructors still accept
opaque strings, for which ``getDefaultExpression()`` returns ``null``.
Replacing an expression also replaces any opaque value. Unrelated actions
return an empty list. This lookup follows the action's current rendering path:
changing an operation or retaining unused metadata does not make a fixed-body
action expose ``SET DEFAULT`` entries it will not render.

``hasActiveColumnDefinitions()`` reports whether the action currently renders
its column definitions through the general ALTER body. ``getColDataTypeList()``
continues to expose stored metadata even when another body takes precedence.
``AlterDeParser`` uses the active path, so expression visitors rewrite the
selected default or column expressions without rendering inactive metadata.
``TableDefinitionTraversal`` and the table-name finder visit the expressions
and tables belonging to the selected body. The metadata validator uses that
same selection. For example, adding a column definition to an action that still
stores an earlier foreign key renders the column and does not require the
inactive foreign table or its columns to exist. ``getConstraint()`` and
``getIndex()`` still expose the stored objects for direct inspection.

``getActiveTableDefinition()`` returns the original definition contributing to
the current body, or ``null`` when another payload takes precedence. A legacy
``ADD CONSTRAINT`` header contributes only its keys; its unused storage options
are not traversed. Column options likewise visit only the payload selected by
their ``Kind``, including after changing an option to ``FOREIGN_OPTIONS``.

Custom ``AlterExpression`` subclasses overriding ``appendBody`` should also
reflect their dispatch in ``usesColumnActionBody()``, ``usesGeneralBody()`` and
``hasActiveConstraintHeader()``. Subclasses rendering a separate definition can
override ``getActiveTableDefinition()``. The general-body hook also determines
``hasActiveColumnDefinitions()``.

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

MySQL column attributes have their own kinds: ``SRID`` (``getSrid()``),
``COLUMN_FORMAT`` (``getColumnFormat()``: FIXED, DYNAMIC or DEFAULT), and
``ENGINE_ATTRIBUTE``/``SECONDARY_ENGINE_ATTRIBUTE`` (``getEngineAttribute()``
and ``isUseEquals()`` for the optional equals sign). Engine attribute literals
are visited and rendered like comment literals. Inline ``UNIQUE KEY`` is one
``KeyConstraint`` of type ``UNIQUE KEY``. A bare inline ``KEY``, MySQL's column
synonym for PRIMARY KEY, keeps its ``KEY`` spelling and reports
``ConstraintKind.PRIMARY_KEY``. Other values, such as Percona Server's
``COLUMN_FORMAT COMPRESSED``, remain opaque options.

.. code-block:: java

    CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
            "CREATE TABLE t (id INT KEY, g POINT SRID 4326 COLUMN_FORMAT FIXED)");
    List<ColumnDefinition> columns = table.getColumnDefinitions();
    columns.get(0).getColumnOptions().get(0).getConstraintKind(); // PRIMARY_KEY
    ColumnOption srid = columns.get(1).getColumnOptions().get(0);
    srid.getSrid();                                                // 4326
    srid.setSrid(0);
    columns.get(1).addColumnOptions(
            ColumnOption.engineAttribute(new StringValue("{}")));
    // CREATE TABLE t (id INT KEY, g POINT SRID 0 COLUMN_FORMAT FIXED ENGINE_ATTRIBUTE '{}')

Migration: these attributes were previously one opaque option per token, and
``UNIQUE KEY`` was a UNIQUE constraint followed by an opaque ``KEY``. Inspect the
kinds instead of the opaque nodes. ``getColumnSpecs()`` and ``getTokens()`` keep
the previous token split, for example ``[SRID, 4326]`` and ``[UNIQUE, KEY]``.
When constructing the KEY spelling, set the kind after the type:
``new KeyConstraint().withType("KEY").withKind(ConstraintKind.PRIMARY_KEY)``.
``CHAR(n) BINARY`` remains an opaque option: MySQL treats ``BINARY`` as part of
the data type's character set clause, not as a column attribute.

``ColumnDefinition`` stores all options in one ordered list, including opaque
``ColumnOption.Kind.OTHER`` nodes. ``getColumnOptions()`` returns that mutable
list. ``getColumnSpecs()`` returns an **unmodifiable snapshot** for both raw and
structured options; expression fragments may occupy one token and structured
keywords use canonical capitalization. Unset options return null; an explicitly
empty list returns an empty snapshot.

.. code-block:: java

    CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
            "CREATE TABLE t (id INT DEFAULT 1)");
    ColumnDefinition column = table.getColumnDefinitions().get(0);
    column.getColumnOptions().get(0).setDefaultExpression(new LongValue(2));
    column.addColumnSpecs("NOT", "NULL"); // keeps the structured DEFAULT node
    // CREATE TABLE t (id INT DEFAULT 2 NOT NULL)

Migration: calls such as ``getColumnSpecs().add(...)`` or ``.set(...)`` now throw
``UnsupportedOperationException`` instead of sometimes changing the AST and
sometimes editing a discarded copy. Use ``addColumnSpecs`` to append raw tokens,
edit the option nodes directly, or pass a modified token-list copy to
``setColumnSpecs`` to replace every option with opaque text. Raw tokens are never
automatically parsed. Consequently ``getColumnOptions()`` is also non-null for
raw specifications; inspect ``Kind`` to distinguish them.

The constructor and ``setColumnSpecs`` copy supplied tokens. ``setColumnOptions``
copies list membership but retains the mutable child nodes; later changes to the
caller's list do not change the column. Both getters return null after either
setter receives null. Appending preserves the current nodes and accepts an
immutable input list. An empty append preserves existing options and creates an
empty option list when none was set.

``ColumnOption.getTokens()`` and ``GeneratedColumnDefinition.getTokens()`` also
return unmodifiable, detached snapshots. Reading tokens does not detach the
underlying expressions or other child nodes: their setters and direct edits
remain visible in SQL output, visitors and newly requested snapshots.

For opaque ``Kind.OTHER`` options, copy the tokens, edit the copy, then call
``setTokens``. The setter and both ``ColumnOption.raw`` factories copy their
inputs; subsequent changes to the supplied list or array do not edit the AST.
Null raw tokens remain unset and return null; an empty list returns an empty
snapshot. Raw tokens are never parsed or classified.

.. code-block:: java

    ColumnOption raw = ColumnOption.raw("SELECTIVITY", "10");
    List<String> tokens = new ArrayList<>(raw.getTokens());
    tokens.set(1, "20");
    raw.setTokens(tokens); // SELECTIVITY 20

Migration: raw token getter edits now throw ``UnsupportedOperationException``,
as do edits to structured token snapshots that previously changed a discarded
copy. ``setTokens`` only accepts opaque options; it throws
``IllegalStateException`` for structured options, including SERIAL DEFAULT
VALUE. Use typed setters for structured edits. To explicitly replace a
structured option with raw text, replace its node in the column's option list:

.. code-block:: java

    column.getColumnOptions().set(0, ColumnOption.raw("DEFAULT", "8"));

MySQL column positions
~~~~~~~~~~~~~~~~~~~~~~

The trailing ``FIRST`` or ``AFTER column`` of an ALTER TABLE ADD, CHANGE or
MODIFY column definition is a ``ColumnPosition`` on the
``AlterExpression.ColumnDataType``, not a column option. ``getColumnSpecs()``
still ends with its tokens, and ``setColumnSpecs`` replaces the options and the
position. Migration: ``getColumnOptions()`` no longer contains the two opaque
``AFTER`` and column-name nodes. As in MySQL, the position must follow the
column attributes; a position followed by further attributes is now rejected.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
            "ALTER TABLE t ADD COLUMN c INT NOT NULL AFTER id");
    AlterExpression.ColumnDataType column =
            alter.getAlterExpressions().get(0).getColDataTypeList().get(0);
    column.getPosition().getColumnName();                         // id
    column.getColumnSpecs();                                      // [NOT, NULL, AFTER, id]
    column.setPosition(ColumnPosition.first());
    // ALTER TABLE t ADD COLUMN c INT NOT NULL FIRST

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

When the analysis identifies an unresolved function, procedure or dynamic SQL marker, it records the reference so you can consult your own catalogue or allow-list:

.. code-block:: java
    :caption: Safeguarding a read-only connection

    StatementFeatures features = CCJSqlParserUtil.parse(sqlStr).getFeatures();

    if (connection.isReadOnly() && features.mayModifyData()) {
        throw new SQLException(
                "rejected, unresolved: " + features.getUnresolvedReferences());
        // e.g. [nextval]
    }

The generic fallback for an unclassified statement preserves ``OPAQUE`` and the possible effects ``READS_DATA``, ``RETURNS_RESULT_SET``, ``MODIFIES_DATA`` and ``MODIFIES_SCHEMA``, but does not add a Java statement or container class name to ``getUnresolvedReferences()``. The reference set can therefore be empty while the effects remain unknown. Check ``isOpaque()`` and ``may(..)``; an empty reference set alone does not establish safety.

Existing explicit references and markers remain, including procedure and function names, ``do``, ``unsupported`` and ``explain``. Stored routine declarations also retain their existing statement-type markers, such as ``createfunction``; these are independent of the generic fallback.

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

Customize JSON expression output
--------------------------------

``ExpressionDeParser`` visits structured children of ``JsonFunction`` and
``JsonTableFunction`` while rendering them. A custom expression printer can
rewrite inputs, paths, PASSING values and DEFAULT expressions, including nested
JSON_TABLE columns, without changing the AST. JSON object keys and values are
visited when they are ``Expression`` objects; opaque objects and additional
query-path strings retain their existing rendering.

.. code-block:: java

    Statement statement = CCJSqlParserUtil.parse(
            "SELECT JSON_VALUE((SELECT doc FROM source_table), '$.a')");
    ExpressionDeParser expressions = new ExpressionDeParser() {
        @Override
        public <S> StringBuilder visit(Column column, S context) {
            return getBuilder().append("payload");
        }
    };
    statement.accept(new StatementDeParser(expressions, new SelectDeParser(),
            expressions.getBuilder()), null);
    // SELECT JSON_VALUE((SELECT payload FROM source_table), '$.a')

``StatementDeParser`` connects the select and expression printers, so subqueries
inside JSON expressions use the same printer. For expression-only use with
subqueries, connect an appropriate ``SelectDeParser`` explicitly.

JSON nodes also expose ``appendTo(StringBuilder, Consumer<Expression>)`` for
direct rendering. The consumer prints each child into the same builder;
``ExpressionDeParser`` forwards the visitor context through that callback.
``JsonFunction`` additionally has consumer overloads of ``appendObject``,
``appendArray``, ``appendValue``, ``appendQuery`` and ``appendExists``.

Subclass migration: override these consumer overloads or ``appendTo`` to
customize both default and visitor output. The deparser enters through ``appendTo``;
overriding only a legacy single-argument ``append*`` or ``toString()`` method does
not customize that path. ``JsonFunction`` also uses the consumer overloads for
type-specific default rendering. Subclasses of concrete JSON_TABLE column
definitions should move ``toString()`` customization
to ``appendTo``. Direct subclasses of the abstract ``JsonTableColumnDefinition``
retain their opaque ``toString()`` fallback until they override ``appendTo``.


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
- ``parser.withUnsupportedStatements(true)`` captures an unrecognised statement root as
  ``UnsupportedStatement``; it may be the first statement. Failures in recognised statement
  grammar still throw, or use ``withErrorRecovery(true)`` and record the error. Thus
  ``SELECT * FROM``, ``INSERT INTO t (a`` and ``UPDATE t SET`` do not become opaque statements.
  The two options serve different purposes; enabling unsupported capture does not suppress
  recognised syntax errors.

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
            "select * from mytable; shutdown defrag; select * from mytable2; select 4;"
            , parser -> parser.withUnsupportedStatements() );

    // 4 statements with one Unsupported Statement holding the content
    assertEquals(4, statements.size());
    assertInstanceOf(UnsupportedStatement.class, statements.get(1));
    assertEquals("shutdown defrag", statements.get(1).toString());

    // no errors records, because a statement has been returned
    assertEquals(0, parser.getParseErrors().size());

.. note::

    Opaque capture is not SQL validation. An unrecognised root or an existing opaque
    grammar branch cannot establish validity in an unknown dialect. Callers that need
    validity must validate against that dialect. Recognised syntax failures formerly
    captured by the generic fallback now throw or produce an error-recovery placeholder;
    partial IF results and typed prefixes with invalid trailing clauses are not published
    as successful statements. ``SingleStatement()`` remains an incremental entry point;
    use ``Statement()`` or ``Statements()`` to check complete statement boundaries.

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

MySQL comments
~~~~~~~~~~~~~~

With ``Dialect.MYSQL``, ``--`` starts a line comment only when followed by
whitespace or a control character. Thus ``SELECT 1--2`` contains subtraction
and a negative operand. SQL inside an unversioned executable comment is parsed
normally: ``SELECT 1 /*! + 2 */`` becomes ``SELECT 1 + 2``. Optimizer hints
(``/*+ ... */``) remain separate from executable comments.

Conditional executable comments require an explicit target server version:

.. code-block:: java

    Statement stmt = CCJSqlParserUtil.parse(
            "SELECT 1 /*!90702 + 2 */",
            parser -> parser.withDialect(Dialect.MYSQL).withMySqlServerVersion(90702));

The value is ``major * 10000 + minor * 100 + patch``; for example, ``90702``
means 9.7.2 and ``260700`` means 26.7.0. Five-digit and six-digit comment version
prefixes follow MySQL's whitespace rules. SQL for a newer server is ignored;
SQL for the configured version or an older one participates in the AST.
Both SQL renderers produce the resolved SQL for that target, without the
conditional wrappers. Without a target, a conditional executable comment
raises an error instead of silently discarding its SQL. This replaces the
previous treatment of all executable comments as ordinary discarded comments.
The same configuration applies to parsers constructed with a ``StreamProvider``
or ``FeatureConfiguration``. Other dialects retain their comment behavior.
Nested executable comments are rejected; nested-comment acceptance in other
dialects is not a guarantee that the same SQL is valid on MySQL.

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

String literal values
~~~~~~~~~~~~~~~~~~~~~

``StringValue.getValue()`` returns the literal body as written, and ``toString()``
renders it unchanged. ``getNotExcapedValue()`` returns the characters it denotes.
A doubled quote of the literal's own quote character is resolved, so
``"it""s"`` yields ``it"s``. ``getEscaping()`` records how the parser read
backslashes:

* ``MYSQL_BACKSLASH`` without a dialect or with ``Dialect.MYSQL``/``MARIADB`` when
  backslash escapes are enabled: ``'it\'s'`` yields ``it's``, ``\n``, ``\t``,
  ``\0`` and ``\Z`` become their characters, and ``\%`` and ``\_`` keep their backslash.
* ``POSTGRESQL_ESCAPE_STRING`` for ``E'...'`` with ``Dialect.POSTGRESQL``, including
  ``\x``, octal and ``\u`` escapes; octal and hexadecimal escapes are UTF-8 bytes.
* ``STANDARD`` otherwise, including dollar-quoted strings and constructed values;
  backslashes stay literal. Other dialects with backslash escapes keep this reading.

``setEscaping()`` selects the reading for a constructed value. Escaping is not
part of ``equals()``, like the quote character.

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
parameters in ``getConstraint()``. ``isUsingHash()`` preserves ``USING HASH``, while
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
``getNullOrdering()`` on ``KeyElement``. Under this dialect these
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
      - ``".."`` reads as a string literal instead of a quoted identifier (BigQuery, Spark/Databricks, MySQL default ``sql_mode``); combined with ``withBackslashEscapeCharacter``, ``\\"`` does not end the literal, as in MySQL ``"it\\"s"``
    * - ``withHashLineComments``
      - ``#`` starts a line comment
    * - ``withBlankLinesAsSeparator``
      - ``false`` stops two empty lines (``\n\n\n``) ending a statement, so they read as plain whitespace
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

MySQL account names (``user[@host]``) of ``CREATE USER`` and of view, trigger and
routine ``DEFINER`` clauses share one grammar rule and are read into separate user
and host ``StringValue`` parts (``UserAccount`` and ``MySqlDefiner``). Every MySQL
spelling is accepted, including ``app@localhost``, ``app@'%'`` and
``'app'@localhost``. A part quoted with ``'``, ``"`` or a backquote keeps its quote
character in ``getQuoteStr()`` and its text in ``getValue()``::

    CreateUser createUser = (CreateUser) CCJSqlParserUtil.parse(
        "CREATE USER `app`@'%' IDENTIFIED BY 'secret'");
    UserAccount account = createUser.getAccounts().get(0);
    account.getUser().getValue();    // app
    account.getUser().getQuoteStr(); // `
    account.getHost().getValue();    // %

``Grant`` retains its string-based getters, setters and fluent methods. Its
``PrivilegeClause`` exposes typed ``Privilege`` items, column lists,
``PrivilegeTarget`` kinds, multiple role memberships and grantor information.
``getPrivileges()`` is a mutable string view of the typed privilege list.
``getRole()`` and ``getObjectName()`` expose the first role or named target for
legacy callers; use ``getRoleItems()`` and ``getTarget()`` for the complete lists.
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

MySQL privileges and accounts in GRANT and REVOKE
-------------------------------------------------

``Grant`` and ``Revoke`` also parse the MySQL forms, in every dialect:

- **Privilege levels**: ``PrivilegeTarget.getWildcard()`` is ``DEFAULT_DATABASE`` for ``*``,
  ``GLOBAL`` for ``*.*`` and ``DATABASE`` for ``db.*``, whose database is the single entry
  of ``getNames()``. Named levels such as ``db.tbl`` keep a null wildcard. Only named
  table targets are reported by table-name discovery.
- **Privileges**: MySQL static privileges are ``Privilege.Kind`` constants, for example
  ``CREATE_TEMPORARY_TABLES`` or ``REPLICATION_SLAVE``; ``getName()`` returns the SQL
  spelling. Another identifier, such as ``BACKUP_ADMIN``, is a dynamic privilege of kind
  ``DYNAMIC`` with ``getDynamicName()``.
- **Grantees**: ``PrivilegeClause.getGranteeItems()`` returns ``Grantee`` values, which
  MySQL and PostgreSQL share. ``ACCOUNT`` holds a ``UserAccount``: a MySQL ``user[@host]``,
  read like the ``CREATE USER`` account names above, or a PostgreSQL role name without
  host. The other kinds are
  PostgreSQL ``GROUP name``, ``PUBLIC``, ``CURRENT_USER`` (MySQL also ``CURRENT_USER()``),
  ``CURRENT_ROLE`` and ``SESSION_USER``. ``getGrantor()`` types ``GRANTED BY`` the same way.
- **Role memberships**: ``getRoleItems()`` holds the granted roles as ``UserAccount``
  values, so ``'reader'`` and ``'writer'@'%'`` keep their quoting and host. A membership
  has no privileges and no target.
- **MySQL clauses**: ``PROXY ON user`` is a ``PROXY`` privilege on an ``ACCOUNT`` target
  (``PrivilegeTarget.getAccount()``). ``REVOKE ALL [PRIVILEGES], GRANT OPTION FROM ...``
  has the privileges ``ALL`` and ``GRANT_OPTION`` and no target. ``Revoke`` records
  ``IF EXISTS`` and ``IGNORE UNKNOWN USER``; ``Grant.getAsUser()`` and ``getWithRole()``
  hold ``AS user [WITH ROLE ...]``; the role selection is a ``RoleSelection`` from
  ``net.sf.jsqlparser.statement.create.user``.

.. code-block:: java

    Grant grant = (Grant) CCJSqlParserUtil.parse(
        "GRANT SELECT, BACKUP_ADMIN ON db1.* TO 'app'@'%', CURRENT_USER");
    grant.getTarget().getWildcard();                    // DATABASE
    grant.getPrivilegeItems().get(1).getDynamicName();  // BACKUP_ADMIN
    UserAccount app = grant.getClause().getGranteeItems().get(0).getAccount();
    app.getHost().getValue();                           // %

``Grant.getOptions()`` lists every ``WITH`` option, including PostgreSQL membership
options such as ``WITH INHERIT TRUE, SET FALSE``. Privileges take only ``GRANT OPTION``;
memberships take distinct ``ADMIN``, ``INHERIT`` and ``SET`` options.

The string accessors are kept and derived from the typed state: ``getUsers()``,
``getGrantees()`` and ``getRoles()`` are mutable string views that write through,
``getOption()`` is the first option and ``getGrantedBy()`` renders the grantor.
``getObjectName()`` returns a wildcard level as written, for example ``db1.*``, and
``setObjectName()`` replaces it with a named target. Assigned text is converted with
``Grantee.valueOf()``: special role names are recognized and other text is kept as the
account name. Parsed memberships have no target; ``new Grant()`` still starts with an
empty table target, and ``setRole()`` clears it.

The MySQL and MariaDB validation capabilities now include ``revoke``.

References: `GRANT <https://dev.mysql.com/doc/refman/8.4/en/grant.html>`_,
`REVOKE <https://dev.mysql.com/doc/refman/8.4/en/revoke.html>`_,
`Specifying Account Names <https://dev.mysql.com/doc/refman/8.4/en/account-names.html>`_.

Migrating shared ALTER enums
----------------------------

``AlterServer.Action`` and ``AlterForeignDataWrapper.Action`` have been replaced
by ``ForeignObjectAlterAction`` in ``net.sf.jsqlparser.statement.alter``. Both
statements' ``getAction()`` and ``setAction()`` now use this enum, with the same
``OPTIONS``, ``OWNER`` and ``RENAME`` constants. Update imports and recompile code
using these APIs; the removed nested enums are not compatibility aliases.

.. code-block:: java

    AlterServer server = (AlterServer) CCJSqlParserUtil.parse(
        "ALTER SERVER s VERSION NULL");
    server.setAction(ForeignObjectAlterAction.OWNER);
    server.setOwner("CURRENT_USER");
    // ALTER SERVER s OWNER TO CURRENT_USER

    AlterForeignDataWrapper wrapper = (AlterForeignDataWrapper) CCJSqlParserUtil.parse(
        "ALTER FOREIGN DATA WRAPPER w NO HANDLER");
    wrapper.setAction(ForeignObjectAlterAction.OWNER);
    wrapper.setOwner("CURRENT_USER");
    // ALTER FOREIGN DATA WRAPPER w OWNER TO CURRENT_USER

``OPTIONS`` remains the initial action. It includes server ``VERSION`` changes
and wrapper handler/validator changes as well as foreign-data options. These
payloads remain on their respective statement classes. Switching to ``OWNER`` or
``RENAME`` leaves the inactive payload available for switching back to ``OPTIONS``.

``RelationAlterAction.TriggerState``, ``getTriggerState()`` and
``setTriggerState()`` have been removed. Use the existing
``RelationAlterAction.EnableState``, ``getEnableState()`` and ``setEnableState()``
for both trigger and rewrite-rule actions:

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE t ENABLE REPLICA TRIGGER trg",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    RelationAlterAction trigger = (RelationAlterAction) alter.getAlterExpressions().get(0);
    trigger.setEnableState(RelationAlterAction.EnableState.DISABLE);
    // ALTER TABLE t DISABLE TRIGGER trg

The action's ``Kind`` still distinguishes ``TRIGGER_STATE`` from ``RULE_STATE``.
Trigger targets also retain their ``NAME``, ``ALL`` and ``USER`` distinctions.

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
        alter.getAlterExpressions().get(0).getConstraint();
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

Database options and configuration parameters
---------------------------------------------

``CreateDatabase``, MySQL's ``CREATE SCHEMA`` and ``AlterDatabase`` share the
``DatabaseOption`` model. MySQL options are ``CHARACTER_SET`` (``getName()`` keeps
the ``CHARACTER SET``, ``CHAR SET`` or ``CHARSET`` spelling), ``COLLATE``,
``ENCRYPTION`` and, for ALTER only, ``READ_ONLY``. PostgreSQL options have one kind
each, from ``OWNER`` and ``TEMPLATE`` to ``CONNECTION_LIMIT``, ``IS_TEMPLATE`` and
``OID``; ``isUseWith()`` records the optional ``WITH``. Values keep their SQL text,
such as ``'UTF8'`` or ``-1``. Other option text remains one raw ``OTHER`` option.

``AlterDatabase.getAction()`` selects what is rendered: ``OPTIONS`` (the default),
``RENAME``, ``OWNER``, ``SET_TABLESPACE``, ``REFRESH_COLLATION_VERSION`` or
``CONFIGURATION_PARAMETER``. Payloads of the other actions are kept. ``SET`` and
``RESET`` of a configuration parameter use ``ConfigurationParameterChange``, which
``AlterRole`` shares:

.. code-block:: java

    AlterDatabase alter = (AlterDatabase) CCJSqlParserUtil.parse(
        "ALTER DATABASE app SET search_path TO app, public",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    ConfigurationParameterChange change = alter.getParameterChange();
    change.getParameter();  // search_path
    change.getValues();     // [app, public], a mutable ExpressionList
    change.setValueKind(ConfigurationParameterChange.ValueKind.DEFAULT);
    // ALTER DATABASE app SET search_path TO DEFAULT

``getValueKind()`` is ``VALUES``, ``DEFAULT`` or ``FROM_CURRENT``; ``RESET ALL`` uses
the parameter name ``ALL``. ``AlterRole`` keeps ``getParameter()``, ``isUseEquals()``,
``isFromCurrent()``, ``isUseDefault()``, ``getValues()`` and their setters; they read
and edit ``getParameterChange()``. ``FROM CURRENT`` and ``DEFAULT`` are now mutually
exclusive: ``setFromCurrent(false)`` and ``setUseDefault(false)`` return to the values.

Without the MySQL dialect, ``ALTER DATABASE`` forms that are neither MySQL nor
PostgreSQL syntax, such as ``ALTER DATABASE d SET RECOVERY SIMPLE``, remain an
``UnsupportedStatement``; ``Dialect.MYSQL`` reports them as errors. ``ALTER SCHEMA``
followed by MySQL options is an ``AlterDatabase`` in every dialect, while PostgreSQL's
``ALTER SCHEMA ... RENAME TO/OWNER TO`` remains an ``AlterSchema``. Validation
reports the ``createDatabase`` and ``alterDatabase`` features, which the MySQL,
MariaDB and PostgreSQL capabilities enable.

``CreateDatabase.getOptions()`` is now a live list that is never null, and
``setOptions`` copies its argument. ``getDatabaseOptions()`` is an unmodifiable
token snapshot derived from the options, including ``WITH``, and is null without
options. ``setDatabaseOptions`` replaces all options with one raw option, and
``addDatabaseOptions`` appends one while keeping the typed options. Parsed
statements no longer end with an empty raw option.

References: MySQL `CREATE DATABASE <https://dev.mysql.com/doc/refman/8.4/en/create-database.html>`_,
`ALTER DATABASE <https://dev.mysql.com/doc/refman/8.4/en/alter-database.html>`_;
PostgreSQL `CREATE DATABASE <https://www.postgresql.org/docs/18/sql-createdatabase.html>`_,
`ALTER DATABASE <https://www.postgresql.org/docs/18/sql-alterdatabase.html>`_.

Routine and trigger alterations
-------------------------------

PostgreSQL ``ALTER FUNCTION``, ``ALTER PROCEDURE`` and ``ALTER ROUTINE`` and MySQL
``ALTER FUNCTION`` and ``ALTER PROCEDURE`` parse to ``AlterRoutine``. ``getRoutineType()``
names the keyword and ``getRoutine()`` is a ``RoutineReference`` with the optional
PostgreSQL argument types. ``getAction()`` is ``CHARACTERISTICS``, ``RENAME``, ``OWNER``,
``SET_SCHEMA`` or ``DEPENDS_ON_EXTENSION`` (``isNoDependency()`` for ``NO DEPENDS``);
payloads of the other actions are kept. ``getCharacteristics()`` is a mutable list of
``RoutineCharacteristic`` in source order, shared by both dialects. Each ``Kind`` renders
its own keywords, such as ``IMMUTABLE``, ``CALLED_ON_NULL_INPUT``, ``PARALLEL_SAFE``,
``COST`` or MySQL's ``READS_SQL_DATA``. ``SECURITY_DEFINER`` and ``SECURITY_INVOKER`` keep
the ``[EXTERNAL] SECURITY`` or ``SQL SECURITY`` spelling in ``getSecurityClause()``.
``COST``, ``ROWS``, ``SUPPORT`` and ``COMMENT`` keep their value as SQL text in ``getValue()``.
``getComment()`` returns the ``COMMENT`` literal as a ``StringValue`` read with the dialect's
escaping, so ``getNotExcapedValue()`` is the text MySQL stores. ``SET`` and ``RESET`` reuse
``ConfigurationParameterChange``:

.. code-block:: java

    AlterRoutine alter = (AlterRoutine) CCJSqlParserUtil.parse(
        "ALTER FUNCTION f(int) IMMUTABLE SET search_path = public",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    alter.getRoutine().getArguments();                // [int]
    alter.getCharacteristics().get(0).getKind();      // IMMUTABLE
    alter.getCharacteristics().get(1).getParameterChange().getParameter(); // search_path

The characteristics of one statement belong to one dialect, and MySQL's take no argument
list. Other forms after ``ALTER FUNCTION`` or ``ALTER PROCEDURE``, such as Oracle's
``COMPILE`` or SQL Server routine definitions without the SQL Server dialect, remain an
``UnsupportedStatement``. PostgreSQL's ``ALTER TRIGGER name ON table RENAME TO`` and
``[NO] DEPENDS ON EXTENSION`` parse to ``AlterTrigger``; table visitors report its table.
Validators report ``alterFunction``, ``alterProcedure``, ``alterRoutine`` and
``alterTrigger``; PostgreSQL enables all of them, MySQL and MariaDB the first two.

``CreateFunctionalStatement.getDefiner()`` returns the MySQL ``DEFINER`` of
``CREATE DEFINER = ... FUNCTION|PROCEDURE`` as the ``MySqlDefiner`` that views and
triggers use. With ``Dialect.MYSQL`` or ``Dialect.MARIADB``,
``CreateFunctionalStatement.getCharacteristics()`` lists the characteristics between the
routine header and its body, such as ``COMMENT``, ``DETERMINISTIC``, ``READS SQL DATA`` or
``SQL SECURITY INVOKER``, with the same ``RoutineCharacteristic`` kinds; ``DETERMINISTIC``
and ``NOT_DETERMINISTIC`` occur only here. ``getFunctionDeclarationParts()`` still holds
every token and renders the statement unchanged, so the list is read-only. Replacing the
tokens with ``setFunctionDeclarationParts(parts)`` clears it, while
``setFunctionDeclarationParts(parts, characteristics)`` sets both, and appending tokens
keeps it. Without a MySQL dialect the list stays empty. MySQL ``CREATE TRIGGER`` accepts ``IF NOT EXISTS`` (``isIfNotExists()``)
and keeps MariaDB's ``OR REPLACE``. A ``BEGIN ... END`` trigger body with only SQL
statements remains a ``Block``; a compound body with procedural statements such as
``IF``, ``DECLARE`` or loops is an ``UnsupportedStatement`` with its tokens, captured with
the boundary of routine bodies. In both cases the semicolon after ``END`` separates the
next statement.

References: PostgreSQL `ALTER FUNCTION <https://www.postgresql.org/docs/18/sql-alterfunction.html>`_,
`ALTER PROCEDURE <https://www.postgresql.org/docs/18/sql-alterprocedure.html>`_,
`ALTER ROUTINE <https://www.postgresql.org/docs/18/sql-alterroutine.html>`_,
`ALTER TRIGGER <https://www.postgresql.org/docs/18/sql-altertrigger.html>`_;
MySQL `ALTER FUNCTION <https://dev.mysql.com/doc/refman/8.4/en/alter-function.html>`_,
`ALTER PROCEDURE <https://dev.mysql.com/doc/refman/8.4/en/alter-procedure.html>`_,
`CREATE TRIGGER <https://dev.mysql.com/doc/refman/8.4/en/create-trigger.html>`_.

MySQL table maintenance statements
----------------------------------

``CHECK TABLE``, ``OPTIMIZE TABLE`` and ``REPAIR TABLE`` parse to
``TableMaintenanceStatement`` in every dialect; ``ANALYZE TABLE`` remains ``Analyze``.
``getOperation()`` identifies the statement, ``getModifier()`` returns
``NO_WRITE_TO_BINLOG`` or ``LOCAL`` for OPTIMIZE and REPAIR, and ``isTablesKeyword()``
records the ``TABLES`` spelling. ``getOptions()`` keeps the trailing options in source
order. CHECK accepts ``FOR UPGRADE``, ``QUICK``, ``FAST``, ``MEDIUM``, ``EXTENDED`` and
``CHANGED``; REPAIR accepts ``QUICK``, ``EXTENDED`` and ``USE_FRM``. Other combinations
are rejected, and the option words remain ordinary identifiers elsewhere.

.. code-block:: java

    TableMaintenanceStatement repair = (TableMaintenanceStatement) CCJSqlParserUtil.parse(
        "REPAIR NO_WRITE_TO_BINLOG TABLE t1, t2 QUICK");
    repair.getTables();   // [t1, t2]
    repair.getOptions();  // [QUICK]

Table visitors and metadata validation use the table list. Validators report the
``tableMaintenance`` feature, which the MySQL and MariaDB capabilities enable. Feature
analysis reports ``READS_DATA`` and ``RETURNS_RESULT_SET``; OPTIMIZE and REPAIR also
report ``MODIFIES_SCHEMA``, and REPAIR may report ``MODIFIES_DATA``.

MySQL account management statements
-----------------------------------

``CREATE USER`` and ``ALTER USER`` share ``UserOptions`` (``getOptions()``) in MySQL's
clause order: the ``DEFAULT ROLE`` roles, the ``REQUIRE`` options (``TlsOption``), the
``WITH`` resource limits (``ResourceLimit``), the password and locking options
(``PasswordLockOption``) in source order, and ``COMMENT`` or ``ATTRIBUTE``. Omitted
clauses stay omitted. ``UserAuthentication`` adds ``ALTER USER``'s ``REPLACE 'current'``
(``getCurrentCredential()``), ``RETAIN CURRENT PASSWORD`` and the
``DISCARD_OLD_PASSWORD`` mode.

.. code-block:: java

    AlterUser alter = (AlterUser) CCJSqlParserUtil.parse(
        "ALTER USER 'app'@'%' IDENTIFIED BY 'new' RETAIN CURRENT PASSWORD "
            + "PASSWORD EXPIRE INTERVAL 90 DAY ACCOUNT LOCK");
    alter.getAccounts().get(0).getAuthentication().isRetainCurrentPassword(); // true
    alter.getOptions().getPasswordLockOptions(); // [PASSWORD EXPIRE INTERVAL 90 DAY, ACCOUNT LOCK]

``ALTER USER USER() ...`` keeps ``USER()`` as the unquoted account name, as
``DEFINER = CURRENT_USER`` does; ``isUserFunction()`` identifies it.
``ALTER USER acct DEFAULT ROLE {NONE | ALL | role, ...}`` stores a ``RoleSelection`` in
``getOptions().getDefaultRoles()``. PostgreSQL's ``ALTER USER``, an alias of ``ALTER ROLE``
(``WITH LOGIN``, ``PASSWORD 'x'``, ``SET``, ``RENAME TO`` ...), still parses to
``AlterRole``. MySQL syntax is recognized by ``IF EXISTS``, ``USER()``, quoted names,
hosts, account lists, a missing action or a MySQL account clause.

``RENAME USER`` parses to ``RenameUser`` with ordered ``from``/``to`` pairs. MySQL
``CREATE ROLE [IF NOT EXISTS] role, ...`` with quoted names, hosts or several roles keeps
them in ``CreateRole.getRoles()``, and ``getName()`` is null. A single unquoted name, which
both databases accept, keeps the PostgreSQL ``getName()``; so does an unquoted ``r@h``, which
the lexer reads as one identifier, unless the MySQL or MariaDB dialect is selected. Role names
use the shared account spelling and are ``UserAccount`` values without authentication.

``SET DEFAULT ROLE`` (MySQL's ``TO``, MariaDB's ``FOR``) and ``SET PASSWORD FOR`` parse to
``SetDefaultRoleStatement`` and ``SetPasswordStatement`` in every dialect. ``SET ROLE`` and
``SET PASSWORD`` without ``FOR`` share the variable assignment shape that PostgreSQL uses
for ``SET ROLE name`` and ``SET ROLE NONE``, so they remain a ``SetStatement`` unless
``Dialect.MYSQL`` or ``Dialect.MARIADB`` is selected. With these dialects every form parses
to ``SetRoleStatement`` (``DEFAULT``, ``NONE``, ``ALL [EXCEPT ...]`` or a role list) or
``SetPasswordStatement`` (``= password`` or ``TO RANDOM``, then ``REPLACE`` and
``RETAIN CURRENT PASSWORD``). The MySQL-only ``SET ROLE ALL EXCEPT ...`` and role names
with a quoted user or host, such as ``'r'@'%'``, parse to ``SetRoleStatement`` in every
dialect.

.. code-block:: java

    SetRoleStatement setRole = (SetRoleStatement) CCJSqlParserUtil.parse(
        "SET ROLE ALL EXCEPT 'admin'@'%'",
        parser -> parser.withDialect(Dialect.MYSQL));
    setRole.getRoles().getKind();  // ALL_EXCEPT
    setRole.getRoles().getRoles(); // ['admin'@'%']

Validators report ``alterUser``, ``renameUser``, ``setRole``, ``setDefaultRole`` and
``setPassword``; the MySQL and MariaDB capabilities enable them and ``createRole``.
Feature analysis reports ``MODIFIES_SCHEMA``, or ``MODIFIES_SESSION`` for ``SET ROLE``.
Multi-factor authentication clauses are not modeled. Serialized account SQL can contain
passwords; avoid logging real credentials.

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

``COMMENT ON`` supports these PostgreSQL object kinds in addition to the existing
``TABLE``, ``COLUMN`` and ``VIEW`` forms:

- ``INDEX``, ``SCHEMA``, ``SEQUENCE``, ``DOMAIN``, ``TYPE``, ``MATERIALIZED VIEW``,
  ``FOREIGN TABLE``, ``COLLATION`` and ``STATISTICS``
- ``ACCESS METHOD``, ``DATABASE``, ``EVENT TRIGGER``, ``EXTENSION``,
  ``FOREIGN DATA WRAPPER``, ``[PROCEDURAL] LANGUAGE``, ``PUBLICATION``, ``ROLE``,
  ``SERVER``, ``SUBSCRIPTION`` and ``TABLESPACE``
- ``FUNCTION``, ``PROCEDURE``, ``ROUTINE`` and ``AGGREGATE`` signatures
- ``CONSTRAINT``, ``TRIGGER``, ``POLICY`` and ``RULE`` with their owning table
- ``CAST (source_type AS target_type)``

These unambiguous target forms also parse without a dialect preset. Kind words such as
``ROLE``, ``SERVER`` or ``RULE`` are not reserved and remain ordinary identifiers.
Tagged dollar strings still require ``Dialect.POSTGRESQL`` or
``withDollarQuotedStringTags(true)``.

The additional targets are exposed through ``Comment.getTarget()`` as a
``CommentTarget``. Its ``Kind`` identifies the object, and ``getName()`` preserves
the individual identifier components. ``Table`` is used as the name container;
an index or type name does not thereby represent a table dependency.
``isProcedural()`` records ``PROCEDURAL LANGUAGE``.
The original ``Comment.getTable()``, ``getColumn()`` and ``getView()`` accessors
continue to describe their respective existing forms.

``COMMENT ON FUNCTION app.f(IN value integer) IS 'description'`` uses the shared
``RoutineReference`` in ``getTarget().getRoutine()``; ``Kind.isRoutine()`` identifies
these kinds and ``getName()`` is null. Argument mode, optional name
and ``ColDataType`` are preserved. An omitted signature has null arguments;
``f()`` has an empty argument list. The signature identifies a routine and is not
a function call. An ``AGGREGATE`` requires a signature and also accepts ``(*)`` and
ordered-set signatures such as ``(float8 ORDER BY integer)`` or ``(ORDER BY integer)``.

For ``COMMENT ON CONSTRAINT ck ON app.t IS NULL``, ``getName()`` is the constraint
name and ``getRelation()`` is its owning table. ``TRIGGER``, ``POLICY`` and ``RULE``
targets use the same fields; ``Kind.isOwnedByRelation()`` identifies these kinds.
Only a constraint accepts ``ON DOMAIN app.d``, which sets ``isOnDomain()`` and stores
the domain as the owner instead. A ``CAST`` target exposes ``getSourceType()`` and
``getTargetType()``. ``NULL`` removes the comment and remains represented by a null
``Comment.getComment()``.

Table discovery visits tables, column owners, views, materialized views, foreign
tables and the tables owning constraints, triggers, policies and rules. It does not
infer an index's table or treat domains, sequences, types, routines or other catalog
objects as tables. Statement visitors visit comment literals, and custom SQL deparsers
can replace the explicit relation or literal. Feature analysis reports a schema
modification. Validation exposes a separate ``commentOn...`` capability for each
additional target kind.

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
``AlterExpression.getConstraint()``. Its ``getName()`` is the optional new constraint
name, while ``getExistingIndexName()`` identifies the existing index. This is
separate from an index declaration's name, columns and access method.

.. code-block:: java

    Alter alter = (Alter) CCJSqlParserUtil.parse(
        "ALTER TABLE t ADD CONSTRAINT uq UNIQUE USING INDEX i",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    ConstraintUsingIndex constraint = (ConstraintUsingIndex)
        alter.getAlterExpressions().get(0).getConstraint();
    constraint.setExistingIndexName("replacement_index");
    constraint.setName("replacement_constraint");

``getConstraintAttributes()`` exposes deferrability and initial timing when
present. The same AST can be constructed with ``new ConstraintUsingIndex()``
and its fluent ``withName``, ``withType`` and ``withExistingIndexName`` methods.

Transaction statements
======================

``START TRANSACTION`` produces a ``StartTransaction`` with an ordered list of typed
transaction modes. PostgreSQL isolation levels, ``READ ONLY`` / ``READ WRITE``, and
``[NOT] DEFERRABLE`` are supported, as is MySQL ``WITH CONSISTENT SNAPSHOT``.
PostgreSQL accepts mode lists with or without commas; output uses commas.
The explicit MySQL dialect rejects a transaction containing both ``READ ONLY`` and
``READ WRITE``, while retaining repeated occurrences of the same mode.

Choose the dialect explicitly to parse ``BEGIN`` as a transaction command:

.. code-block:: java

    StartTransaction transaction = (StartTransaction) CCJSqlParserUtil.parse(
        "BEGIN ISOLATION LEVEL SERIALIZABLE READ ONLY",
        parser -> parser.withDialect(Dialect.POSTGRESQL));
    transaction.getModes().set(1, StartTransaction.Mode.READ_WRITE);
    // BEGIN ISOLATION LEVEL SERIALIZABLE, READ WRITE
    String sql = transaction.toString();

Without a dialect, ``BEGIN`` retains the existing procedural block interpretation.
MySQL and PostgreSQL dialects distinguish ``BEGIN`` transactions from
``BEGIN SELECT ...; END`` blocks by the following tokens. Oracle blocks and opaque
routine bodies retain their existing handling.

``Commit`` and ``RollbackStatement`` expose optional ``TransactionKeyword``
(``WORK`` or PostgreSQL ``TRANSACTION``) and ``TransactionChain`` (``CHAIN`` or
``NO_CHAIN``). A null chain preserves the server's default behavior. The existing
rollback work/savepoint/force accessors remain available. ``RELEASE [SAVEPOINT]``
produces ``ReleaseSavepointStatement``; the MySQL dialect requires ``SAVEPOINT``.
Savepoint names retain identifier quoting. These statements are traversed by
statement visitors, deparsers and transaction feature analysis.

The validator checks the new statement and mode capabilities. PostgreSQL
``AND [NO] CHAIN`` is enabled beginning with its version 12 capability set.
This support does not include MySQL transaction completion ``[NO] RELEASE``,
PostgreSQL ``COMMIT PREPARED`` / ``ROLLBACK PREPARED``, or XA commands.
