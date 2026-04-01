set search_path to pspadm;
\timing
call mchoubey.PRC_FIX_PROPERTY_AUDIT('InitiationDate', timestamp '2023-10-13 00:00:00');
call mchoubey.PRC_FIX_PROPERTY_AUDIT('EffectiveDate', timestamp '2023-10-13 00:00:00');
call mchoubey.PRC_FIX_PROPERTY_AUDIT('StatusEffectiveDate', timestamp '2023-10-13 00:00:00');
call mchoubey.PRC_FIX_PROPERTY_AUDIT('HireDate', timestamp '2023-10-13 00:00:00');
call mchoubey.PRC_FIX_PROPERTY_AUDIT('TerminationDate', timestamp '2023-10-13 00:00:00');

