--
-- This provides the connection to the dev PSP database.
--

CREATE DATABASE LINK "PSPMIGRATION.WORLD"
 CONNECT TO PSPAPP
 IDENTIFIED BY pspapp
 USING 'VPSPQA05.WORLD';

-- ensure the following lines are added to the pse database tnsnames.ora
--
-- VPSPQA05.WORLD =                                                                  
--   (DESCRIPTION =                                                                  
--     (ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpspqa05.corp.intuit.net)(PORT = 1521))
--     (CONNECT_DATA =                                                               
--       (SERVER = DEDICATED)                                                        
--       (SERVICE_NAME = pspqa01)                                                    
--     )                                                                             
--   )                                                                               
--   )
