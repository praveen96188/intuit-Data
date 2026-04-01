\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


select 'creating index on pstub_pay_item' as status;
create index psp_pstub_pay_item_fk1_p0 on pspadm.psp_pstub_pay_item_p0 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p1 on pspadm.psp_pstub_pay_item_p1 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p10 on pspadm.psp_pstub_pay_item_p10 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p11 on pspadm.psp_pstub_pay_item_p11 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p12 on pspadm.psp_pstub_pay_item_p12 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p13 on pspadm.psp_pstub_pay_item_p13 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p14 on pspadm.psp_pstub_pay_item_p14 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p15 on pspadm.psp_pstub_pay_item_p15 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p2 on pspadm.psp_pstub_pay_item_p2 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p3 on pspadm.psp_pstub_pay_item_p3 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p4 on pspadm.psp_pstub_pay_item_p4 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p5 on pspadm.psp_pstub_pay_item_p5 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p6 on pspadm.psp_pstub_pay_item_p6 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p7 on pspadm.psp_pstub_pay_item_p7 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p8 on pspadm.psp_pstub_pay_item_p8 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk1_p9 on pspadm.psp_pstub_pay_item_p9 USING BTREE (company_fk ASC,paystub_fk ASC);
create index psp_pstub_pay_item_fk2_p0 on pspadm.psp_pstub_pay_item_p0 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p1 on pspadm.psp_pstub_pay_item_p1 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p10 on pspadm.psp_pstub_pay_item_p10 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p11 on pspadm.psp_pstub_pay_item_p11 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p12 on pspadm.psp_pstub_pay_item_p12 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p13 on pspadm.psp_pstub_pay_item_p13 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p14 on pspadm.psp_pstub_pay_item_p14 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p15 on pspadm.psp_pstub_pay_item_p15 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p2 on pspadm.psp_pstub_pay_item_p2 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p3 on pspadm.psp_pstub_pay_item_p3 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p4 on pspadm.psp_pstub_pay_item_p4 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p5 on pspadm.psp_pstub_pay_item_p5 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p6 on pspadm.psp_pstub_pay_item_p6 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p7 on pspadm.psp_pstub_pay_item_p7 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p8 on pspadm.psp_pstub_pay_item_p8 USING BTREE (company_fk ASC);
create index psp_pstub_pay_item_fk2_p9 on pspadm.psp_pstub_pay_item_p9 USING BTREE (company_fk ASC);
select  'psp_pstub_pay_item_ completed' as status;
SELECT CURRENT_TIMESTAMP;

