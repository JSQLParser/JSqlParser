---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
extractvalue(value(t), '/select_list_item/pos') + 1 pos,
extractvalue(value(t), '/select_list_item/value') res,
extractvalue(value(t), '/select_list_item/nonnulls') nonnulls,
extractvalue(value(t), '/select_list_item/ndv') ndv,
extractvalue(value(t), '/select_list_item/split') split,
extractvalue(value(t), '/select_list_item/rsize') rsize,
extractvalue(value(t), '/select_list_item/rowcnt') rowcnt,
extract(value(t), '/select_list_item/hash_val').getclobval() hashval
from
table
(
	xmlsequence
	(
		extract(:b1 , '/process_result/select_list_item')
	)
) t


--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "(" recorded first on 21 Dec 2021, 15:15:17