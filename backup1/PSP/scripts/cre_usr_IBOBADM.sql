                                                                                                                                    
   CREATE USER "IBOBADM" IDENTIFIED BY VALUES 'S:503F25593389640FD7C08FFD0F2767A                                                    
5A6EF29F361646E37FA602682AA0C;T:51DE5D2B023C74D3D0056E01619BE6FD8DB5A7058BF5A265                                                    
68FCD99185DE5FF8A775B7CEFA3CCB113C3A810575975F6C5EDF87BD44D059BDDA3DD449056C9113                                                    
3FB1FF21B1C37EA66684F049C07BD2C9'                                                                                                   
      DEFAULT TABLESPACE "PSP_DATA01"                                                                                               
      TEMPORARY TABLESPACE "TEMP"                                                                                                   
      PROFILE "APPLICATION_HIGH_RISK_PROFILE"                                                                                       
      ACCOUNT LOCK;                                                                                                                 
 ALTER USER "IBOBADM" LOCAL TEMPORARY TABLESPACE "SYSTEM";                                                                          
                                                                                                                                    
                                                                                                                                    
  GRANT DEBUG ANY PROCEDURE TO "IBOBADM";                                                                                           
  GRANT DEBUG CONNECT SESSION TO "IBOBADM";                                                                                         
  GRANT DROP ANY OUTLINE TO "IBOBADM";                                                                                              
  GRANT ALTER ANY OUTLINE TO "IBOBADM";                                                                                             
  GRANT CREATE ANY OUTLINE TO "IBOBADM";                                                                                            
  GRANT CREATE ANY CONTEXT TO "IBOBADM";                                                                                            
  GRANT CREATE MATERIALIZED VIEW TO "IBOBADM";                                                                                      
  GRANT CREATE ANY TRIGGER TO "IBOBADM";                                                                                            
  GRANT CREATE ROLE TO "IBOBADM";                                                                                                   
  GRANT CREATE DATABASE LINK TO "IBOBADM";                                                                                          
  GRANT DROP ANY SYNONYM TO "IBOBADM";                                                                                              
  GRANT CREATE ANY SYNONYM TO "IBOBADM";                                                                                            
  GRANT CREATE ANY INDEX TO "IBOBADM";                                                                                              
  GRANT SELECT ANY TABLE TO "IBOBADM";                                                                                              
  GRANT LOCK ANY TABLE TO "IBOBADM";                                                                                                
  GRANT DROP ANY TABLE TO "IBOBADM";                                                                                                
  GRANT ALTER ANY TABLE TO "IBOBADM";                                                                                               
  GRANT CREATE ANY TABLE TO "IBOBADM";                                                                                              
  GRANT UNLIMITED TABLESPACE TO "IBOBADM";                                                                                          
                                                                                                                                    
                                                                                                                                    
   GRANT "CONNECT" TO "IBOBADM";                                                                                                    
   GRANT "RESOURCE" TO "IBOBADM";                                                                                                   
   GRANT "SCHEDULER_ADMIN" TO "IBOBADM";                                                                                            
                                                                                                                                    
SP2-0042: unknown command "UNION ALL" - rest of line ignored.
                                                                                                                                    
  GRANT SELECT ON "SYS"."DBA_USERS" TO "IBOBADM";                                                                                   
  GRANT EXECUTE ON "SYS"."DBMS_LOCK" TO "IBOBADM";                                                                                  
                                                                                                                                    
