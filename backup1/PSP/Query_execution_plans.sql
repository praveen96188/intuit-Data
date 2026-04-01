--index online
CREATE INDEX CONCURRENTLY idx_led_op_cr_date ON pspadm.psp_ledger_operation USING BTREE (created_date);
CREATE INDEX CONCURRENTLY idx_led_op_job_cr_date ON pspadm.psp_ledger_operation_job  USING BTREE (created_date);
--without onine
CREATE INDEX  idx_led_op_cr_date ON pspadm.psp_ledger_operation USING BTREE (created_date);
CREATE INDEX  idx_led_op_job_cr_date ON pspadm.psp_ledger_operation_job  USING BTREE (created_date);
--drop index
drop index idx_led_op_cr_date;
drop index idx_led_op_job_cr_date;

Query 1:
explain analyze SELECT
    lo.ledger_operation_job_fk as job_id,
    lo.memo
FROM psp_ledger_operation lo
WHERE lo.created_date >= (CURRENT_DATE - INTERVAL '24 months')
  AND lo.memo LIKE 'TOR%'
GROUP BY lo.ledger_operation_job_fk, lo.memo;


QUERY 1: 
--Before index creation execution plan
pitparmo=> explain analyze SELECT
pitparmo->     lo.ledger_operation_job_fk as job_id,
pitparmo->     lo.memo
pitparmo-> FROM psp_ledger_operation lo
pitparmo-> WHERE lo.created_date >= (CURRENT_DATE - INTERVAL '24 months')
pitparmo->   AND lo.memo LIKE 'TOR%'
pitparmo-> GROUP BY lo.ledger_operation_job_fk, lo.memo;
                                                                   QUERY PLAN                                                                    
-------------------------------------------------------------------------------------------------------------------------------------------------
 HashAggregate  (cost=3417259.35..3869528.94 rows=4423608 width=65) (actual time=28136.370..28137.861 rows=2314 loops=1)
   Group Key: ledger_operation_job_fk, memo
   Planned Partitions: 32  Batches: 1  Memory Usage: 6689kB
   ->  Seq Scan on psp_ledger_operation lo  (cost=0.00..2573990.08 rows=17409430 width=65) (actual time=10.994..23242.597 rows=17368036 loops=1)
         Filter: (((memo)::text ~~ 'TOR%'::text) AND (created_date >= (CURRENT_DATE - '2 years'::interval)))
         Rows Removed by Filter: 27145865
 Planning Time: 6.613 ms
 Execution Time: 28139.832 ms


--After  index creation execution plan
--24 months
pitparmo=> explain analyze SELECT
pitparmo->     lo.ledger_operation_job_fk as job_id,
pitparmo->     lo.memo
pitparmo-> FROM psp_ledger_operation lo
pitparmo-> WHERE lo.created_date >= (CURRENT_DATE - INTERVAL '24 months')
pitparmo->   AND lo.memo LIKE 'TOR%'
pitparmo-> GROUP BY lo.ledger_operation_job_fk, lo.memo;
                                                                                QUERY PLAN                                                                                 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 HashAggregate  (cost=2008132.54..2459989.04 rows=4420019 width=65) (actual time=17591.268..17592.841 rows=2314 loops=1)
   Group Key: ledger_operation_job_fk, memo
   Planned Partitions: 32  Batches: 1  Memory Usage: 6673kB
   ->  Index Scan using idx_led_op_cr_date on psp_ledger_operation lo  (cost=0.45..1165642.82 rows=17393336 width=65) (actual time=2.202..11528.884 rows=17368036 loops=1)
         Index Cond: (created_date >= (CURRENT_DATE - '2 years'::interval))
         Filter: ((memo)::text ~~ 'TOR%'::text)
         Rows Removed by Filter: 70210
 Planning Time: 0.675 ms
 Execution Time: 17594.833 ms
