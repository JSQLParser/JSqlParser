insert into (
select deptno, dname, loc
from dept
where deptno < 30 with check option)
values (99, 'travel', 'seattle')
