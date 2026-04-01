--
-- This script will be executed BEFORE the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.004.010.sql
--
-- Developers can hand code logic here for data migration purposes
--

Prompt Purging all paystub tables;

TRUNCATE TABLE  PSP_PSTUB_DDITEM;
TRUNCATE TABLE  PSP_PSTUB_PAID_TIMEOFF_ITEM;
TRUNCATE TABLE  PSP_PSTUB_MSG;
TRUNCATE TABLE PSP_PSTUB_PAY_ITEM;

alter table PSPADM.PSP_PSTUB_PAY_ITEM disable constraint PSP_PSTUB_PAY_ITEM_FK1;
alter table PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM disable constraint PSP_PSTUB_PAID_TIMEOFF_ITE_FK1;
alter table PSPADM.PSP_PSTUB_MSG disable constraint PSP_PSTUB_MSG_FK1;
alter table PSPADM.PSP_PSTUB_DDITEM disable constraint PSP_PSTUB_DDITEM_FK1;

TRUNCATE TABLE  PSP_PAYSTUB;

alter table PSPADM.PSP_PAYSTUB disable constraint PSP_PAYSTUB_FK2;
alter table PSPADM.PSP_PAYSTUB disable constraint PSP_PAYSTUB_FK1;
alter table PSPADM.PSP_PSTUB_EMPLOYEE_INFO disable constraint PSP_PSTUB_EMPLOYEE_INFO_FK2;
alter table PSPADM.PSP_PSTUB_EMPLOYER_INFO disable constraint PSP_PSTUB_EMPLOYER_INFO_FK1;

TRUNCATE TABLE  PSP_PSTUB_EMPLOYEE_PREFERENCE;
TRUNCATE TABLE  PSP_PSTUB_EMPLOYEE_INFO;
TRUNCATE TABLE  PSP_PSTUB_EMPLOYER_INFO;
TRUNCATE TABLE  PSP_PSTUB_ADDRESS;

alter table PSPADM.PSP_PSTUB_PAY_ITEM enable constraint PSP_PSTUB_PAY_ITEM_FK1;
alter table PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM enable constraint PSP_PSTUB_PAID_TIMEOFF_ITE_FK1;
alter table PSPADM.PSP_PSTUB_MSG enable constraint PSP_PSTUB_MSG_FK1;
alter table PSPADM.PSP_PSTUB_DDITEM enable constraint PSP_PSTUB_DDITEM_FK1;
alter table PSPADM.PSP_PAYSTUB enable constraint PSP_PAYSTUB_FK2;
alter table PSPADM.PSP_PAYSTUB enable constraint PSP_PAYSTUB_FK1;
alter table PSPADM.PSP_PSTUB_EMPLOYEE_INFO enable constraint PSP_PSTUB_EMPLOYEE_INFO_FK2;
alter table PSPADM.PSP_PSTUB_EMPLOYER_INFO enable constraint PSP_PSTUB_EMPLOYER_INFO_FK1;


Prompt Finished Purging;