(9 rows)
--36 months
pitparmo=> explain analyze SELECT
pitparmo->     lo.ledger_operation_job_fk as job_id,
pitparmo->     lo.memo
pitparmo-> FROM psp_ledger_operation lo
pitparmo-> WHERE lo.created_date >= (CURRENT_DATE - INTERVAL '36 months')
pitparmo->   AND lo.memo LIKE 'TOR%'
pitparmo-> GROUP BY lo.ledger_operation_job_fk, lo.memo;
                                                                                QUERY PLAN                                                                                 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 HashAggregate  (cost=2277919.60..2791687.53 rows=4440092 width=65) (actual time=19717.290..19718.992 rows=2722 loops=1)
   Group Key: ledger_operation_job_fk, memo
   Planned Partitions: 32  Batches: 1  Memory Usage: 6673kB
   ->  Index Scan using idx_led_op_cr_date on psp_ledger_operation lo  (cost=0.45..1307894.43 rows=20026326 width=65) (actual time=1.473..12729.540 rows=20035838 loops=1)
         Index Cond: (created_date >= (CURRENT_DATE - '3 years'::interval))
         Filter: ((memo)::text ~~ 'TOR%'::text)
         Rows Removed by Filter: 135377
 Planning Time: 0.187 ms
 Execution Time: 19719.356 ms
--48 months
pitparmo=> explain analyze SELECT
pitparmo->     lo.ledger_operation_job_fk as job_id,
pitparmo->     lo.memo
pitparmo-> FROM psp_ledger_operation lo
pitparmo-> WHERE lo.created_date >= (CURRENT_DATE - INTERVAL '48 months')
pitparmo->   AND lo.memo LIKE 'TOR%'
pitparmo-> GROUP BY lo.ledger_operation_job_fk, lo.memo;
                                                                                QUERY PLAN                                                                                 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 HashAggregate  (cost=2651920.21..3251326.92 rows=4449141 width=65) (actual time=22403.281..22405.115 rows=3229 loops=1)
   Group Key: ledger_operation_job_fk, memo
   Planned Partitions: 32  Batches: 1  Memory Usage: 6673kB
   ->  Index Scan using idx_led_op_cr_date on psp_ledger_operation lo  (cost=0.45..1505095.27 rows=23676386 width=65) (actual time=0.046..14138.199 rows=23687566 loops=1)
         Index Cond: (created_date >= (CURRENT_DATE - '4 years'::interval))
         Filter: ((memo)::text ~~ 'TOR%'::text)
         Rows Removed by Filter: 174222
 Planning Time: 0.204 ms
 Execution Time: 22405.520 ms



Execution time dropped by ~37% (from ~28.1 sec to ~17.6 sec).
Index scan replaced sequential scan, reducing the number of processed rows.




 QUERY 2:

 explain analyze  SELECT
    job.ledger_operation_job_seq as job_id,
    job.creator_id, job.created_date, job.description, job.status,
    COUNT(*) as total_operations,
    SUM(CASE WHEN lo.status = 'Completed' THEN 1 ELSE 0 END) as completed_count,
    SUM(CASE WHEN lo.status = 'Error' THEN 1 ELSE 0 END) as error_count
FROM psp_ledger_operation_job job,
     psp_ledger_operation lo
WHERE job.created_date >= (CURRENT_DATE - INTERVAL '24 months')
  AND lo.ledger_operation_job_fk = job.ledger_operation_job_seq
GROUP BY job.ledger_operation_job_seq, job.creator_id, job.created_date,
         job.description, job.status, job.start_time, job.finish_time, job.job_type
ORDER BY job.created_date DESC;

