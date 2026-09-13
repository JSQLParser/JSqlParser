These self-contained fixtures cover MATCH_RECOGNIZE parsing, AST rendering and
reparsing. `MatchRecognizeDialectTest.executionFixturesRoundTrip` reads the files
listed in `execution-cases.txt`; unit tests do not require a database connection.

The following external checks were run on 2026-09-13. For each fixture, both the
original SQL and SQL emitted by `StatementDeParser` executed successfully and
returned identical output:

| Fixture prefix | Executor | Cases |
| --- | --- | ---: |
| `bigquery-` | GoogleSQL reference evaluator, release `2026.9.1` | 15 |
| `oracle-` | Oracle AI Database 26ai Free, version `23.26.2.0.0` | 17 |

GoogleSQL is the reference evaluator, not the BigQuery service. Snowflake was not
available for execution; its documented syntax and precedence are covered by
separate parser/AST tests. Function eligibility, types, aggregation rules and
data-dependent errors are outside the parser's static validation.

The BigQuery cases exercise greedy/reluctant matching, overlap, longest-match
options, empty alternatives, adjacent anchors, parameter bounds, nested STRUCT
measures, aliases and the query reported in issue #2350. The Oracle cases cover
row output modes, empty/unmatched rows, SUBSET, PERMUTE, exclusion, RUNNING/FINAL,
skip targets, result aliases, empty groups and fixed reluctant quantifiers.

To reproduce execution, use the [official GoogleSQL executable](https://github.com/google/googlesql/releases/tag/2026.9.1)
with `--output_mode=json --sql_file=<fixture.sql>`. For `bigquery-parameter-bound.sql`,
also pass `--parameters='lo=1;hi=2'`. The macOS executable used here has SHA-256
`4237e700ddedbdc331bda4f2715ba97879b5fe4ab5bfdfd8812dbcc8cfbfc7c9`.
For Oracle, run each fixture through SQL*Plus with `WHENEVER SQLERROR EXIT SQL.SQLCODE`.
The local Oracle image was `ghcr.io/gvenzl/oracle-free:23.26.2-slim`, digest
`sha256:6c6b2555e19b81beb79f3429ac34f241b196785c0137125b871f0af8507685b1`.

Render the same statement through `StatementDeParser`, using the corresponding
`Dialect.BIGQUERY` or `Dialect.ORACLE`, and run that SQL with the same parameters
and data. Compare the original and rendered outputs. The Java tests additionally
check `toString()`/deparser agreement and stable output after reparsing.
