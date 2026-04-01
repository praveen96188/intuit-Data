select machine, count(1)
  from gv$session 
 group by rollup(machine) 
 order by 1;