--Before index creation execution plan
pitparmo=> explain analyze  SELECT
pitparmo->     job.ledger_operation_job_seq as job_id,
pitparmo->     job.creator_id, job.created_date, job.description, job.status,
pitparmo->     COUNT(*) as total_operations,
pitparmo->     SUM(CASE WHEN lo.status = 'Completed' THEN 1 ELSE 0 END) as completed_count,
pitparmo->     SUM(CASE WHEN lo.status = 'Error' THEN 1 ELSE 0 END) as error_count
pitparmo-> FROM psp_ledger_operation_job job,
pitparmo->      psp_ledger_operation lo
pitparmo-> WHERE job.created_date >= (CURRENT_DATE - INTERVAL '24 months')
pitparmo->   AND lo.ledger_operation_job_fk = job.ledger_operation_job_seq
pitparmo-> GROUP BY job.ledger_operation_job_seq, job.creator_id, job.created_date,
pitparmo->          job.description, job.status, job.start_time, job.finish_time, job.job_type
pitparmo-> ORDER BY job.created_date DESC;
                                                                                      QUERY PLAN                                                                                      
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Sort  (cost=2009061.37..2009067.81 rows=2577 width=144) (actual time=9179.782..9187.153 rows=2596 loops=1)
   Sort Key: job.created_date DESC
   Sort Method: quicksort  Memory: 538kB
   ->  Finalize GroupAggregate  (cost=2008236.71..2008915.36 rows=2577 width=144) (actual time=9173.931..9185.911 rows=2596 loops=1)
         Group Key: job.ledger_operation_job_seq
         ->  Gather Merge  (cost=2008236.71..2008838.05 rows=5154 width=144) (actual time=9173.920..9183.550 rows=7250 loops=1)
               Workers Planned: 2
               Workers Launched: 2
               ->  Sort  (cost=2007236.69..2007243.13 rows=2577 width=144) (actual time=9168.105..9168.278 rows=2417 loops=3)
                     Sort Key: job.ledger_operation_job_seq
                     Sort Method: quicksort  Memory: 500kB
                     Worker 0:  Sort Method: quicksort  Memory: 503kB
                     Worker 1:  Sort Method: quicksort  Memory: 508kB
                     ->  Partial HashAggregate  (cost=2007064.91..2007090.68 rows=2577 width=144) (actual time=9163.782..9164.447 rows=2417 loops=3)
                           Group Key: job.ledger_operation_job_seq
                           Batches: 1  Memory Usage: 1137kB
                           Worker 0:  Batches: 1  Memory Usage: 1137kB
                           Worker 1:  Batches: 1  Memory Usage: 1137kB
                           ->  Hash Join  (cost=726.35..1918096.49 rows=5931228 width=129) (actual time=13.608..7383.908 rows=5812749 loops=3)
                                 Hash Cond: ((lo.ledger_operation_job_fk)::text = (job.ledger_operation_job_seq)::text)
                                 ->  Parallel Seq Scan on psp_ledger_operation lo  (cost=0.00..1868618.18 rows=18562418 width=46) (actual time=0.019..4352.985 rows=14837967 loops=3)
                                 ->  Hash  (cost=694.14..694.14 rows=2577 width=120) (actual time=13.556..13.557 rows=2596 loops=3)
                                       Buckets: 4096  Batches: 1  Memory Usage: 365kB
                                       ->  Seq Scan on psp_ledger_operation_job job  (cost=0.00..694.14 rows=2577 width=120) (actual time=0.553..12.688 rows=2596 loops=3)
                                             Filter: (created_date >= (CURRENT_DATE - '2 years'::interval))
                                             Rows Removed by Filter: 5469
 Planning Time: 15.863 ms
 Execution Time: 9187.783 ms

----After  index creation execution plan
--24 months
pitparmo=> explain analyze  SELECT
pitparmo->     job.ledger_operation_job_seq as job_id,
pitparmo->     job.creator_id, job.created_date, job.description, job.status,
pitparmo->     COUNT(*) as total_operations,
pitparmo->     SUM(CASE WHEN lo.status = 'Completed' THEN 1 ELSE 0 END) as completed_count,
pitparmo->     SUM(CASE WHEN lo.status = 'Error' THEN 1 ELSE 0 END) as error_count
pitparmo-> FROM psp_ledger_operation_job job,
pitparmo->      psp_ledger_operation lo
pitparmo-> WHERE job.created_date >= (CURRENT_DATE - INTERVAL '24 months')
pitparmo->   AND lo.ledger_operation_job_fk = job.ledger_operation_job_seq
pitparmo-> GROUP BY job.ledger_operation_job_seq, job.creator_id, job.created_date,
pitparmo->          job.description, job.status, job.start_time, job.finish_time, job.job_type
pitparmo-> ORDER BY job.created_date DESC;
                                                                                               QUERY PLAN                                                                                                
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Sort  (cost=2008676.15..2008682.59 rows=2576 width=144) (actual time=9124.176..9131.584 rows=2596 loops=1)
   Sort Key: job.created_date DESC
   Sort Method: quicksort  Memory: 538kB
   ->  Finalize GroupAggregate  (cost=2007851.82..2008530.21 rows=2576 width=144) (actual time=9118.314..9130.386 rows=2596 loops=1)
         Group Key: job.ledger_operation_job_seq
         ->  Gather Merge  (cost=2007851.82..2008452.93 rows=5152 width=144) (actual time=9118.304..9128.022 rows=7252 loops=1)
               Workers Planned: 2
               Workers Launched: 2
               ->  Sort  (cost=2006851.80..2006858.24 rows=2576 width=144) (actual time=9112.673..9112.851 rows=2417 loops=3)
                     Sort Key: job.ledger_operation_job_seq
                     Sort Method: quicksort  Memory: 497kB
                     Worker 0:  Sort Method: quicksort  Memory: 507kB
                     Worker 1:  Sort Method: quicksort  Memory: 508kB
                     ->  Partial HashAggregate  (cost=2006680.10..2006705.86 rows=2576 width=144) (actual time=9108.331..9109.006 rows=2417 loops=3)
                           Group Key: job.ledger_operation_job_seq
                           Batches: 1  Memory Usage: 1137kB
                           Worker 0:  Batches: 1  Memory Usage: 1137kB
                           Worker 1:  Batches: 1  Memory Usage: 1137kB
                           ->  Hash Join  (cost=636.63..1917817.88 rows=5924148 width=129) (actual time=3.043..7341.424 rows=5812749 loops=3)
                                 Hash Cond: ((lo.ledger_operation_job_fk)::text = (job.ledger_operation_job_seq)::text)
                                 ->  Parallel Seq Scan on psp_ledger_operation lo  (cost=0.00..1868468.58 rows=18547458 width=46) (actual time=0.006..4323.697 rows=14837967 loops=3)
                                 ->  Hash  (cost=604.43..604.43 rows=2576 width=120) (actual time=2.977..2.977 rows=2596 loops=3)
                                       Buckets: 4096  Batches: 1  Memory Usage: 365kB
                                       ->  Index Scan using idx_led_op_job_cr_date on psp_ledger_operation_job job  (cost=0.29..604.43 rows=2576 width=120) (actual time=0.028..2.082 rows=2596 loops=3)
                                             Index Cond: (created_date >= (CURRENT_DATE - '2 years'::interval))
 Planning Time: 0.885 ms
 Execution Time: 9132.188 ms
