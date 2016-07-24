select deptno
     ,      cast(
               collect(unique job)
                  as varchar2_ntt) as distinct_jobs
     from   emp
     group  by
            deptno