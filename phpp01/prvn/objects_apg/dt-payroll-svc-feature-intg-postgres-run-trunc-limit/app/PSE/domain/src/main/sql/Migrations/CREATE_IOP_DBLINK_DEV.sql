--
-- This provides the connection to the dev PSP database.
--

CREATE DATABASE LINK "PSPMIGRATION.WORLD"
 CONNECT TO PSP_LOCAL
 IDENTIFIED BY psp_local
 USING 'PSPSKOM.WORLD';

-- ensure the following lines are added to the pse database tnsnames.ora
--
-- PSPSKOM.WORLD =
--   (DESCRIPTION =
--     (ADDRESS_LIST =
--       (ADDRESS = (PROTOCOL = TCP)(HOST = 172.17.218.59)(PORT = 1521))
--     )
--     (CONNECT_DATA =
--       (SID = pspskom)
--     )
--   )
