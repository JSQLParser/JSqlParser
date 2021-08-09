---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select country,prod,year,s
from sales_view_ref
model
partition by (country)
dimension by (prod, year)
measures (sale s)
ignore nav
-- cell_reference_options
unique dimension
-- here starts model_rules_clause
rules upsert sequential order
(
s[prod='mouse pad', year=2001] = s['mouse pad', 1999] + s['mouse pad', 2000],
s['standard mouse', 2002] = s['standard mouse', 2001]
)
order by country, prod, year



--@FAILURE: Encountered unexpected token: "partition" "PARTITION" recorded first on Aug 3, 2021, 7:20:08 AM