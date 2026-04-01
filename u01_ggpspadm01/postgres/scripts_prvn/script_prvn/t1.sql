select machine, count(1) from gv$session where machine!='ip-172-19-1-6' having count(1)>50 group by machine order by 1;
