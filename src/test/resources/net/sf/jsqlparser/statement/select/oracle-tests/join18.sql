---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select department_id as d_e_dept_id, e.last_name
   from departments
   full outer join employees on (a=b)
   left outer join employees on (a=b)
   right outer join employees on (a=b)
   join employees on (a=b)
   inner join employees on (a=b)
   cross join employees
   natural join employees




	

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT department_id AS d_e_dept_id,e.last_name FROM departments FULL OUTER JOIN employees ON(a=b)LEFT OUTER JOIN employees ON(a=b)RIGHT OUTER JOIN employees ON(a=b)JOIN employees ON(a=b)INNER JOIN employees ON(a=b)CROSS JOIN employees NATURAL JOIN employees recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select department_id as d_e_dept_id,e.last_name from departments full outer join employees on(a=b)left outer join employees on(a=b)right outer join employees on(a=b)join employees on(a=b)inner join employees on(a=b)cross join employees natural join employees recorded first on 25 Oct 2021, 18:55:26