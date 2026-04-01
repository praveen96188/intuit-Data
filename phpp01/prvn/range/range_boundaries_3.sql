set lines 300 echo on timing on echo on feedback on trimspool on
spool range_boundaries_3


select /*+ parallel(a,4) */a.TAX_SEQ From
    (
        Select /*+ parallel(e,4) */Row_Number() OVER (Order by TAX_SEQ) rno, e.TAX_SEQ
        From pspadm.PSP_TAX e order by rno
    ) a
Where rno in (375788600,751577201,1127365801,1503154402,1878943002,2254731602,2630520203,3006308803,3382097403,3757886004,4133674604,4509463205,4885251805,5261040405,5636829006,6012617606);


select /*+ parallel(a,4) */a.PSTUB_PAY_ITEM_SEQ From
    (
        Select /*+ parallel(e,4) */Row_Number() OVER (Order by PSTUB_PAY_ITEM_SEQ) rno, e.PSTUB_PAY_ITEM_SEQ
        From pspadm.PSP_PSTUB_PAY_ITEM e order by rno
    ) a
Where rno in (308940403,617880807,926821210,1235761614,1544702017,1853642421,2162582824,2471523228,2780463631,3089404034,3398344438,3707284841,4016225245,4325165648,4634106052,4943046455);



spool off;
