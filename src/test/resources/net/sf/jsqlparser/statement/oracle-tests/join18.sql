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




	

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM