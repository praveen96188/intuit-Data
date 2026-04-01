\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2012_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2013_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2014_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2015_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2016_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2017_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2018_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2019_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2020_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2021_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2022_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2023_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usage_2024_old;
SELECT CURRENT_TIMESTAMP;
