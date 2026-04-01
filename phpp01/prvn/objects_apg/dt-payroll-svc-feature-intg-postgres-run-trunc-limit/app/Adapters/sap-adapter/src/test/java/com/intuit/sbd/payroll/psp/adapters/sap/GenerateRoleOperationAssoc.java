package com.intuit.sbd.payroll.psp.adapters.sap;

import com.csvreader.CsvReader;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * User: dweinberg
 * Date: 9/12/11
 * Time: 10:31 AM
 */
public class GenerateRoleOperationAssoc {

    public static void main(String[] args) {
        String fileName = "D:\\temp\\Copy of PSP_ROLES_10-1_Working-3.csv";

        try {
            Application.initialize();
            ApplicationSecondary.initialize();
            PayrollServices.beginUnitOfWork();

            Map<String, String> overrides = getOverrides();

            CsvReader operations = new CsvReader(fileName);
            operations.readRecord(); //skip user count
            operations.readRecord(); //read roles

            int columns = operations.getColumnCount();

            Map<Integer, AuthRole> roleColumns = new HashMap<Integer, AuthRole>();
            Map<AuthRole, StringBuilder> sqlMap = new HashMap<AuthRole, StringBuilder>();
            Map<AuthRole, StringBuilder> subStatusSqlMap = new HashMap<AuthRole, StringBuilder>();

            for (int i=6; i < columns; i++) {
                String roleString = operations.get(i);
                if (StringUtils.isEmpty(roleString)) {
                    continue;
                }
                if (overrides.containsKey(roleString)) {
                    roleString = overrides.get(roleString);
                }

                //see if we can figure out what it is
                String roleStringStripped = roleString.replaceAll("\\s","");
                DomainEntitySet<AuthRole> authRoles = Application.find(AuthRole.class,
                        AuthRole.RoleId().equalTo(roleString)
                                .Or(AuthRole.Name().equalTo(roleString))
                                .Or(AuthRole.Description().equalTo(roleString))
                                .Or(AuthRole.RoleId().equalTo(roleStringStripped)));
                AuthRole authRole=null;
                if (authRoles.size() == 1) {
                    authRole = authRoles.getFirst();
                }
                if (authRole == null) {
                    System.out.println("Could not map role; skipping " + operations.get(i));
                    continue;
                }

                roleColumns.put(i, authRole);
            }

            //now each operation
            while (operations.readRecord()) {
                String operationId = operations.get(0);
                if (StringUtils.isEmpty(operationId)) {
                    continue;
                }
                OperationId operation;
                try {
                    operation = OperationId.valueOf(operationId);
                } catch (IllegalArgumentException iae) {
                    if(operationId != null && (operationId.equals("CanMoveFromSubStatus") || operationId.equals("CanMoveToSubStatus"))) {
                        ServiceSubStatusCode subStatus = null;
                        String subStatusStr = null;
                        SubStatusChangeType changeType = SubStatusChangeType.valueOf(operationId);
                        try{
                            subStatusStr = operations.get(4);
                            subStatus = ServiceSubStatusCode.valueOf(subStatusStr);
                        }catch (IllegalArgumentException ia) {
                            System.out.println("Could not map sub status; skipping " + subStatusStr);
                        }
                        for (Map.Entry<Integer, AuthRole> roleEntry : roleColumns.entrySet()) {
                            String roleOpText = operations.get(roleEntry.getKey());
                            if (isEnabled(roleOpText)) {
                                DomainEntitySet<RoleSubStatus> roleSubStatuses = Application.find(RoleSubStatus.class, RoleSubStatus.AllowedChangeType().equalTo(changeType)
                                                                                                        .And(RoleSubStatus.AuthRole().equalTo(roleEntry.getValue()))
                                                                                                        .And(RoleSubStatus.ServiceSubStatus().ServiceSubStatusCd().equalTo(subStatus)));
                                SpcfUniqueId roleSubStatusSeq = SpcfUniqueId.generateRandomUniqueId();
                                if(roleSubStatuses.size() > 0) {
                                    roleSubStatusSeq = roleSubStatuses.getFirst().getId();
                                }

                                String sqlLine = String.format("INSERT INTO TEMP_PSP_ROLE_SUB_STATUS (ROLE_SUB_STATUS_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, ALLOWED_CHANGE_TYPE, SERVICE_SUB_STATUS_FK, AUTH_ROLE_FK) \n" +
                                                                       "VALUES ('%s', 0, SYSDATE, SYSDATE, '%s', '%s', '%s')\n/\n",
                                                               roleSubStatusSeq.toString(), changeType.toString(),subStatus.toString(), roleEntry.getValue().getId().toString() );
                                if (!subStatusSqlMap.containsKey(roleEntry.getValue())) {
                                    subStatusSqlMap.put(roleEntry.getValue(), new StringBuilder());
                                }
                                subStatusSqlMap.get(roleEntry.getValue()).append(sqlLine);
                            }
                        }
                    } else {
                        System.out.println("Could not map operation; skipping " + operationId);
                    }
                    continue;
                }

                for (Map.Entry<Integer, AuthRole> roleEntry : roleColumns.entrySet()) {
                    String roleOpText = operations.get(roleEntry.getKey());
                    if (isEnabled(roleOpText)) {
                        String sqlLine = String.format("INSERT INTO TEMP_PSP_ROLE_OPERATION_ASSOC (AUTH_ROLE_FK, AUTH_OPERATION_FK, REALM_ID) VALUES ('%s', '%s', -1)\n/\n",
                                roleEntry.getValue().getId().toString(),
                                operation.toString());
                        if (!sqlMap.containsKey(roleEntry.getValue())) {
                            sqlMap.put(roleEntry.getValue(), new StringBuilder());
                        }
                        sqlMap.get(roleEntry.getValue()).append(sqlLine);
                    }
                }


            }

            System.out.println("\n\nOutput for - populate_role_operation_assoc.sql -\n");
            List<AuthRole> roles = new ArrayList<AuthRole>(roleColumns.values());
            Collections.sort(roles, new RoleComparator());
            for (AuthRole authRole : roles) {
                if (!sqlMap.containsKey(authRole)) {
                    continue;
                }
                System.out.println("--" + authRole.getRoleId() + " (" + authRole.getName() + ")");
                System.out.println(sqlMap.get(authRole).toString());
                System.out.println("");
            }

            System.out.println("\n\nOutput for - populate_role_substatus.sql -\n\n");

            for (AuthRole authRole : roles) {
                if (!subStatusSqlMap.containsKey(authRole)) {
                    continue;
                }
                System.out.println("--" + authRole.getRoleId() + " (" + authRole.getName() + ")");
                System.out.println(subStatusSqlMap.get(authRole).toString());
                System.out.println("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public static class RoleComparator implements Comparator<AuthRole> {

        public int compare(AuthRole o1, AuthRole o2) {
            if (o1.getName().equals("Admin")) {
                return -1;
            }
            if (o2.getName().equals("Admin")) {
                return 1;
            }
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static Map<String, String> getOverrides() {
        Map<String, String> overrides = new HashMap<String, String>();
        //overrides.put("Activations SME", ""); //hidden role
        overrides.put("SD - Full Service Supervior/Lead (PS&S Supervisor)", "SD - Full Service Supervisor/Lead (PS&S Supervisor)"); //spelling error
        overrides.put("Activations SME", ""); // is removed and combined with DesktopCareLead
        overrides.put("PSS Activations Supervisor", ""); // is removed and combined with DesktopCareManager
        overrides.put("PSS Supervisor", ""); // is removed and combined with DesktopCareManager
        overrides.put("PS&S Outbound", ""); // is removed and combined with DesktopCareLead


        overrides.put("Activations QA", ""); // Activations QA is removed and using Activations SME
        overrides.put("PPS (Agent)", "PPS Agent");
        overrides.put("Desktop Care Agent (was PSSRep)", "DesktopCareAgent");
        overrides.put("Desktop Care Manager (was PSSSupervisor and PSSACtivationsSupervisor)", "DesktopCareManager");
        overrides.put("Desktop Care Lead (was ActivationsSme and PSSOutbound)", "DesktopCareLead");

        overrides.put("PPSCancellation (incl ops from PPS)", "PPSCancellations");
        overrides.put("CCTAgent (incl ops from PPSEntity)", "CCTAgent");
        overrides.put("PPSAccountMaintenance (incl ops from PrintQueueAgent, Check DistributionAgent, AssistedDataSteward, WOTCTaxREp)", "PPSAccountMaintenance");
        return overrides;
    }

    public static boolean isEnabled(String roleOpText) {
        //some are 'x' and some are '1'
        return StringUtils.isNotEmpty(roleOpText);
    }

}
