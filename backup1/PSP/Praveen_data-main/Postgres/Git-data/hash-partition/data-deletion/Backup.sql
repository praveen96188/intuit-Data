--PSP_PSTUB_DDITEM
create table MCHOUBEY.PSP_PSTUB_DDITEM_TEMP_PSP_25432 as
select * from PSPADM.PSP_PSTUB_DDITEM where COMPANY_FK is null;

--PSP_PSTUB_MSG
create table MCHOUBEY.PSP_PSTUB_MSG_TEMP_PSP_25432 as
select * from PSPADM.PSP_PSTUB_MSG where COMPANY_FK is null;

--PSP_PSTUB_PAID_TIMEOFF_ITEM
create table MCHOUBEY.PSP_PSTUB_PAID_TIMEOFF_ITEM_TEMP_PSP_25432 as
select * from PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM where COMPANY_FK is null;

--PSP_PSTUB_PAY_ITEM
create table MCHOUBEY.PSP_PSTUB_PAY_ITEM_TEMP_PSP_25432 as
select * from PSPADM.PSP_PSTUB_PAY_ITEM where COMPANY_FK is null;

--PSP_PAYSTUB
create table MCHOUBEY.PSP_PAYSTUB_TEMP_PSP_25432 as
select * from PSPADM.PSP_PAYSTUB where COMPANY_FK is null;

--PSP_EFTPS_PAYMENT_DETAIL
create table MCHOUBEY.PSP_EFTPS_PAYMENT_DETAIL_TEMP_PSP_25432 as
select * from PSPADM.PSP_EFTPS_PAYMENT_DETAIL where COMPANY_FK is null;

--PSP_VOIDED_CHECK
create table MCHOUBEY.PSP_VOIDED_CHECK_TEMP_PSP_25432 as
select * from PSPADM.PSP_VOIDED_CHECK where COMPANY_FK is null;

--PSP_FSET_FILING_DETAIL
create table MCHOUBEY.PSP_FSET_FILING_DETAIL_TEMP_PSP_25432 as
select * from PSPADM.PSP_FSET_FILING_DETAIL where COMPANY_FK is null;

--PSP_QBDT_TRANSACTION_INFO
create table MCHOUBEY.PSP_QBDT_TRANSACTION_INFO_TEMP_PSP_25432 as
select * from PSPADM.PSP_QBDT_TRANSACTION_INFO where COMPANY_FK is null;

--PSP_QBDT_PAYLINE_INFO
create table MCHOUBEY.PSP_QBDT_PAYLINE_INFO_TEMP_PSP_25432 as
select * from PSPADM.PSP_QBDT_PAYLINE_INFO where COMPANY_FK is null;

--PSP_FINANCIAL_TRANS_STATE
create table MCHOUBEY.PSP_FINANCIAL_TRANS_STATE_TEMP_PSP_25432 as
select * from PSPADM.PSP_FINANCIAL_TRANS_STATE where COMPANY_FK is null and FINANCIAL_TRANS_STATE_SEQ in
                                                                            ('2189a934-6dc4-40b0-ab25-e87ce687f50d', '2f70152e-3965-4e0a-bb77-8d0a87973fd8',
                                                                             '94fe50c4-061c-4b2b-93c1-d63291077764', '4dbf97a9-4f3d-4277-aa3b-4871af36a1a8',
                                                                             '292f50d6-640d-4bfd-9a7a-fc5bb2719cca', '6baa75f1-829e-49b4-a192-7cc3b1969908',
                                                                             '15854ecb-f5c4-449e-95bf-5601ccb61673', 'da16020c-31f7-45fe-aea9-523f1759da34',
                                                                             '2429a704-2303-49c1-a23b-9e0a830e7cc4', 'f284c84a-f185-4cd9-a8e0-3f380d19b191',
                                                                             '55d9aec1-e51b-4de7-bc37-da9461b2f7da', 'a08ef178-9188-4e77-ab7c-8a7d2bd989bc',
                                                                             '5d79ceb6-c84f-421e-b48c-b12482b07568', '88a2244c-11e2-4f02-a353-7facb94cf87f',
                                                                             '1226c1ba-eda2-4dc3-89c5-168076890dda', '479e5d20-6b70-4734-9c32-8b698b0c7196',
                                                                             '8eb40271-f2f0-46d9-9227-57e3f051e18d', 'b7124155-9325-4f03-851d-2ce3f24cca78');

--PSP_TAX_PAYMENT_ON_HOLD_REASON
create table MCHOUBEY.PSP_TAX_PAYMENT_ON_HOLD_REASON_TEMP_PSP_25432 as
select * from PSPADM.PSP_TAX_PAYMENT_ON_HOLD_REASON where COMPANY_FK is null;

--PSP_TRANSACTION_OFFLOAD_BATCH
create table MCHOUBEY.PSP_TRANSACTION_OFFLOAD_BATCH_TEMP_PSP_25432 as
select * from PSPADM.PSP_TRANSACTION_OFFLOAD_BATCH where COMPANY_FK is null;