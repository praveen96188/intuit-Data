set search_path=pspadm;
insert into pspadm.psp_entitlement_message_range select * from pspadm.psp_entitlement_message_p6;
insert into pspadm.psp_entitlement_message_range select * from pspadm.psp_entitlement_message_p7;

