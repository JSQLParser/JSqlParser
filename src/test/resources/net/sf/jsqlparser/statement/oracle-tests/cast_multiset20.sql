select deptno
     ,      cast(collect(job) as varchar2_ntt) as jobs
     from   emp
     group  by
            deptno