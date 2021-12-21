---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select   bio, rtrim (str_new, ';') new_str
  from   db_temp
model
   partition by (rownum rn)
dimension by (0 dim)
   measures (bio, bio || ';' str_new)
   rules
      iterate (1000) until (str_new[0] = previous (str_new[0]))
      (str_new [0] =
            regexp_replace (str_new[0], '(^|;)([^;]+;)(.*?;)?\2+', '\1\2\3'));

--@FAILURE: Encountered unexpected token: "partition" "PARTITION" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "partition" "PARTITION" recorded first on 21 Dec 2021, 15:15:16