---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
begin
	forall i in indices of :jobs
		update emp
                   set ename = lower(ename)
                 where job = :jobs(i)
                 returning empno
                 bulk collect into :empnos;
end;

--@FAILURE: Encountered unexpected token: "begin" "BEGIN" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "begin" "BEGIN" recorded first on 21 Dec 2021, 15:15:16