CREATE OR REPLACE PACKAGE PK_IOPDDTOPSP_CONST AS
/******************************************************************************
   NAME:    PK_CONST
   UPDATED: 10.10.2008  02:00 PM  
   PURPOSE:	global constants

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.25.2008  EMR			   Created migration state engine
   1.1        06.19.2008  EMR              Added support for events and 
                                             employees.
   1.2        07.01.2008  EMR              aligned psp enums
   2.0        07.08.2008  EMR              adapted framework for IOP
   2.0.1      08.29.2008  EMR              added pse settlement type check
   2.0.2      09.17.2008  EMR              added pse hold code   
   2.0.3      09.17.2008  EMR              added pse suspend and pend term codes   
   2.0.4      09.29.2008  EMR              fixed global event compare for new 
                                             types.
   2.0.4      10.10.2008  EMR              added constant for bogus company 8350                                                
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- Migration State Engine
  -- ------------------------------------------------------------------------
  gc_Idle_StateCD             CONSTANT VARCHAR2(10) := 'IDL';  -- not ready to migrate
  gc_Ready_StateCD            CONSTANT VARCHAR2(10) := 'RDY';  -- ready to migrate
  gc_Migrating_StateCD        CONSTANT VARCHAR2(10) := 'MIG';  -- moving company
  gc_Validating_StateCD       CONSTANT VARCHAR2(10) := 'VAL';  -- validating what moved
  gc_Syncing_StateCD          CONSTANT VARCHAR2(10) := 'SYNC';  -- one last sync w as400  
  gc_Complete_StateCD         CONSTANT VARCHAR2(10) := 'CP' ;  -- successful migration
  gc_Error_StateCD            CONSTANT VARCHAR2(10) := 'ERR';  -- unsuccessful migration


  -- ------------------------------------------------------------------------
  -- Other Control Constants
  -- ------------------------------------------------------------------------
  
  gc_DD_SERVICE_IOP           CONSTANT VARCHAR2(10) := 'QBOE';
  gc_DD_SERVICE_DIY           CONSTANT VARCHAR2(10) := 'DIY';
  gc_TRUE                     CONSTANT VARCHAR2(10) := 'TRUE';
  gc_FALSE                    CONSTANT VARCHAR2(10) := 'FALSE';
  
  gc_GMT_timezone             CONSTANT VARCHAR2(10) := 'GMT';
  gc_PST_timezone             CONSTANT VARCHAR2(10) := 'PST';  -- standard time
  gc_PDT_timezone             CONSTANT VARCHAR2(10) := 'PDT';  -- daylight savings time
  
  gc_Bogus_Company_8350       CONSTANT NUMBER       := 8350;   -- from July 08 production issue  


  -- ------------------------------------------------------------------------
  -- Mapping Constants
  -- ------------------------------------------------------------------------
  
  gc_PSE_Status_Hold          CONSTANT VARCHAR2(10) := 'HOLD';
  gc_PSE_Status_Suspend       CONSTANT VARCHAR2(10) := 'SSPND';
  gc_PSE_Status_PendgTerm     CONSTANT VARCHAR2(10) := 'PNDTERMN';  
  
  gc_PSE_Settlement_Check     CONSTANT VARCHAR2(10) := 'CHECK';
  gc_PSP_Settlement_Check     CONSTANT VARCHAR2(10) := 'CheckType';
  
  gc_PSP_Event_ManualNote     CONSTANT VARCHAR2(100) := 'ManualNoteEvent';
  gc_PSP_Event_FirstPayroll   CONSTANT VARCHAR2(100) := 'FirstPayrollReceived';

  
END PK_IOPDDTOPSP_CONST; 
/

