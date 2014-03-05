-- without check option
insert into (
select deptno, dname, loc
from dept
where deptno < 30)
values (98, 'travel', 'seattle')
