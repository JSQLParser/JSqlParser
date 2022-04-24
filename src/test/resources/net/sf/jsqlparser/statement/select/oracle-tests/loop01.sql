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
--@FAILURE: begin forall i in indices of:jobs update emp set ename=lower(ename)where job=:jobs(i)returning empno bulk collect into:empnos ; end ; recorded first on 23 Apr 2022, 15:05:41
--@FAILURE: forall i in indices of:jobs update emp set ename=lower(ename)where job=:jobs(i)returning empno bulk collect into:empnos ; end ; recorded first on 23 Apr 2022, 15:48:28
--@FAILURE: Encountered unexpected token: "end" "END" recorded first on 23 Apr 2022, 16:09:20
--@FAILURE: Encountered unexpected token: ";" ";" recorded first on 23 Apr 2022, 23:14:35