--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.004.007.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- Set the PIN hash type based on the length of the PIN
-- 28 characters = SHA, 11 or 12 characters = AS400
-- Any other found lengths should generate an error since ERROR is an invalid value for hash_type.
update psp_company_pin
    set hash_type =     
         decode(length(p_i_n_value), 28, 'SHA',
                                     11, 'AS400',
                                     12, 'AS400',
                                         'ERROR')
   where hash_type is null
/

commit;

-- Now that it is populated, make it not null.
alter table psp_company_pin modify (hash_type NOT NULL)
/
