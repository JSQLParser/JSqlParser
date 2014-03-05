select deptno
     ,      cast(
               set(collect(job))
                  as varchar2_ntt) as distinct_jobs
     from   emp
     group  by
            deptno