(27 rows)
--36 months
pitparmo=> explain analyze  SELECT
pitparmo->     job.ledger_operation_job_seq as job_id,
pitparmo->     job.creator_id, job.created_date, job.description, job.status,
pitparmo->     COUNT(*) as total_operations,
pitparmo->     SUM(CASE WHEN lo.status = 'Completed' THEN 1 ELSE 0 END) as completed_count,
pitparmo->     SUM(CASE WHEN lo.status = 'Error' THEN 1 ELSE 0 END) as error_count
pitparmo-> FROM psp_ledger_operation_job job,
pitparmo->      psp_ledger_operation lo
pitparmo-> WHERE job.created_date >= (CURRENT_DATE - INTERVAL '36 months')
pitparmo->   AND lo.ledger_operation_job_fk = job.ledger_operation_job_seq
pitparmo-> GROUP BY job.ledger_operation_job_seq, job.creator_id, job.created_date,
pitparmo->          job.description, job.status, job.start_time, job.finish_time, job.job_type
pitparmo-> ORDER BY job.created_date DESC;
                                                                                               QUERY PLAN                                                                                                
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Sort  (cost=2043470.56..2043479.49 rows=3572 width=144) (actual time=9664.036..9671.071 rows=3613 loops=1)
   Sort Key: job.created_date DESC
   Sort Method: quicksort  Memory: 734kB
   ->  Finalize GroupAggregate  (cost=2042319.08..2043259.77 rows=3572 width=144) (actual time=9656.275..9669.447 rows=3613 loops=1)
         Group Key: job.ledger_operation_job_seq
         ->  Gather Merge  (cost=2042319.08..2043152.61 rows=7144 width=144) (actual time=9656.265..9666.354 rows=9233 loops=1)
               Workers Planned: 2
               Workers Launched: 2
               ->  Sort  (cost=2041319.06..2041327.99 rows=3572 width=144) (actual time=9649.143..9649.368 rows=3078 loops=3)
                     Sort Key: job.ledger_operation_job_seq
                     Sort Method: quicksort  Memory: 618kB
                     Worker 0:  Sort Method: quicksort  Memory: 618kB
                     Worker 1:  Sort Method: quicksort  Memory: 642kB
                     ->  Partial HashAggregate  (cost=2041072.55..2041108.27 rows=3572 width=144) (actual time=9643.479..9644.324 rows=3078 loops=3)
                           Group Key: job.ledger_operation_job_seq
                           Batches: 1  Memory Usage: 1137kB
                           Worker 0:  Batches: 1  Memory Usage: 1137kB
                           Worker 1:  Batches: 1  Memory Usage: 1137kB
                           ->  Hash Join  (cost=670.86..1917852.11 rows=8214696 width=129) (actual time=4.102..7587.538 rows=6723738 loops=3)
                                 Hash Cond: ((lo.ledger_operation_job_fk)::text = (job.ledger_operation_job_seq)::text)
                                 ->  Parallel Seq Scan on psp_ledger_operation lo  (cost=0.00..1868468.58 rows=18547458 width=46) (actual time=0.006..4363.019 rows=14837967 loops=3)
                                 ->  Hash  (cost=626.21..626.21 rows=3572 width=120) (actual time=4.051..4.052 rows=3613 loops=3)
                                       Buckets: 4096  Batches: 1  Memory Usage: 520kB
                                       ->  Index Scan using idx_led_op_job_cr_date on psp_ledger_operation_job job  (cost=0.29..626.21 rows=3572 width=120) (actual time=0.036..2.778 rows=3613 loops=3)
                                             Index Cond: (created_date >= (CURRENT_DATE - '3 years'::interval))
 Planning Time: 0.458 ms
 Execution Time: 9671.824 ms
