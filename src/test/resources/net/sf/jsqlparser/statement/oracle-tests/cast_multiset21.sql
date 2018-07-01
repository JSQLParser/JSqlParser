select deptno
     ,      cast(
               collect(distinct job)
                  as varchar2_ntt) as distinct_jobs
     from   emp
     group  by
            deptno