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

