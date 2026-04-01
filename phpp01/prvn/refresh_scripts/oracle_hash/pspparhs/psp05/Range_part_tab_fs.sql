set termout off timing on
spool Range_patition_fs

select /*+ FULL(ENTMT_MESSAGE_Y2024) parallel(ENTMT_MESSAGE_Y2024 ,4) */count(*) from pspadm.PSP_ENTITLEMENT_MESSAGE partition (ENTMT_MESSAGE_Y2024)ENTMT_MESSAGE_Y2024;
select /*+ FULL(ENTMT_MESSAGE_Y2025) parallel(ENTMT_MESSAGE_Y2025 ,4) */count(*) from pspadm.PSP_ENTITLEMENT_MESSAGE partition (ENTMT_MESSAGE_Y2025)ENTMT_MESSAGE_Y2025;
select /*+ FULL(ENTMT_MESSAGE_Y2023) parallel(ENTMT_MESSAGE_Y2023 ,4) */count(*) from pspadm.PSP_ENTITLEMENT_MESSAGE partition (ENTMT_MESSAGE_Y2023)ENTMT_MESSAGE_Y2023;
select /*+ FULL(ENTITY_UPDATE_M012023) parallel(ENTITY_UPDATE_M012023 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (ENTITY_UPDATE_M012023)ENTITY_UPDATE_M012023;
select /*+ FULL(SYS_P632) parallel(SYS_P632 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (SYS_P632)SYS_P632;
select /*+ FULL(SYS_P635) parallel(SYS_P635 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (SYS_P635)SYS_P635;
select /*+ FULL(ENTITY_UPDATE_M112022) parallel(ENTITY_UPDATE_M112022 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (ENTITY_UPDATE_M112022)ENTITY_UPDATE_M112022;
select /*+ FULL(ENTITY_UPDATE_M122022) parallel(ENTITY_UPDATE_M122022 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (ENTITY_UPDATE_M122022)ENTITY_UPDATE_M122022;
select /*+ FULL(ENTITY_UPDATE_M102022) parallel(ENTITY_UPDATE_M102022 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (ENTITY_UPDATE_M102022)ENTITY_UPDATE_M102022;
select /*+ FULL(SYS_P629) parallel(SYS_P629 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (SYS_P629)SYS_P629;
select /*+ FULL(SYS_P1105) parallel(SYS_P1105 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (SYS_P1105)SYS_P1105;
select /*+ FULL(SYS_P813) parallel(SYS_P813 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (SYS_P813)SYS_P813;
select /*+ FULL(SYS_P1369) parallel(SYS_P1369 ,4) */count(*) from pspadm.PSP_ENTITY_UPDATE partition (SYS_P1369)SYS_P1369;
select /*+ FULL(PC_USAGE_Y2024) parallel(PC_USAGE_Y2024 ,4) */count(*) from pspadm.PSP_PAYCHECK_USAGE partition (PC_USAGE_Y2024)PC_USAGE_Y2024;
select /*+ FULL(PC_USAGE_Y2025) parallel(PC_USAGE_Y2025 ,4) */count(*) from pspadm.PSP_PAYCHECK_USAGE partition (PC_USAGE_Y2025)PC_USAGE_Y2025;
select /*+ FULL(PC_USAGE_Y2023) parallel(PC_USAGE_Y2023 ,4) */count(*) from pspadm.PSP_PAYCHECK_USAGE partition (PC_USAGE_Y2023)PC_USAGE_Y2023;


spool off;
