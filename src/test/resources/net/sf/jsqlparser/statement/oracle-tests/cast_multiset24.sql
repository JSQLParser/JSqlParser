select deptno
     ,      cast(
               collect(
                  distinct job
                  order by job
                  ) as varchar2_ntt) as distinct_ordered_jobs
     from   emp
     group  by
            deptno