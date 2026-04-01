--
-- This provides the connection to the dev PSP database.
--

CREATE DATABASE LINK "PSPMIGRATION.WORLD"
 CONNECT TO PSPMIGADM
 IDENTIFIED BY pspmigadm
 USING 'PSPPRF1.WORLD';

-- ensure the following lines are added to the pse database tnsnames.ora
--
-- PSPPRF1.WORLD =                                                                       
--   (DESCRIPTION =                                                                      
--     (ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpsplt02-vip.corp.intuit.net)(PORT = 1521))
--     (ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpsplt01-vip.corp.intuit.net)(PORT = 1521))
--     (ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpsplt03-vip.corp.intuit.net)(PORT = 1521))
--     (LOAD_BALANCE = yes)                                                              
--     (CONNECT_DATA =                                                                   
--       (SERVER = DEDICATED)                                                            
--       (SERVICE_NAME = pspprf)                                                         
--         (FAILOVER_MODE =                                                                
--           (TYPE = SELECT)                                                               
--           (METHOD = BASIC)                                                              
--           (RETRIES = 180)                                                               
--           (DELAY = 5)                                                                   
--         )                                                                               
--       )                                                                                 
--     )                                                                                   