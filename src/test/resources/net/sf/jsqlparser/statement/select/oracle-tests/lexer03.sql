---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select :1, :X, :1 + 1, 1 + :2 from A where A=:3 and b= :4 and c= :5 and :A = :b


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT :1,:X,:1+1,1+:2 FROM A WHERE A=:3 AND b=:4 AND c=:5 AND :A=:b recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select :1,:x,:1+1,1+:2 from a where a=:3 and b=:4 and c=:5 and :a=:b recorded first on 25 Oct 2021, 18:55:26