(27 rows)
--48 months
pitparmo=> explain analyze  SELECT
pitparmo->     job.ledger_operation_job_seq as job_id,
pitparmo->     job.creator_id, job.created_date, job.description, job.status,
pitparmo->     COUNT(*) as total_operations,
pitparmo->     SUM(CASE WHEN lo.status = 'Completed' THEN 1 ELSE 0 END) as completed_count,
pitparmo->     SUM(CASE WHEN lo.status = 'Error' THEN 1 ELSE 0 END) as error_count
pitparmo-> FROM psp_ledger_operation_job job,
pitparmo->      psp_ledger_operation lo
pitparmo-> WHERE job.created_date >= (CURRENT_DATE - INTERVAL '48 months')
pitparmo->   AND lo.ledger_operation_job_fk = job.ledger_operation_job_seq
pitparmo-> GROUP BY job.ledger_operation_job_seq, job.creator_id, job.created_date,
pitparmo->          job.description, job.status, job.start_time, job.finish_time, job.job_type
pitparmo-> ORDER BY job.created_date DESC;
                                                                                               QUERY PLAN                                                                                                
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Sort  (cost=2063348.70..2063359.05 rows=4141 width=144) (actual time=10287.206..10294.332 rows=4189 loops=1)
   Sort Key: job.created_date DESC
   Sort Method: quicksort  Memory: 928kB
   ->  Finalize GroupAggregate  (cost=2062009.38..2063099.91 rows=4141 width=144) (actual time=10278.062..10292.378 rows=4189 loops=1)
         Group Key: job.ledger_operation_job_seq
         ->  Gather Merge  (cost=2062009.38..2062975.68 rows=8282 width=144) (actual time=10278.052..10288.759 rows=10914 loops=1)
               Workers Planned: 2
               Workers Launched: 2
               ->  Sort  (cost=2061009.35..2061019.71 rows=4141 width=144) (actual time=10270.831..10271.103 rows=3638 loops=3)
                     Sort Key: job.ledger_operation_job_seq
                     Sort Method: quicksort  Memory: 733kB
                     Worker 0:  Sort Method: quicksort  Memory: 713kB
                     Worker 1:  Sort Method: quicksort  Memory: 717kB
                     ->  Partial HashAggregate  (cost=2060719.16..2060760.57 rows=4141 width=144) (actual time=10263.987..10265.022 rows=3638 loops=3)
                           Group Key: job.ledger_operation_job_seq
                           Batches: 1  Memory Usage: 1233kB
                           Worker 0:  Batches: 1  Memory Usage: 1233kB
                           Worker 1:  Batches: 1  Memory Usage: 1233kB
                           ->  Hash Join  (cost=689.13..1917870.38 rows=9523252 width=129) (actual time=4.865..7838.033 rows=7953929 loops=3)
                                 Hash Cond: ((lo.ledger_operation_job_fk)::text = (job.ledger_operation_job_seq)::text)
                                 ->  Parallel Seq Scan on psp_ledger_operation lo  (cost=0.00..1868468.58 rows=18547458 width=46) (actual time=0.013..4355.580 rows=14837967 loops=3)
                                 ->  Hash  (cost=637.37..637.37 rows=4141 width=120) (actual time=4.799..4.800 rows=4189 loops=3)
                                       Buckets: 8192  Batches: 1  Memory Usage: 626kB
                                       ->  Index Scan using idx_led_op_job_cr_date on psp_ledger_operation_job job  (cost=0.29..637.37 rows=4141 width=120) (actual time=0.031..3.319 rows=4189 loops=3)
                                             Index Cond: (created_date >= (CURRENT_DATE - '4 years'::interval))
 Planning Time: 0.482 ms
 Execution Time: 10295.161 ms
(27 rows)
