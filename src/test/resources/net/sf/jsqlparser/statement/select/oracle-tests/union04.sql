---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
(
	select distinct job_id from hr.jobs
)
union all
(
	select distinct job_id from hr.job_history
	union all
	((((
		select distinct job_id from hr.job_history
		union all
		(
			select distinct job_id from hr.job_history
		)
	)))
	union all
		select distinct job_id from hr.job_history	
	)	
)
union all
(
	select distinct job_id from hr.job_history
	union all
	(
		select distinct job_id from hr.job_history
		union all
		(
			select distinct job_id from hr.job_history
		)
	)
)
union all
(
	select distinct job_id from hr.job_history
	union all
	select distinct job_id from hr.job_history
)
union all
	select distinct job_id from hr.job_history
union all
	select distinct job_id from hr.job_history
union all
	select distinct job_id from hr.job_history
union all
	select distinct job_id from hr.job_history	


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: (SELECT DISTINCT job_id FROM hr.jobs)UNION ALL(SELECT DISTINCT job_id FROM hr.job_history UNION ALL((((SELECT DISTINCT job_id FROM hr.job_history UNION ALL(SELECT DISTINCT job_id FROM hr.job_history))))UNION ALL SELECT DISTINCT job_id FROM hr.job_history))UNION ALL(SELECT DISTINCT job_id FROM hr.job_history UNION ALL(SELECT DISTINCT job_id FROM hr.job_history UNION ALL(SELECT DISTINCT job_id FROM hr.job_history)))UNION ALL(SELECT DISTINCT job_id FROM hr.job_history UNION ALL SELECT DISTINCT job_id FROM hr.job_history)UNION ALL SELECT DISTINCT job_id FROM hr.job_history UNION ALL SELECT DISTINCT job_id FROM hr.job_history UNION ALL SELECT DISTINCT job_id FROM hr.job_history UNION ALL SELECT DISTINCT job_id FROM hr.job_history recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: (select distinct job_id from hr.jobs)union all(select distinct job_id from hr.job_history union all((((select distinct job_id from hr.job_history union all(select distinct job_id from hr.job_history))))union all select distinct job_id from hr.job_history))union all(select distinct job_id from hr.job_history union all(select distinct job_id from hr.job_history union all(select distinct job_id from hr.job_history)))union all(select distinct job_id from hr.job_history union all select distinct job_id from hr.job_history)union all select distinct job_id from hr.job_history union all select distinct job_id from hr.job_history union all select distinct job_id from hr.job_history union all select distinct job_id from hr.job_history recorded first on 25 Oct 2021, 18:55:27