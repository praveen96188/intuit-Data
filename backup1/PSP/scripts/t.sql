select machine, count(1)
  from gv$session
where machine not like 'INTU%' and machine not like 'ggpspibob1%' and machine not like 'intuit%' and machine not like 'ip-%'
 group by rollup(machine)
 order by 1;

