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
--@FAILURE: Encountered unexpected token: "forall" <S_IDENTIFIER> recorded first on 9 Dec 2022, 14:03:29
--@FAILURE: Encountered: <S_IDENTIFIER> / "forall", at line 11, column 2, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08