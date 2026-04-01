package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.ErrorMessageList;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessage;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.VoidPayroll401k;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;
import java.math.BigDecimal;

/**
 * User: rnorian
 * Date: Jan 26, 2010
 * Time: 12:31:10 PM
 */
public class QBDTWSSubmitPayrollRequestProcess {
    private static final SpcfLogger logger = PayrollServices.getLogger(QBDTWSSubmitPayrollRequestProcess.class);
    private static final BigDecimal NegativeOne = new BigDecimal(-1);

    private String transmissionId;
    private SubmitPayrollRequest submitPayrollRequest;

    private Company company;

    private boolean companyIsOn401KService;
    private String companyCustodialId;
    private boolean companyIsTax401K;

    // not used for v1
    private boolean isSafeHarbor;

    //TODO_RN: replace adapterValidationResult; right now necessary since Core adds some and Adapter adds some and to avoid double add adapter tracks its own errors to post as events later (future: remove from core and leave adapter as entirely responsible?)
    private ProcessResult adapterValidationResult = new ProcessResult();

    // in processing loops, set to the paycheck and associated employee currently being processed
    private QBPaycheck currentPaycheck;
    private Employee currentEmployee;

    private HashMap<String, QBPayrollItem> qbPayrollItemMap = new HashMap<String, QBPayrollItem>();
    private HashMap<String, CompanyPayrollItemDTO> companyPayrollItemDTOMap = new HashMap<String, CompanyPayrollItemDTO>();

    private HashMap<String, QBEmployee> qbEmployeeMap = new HashMap<String, QBEmployee>();

    public static final String TAX_TRACKING_TYPE_401K = "11";
    public static final String TAX_TRACKING_TYPE_ROTH = "57";

    public QBDTWSSubmitPayrollRequestProcess(String pTransmissionId, Company pCompany, SubmitPayrollRequest pSubmitPayrollRequest) {
        transmissionId = pTransmissionId;
        company = pCompany;
        submitPayrollRequest = pSubmitPayrollRequest;

        companyIsOn401KService = company.isCompanyOnService(ServiceCode.ThirdParty401k);
        companyIsTax401K = company.isCompanyOnService(ServiceCode.Tax) && companyIsOn401KService;
    }

    private boolean requestHasPaychecks() {
        QBPaychecks paycheckList = submitPayrollRequest.getPaycheckList();
        return (paycheckList != null && paycheckList.getPaycheck() != null && paycheckList.getPaycheck().size() > 0);
    }

    public ProcessResult process() {
        StopWatch timer = new StopWatch().start();
        ProcessResult processResult = new ProcessResult();

        if (companyIsOn401KService) {
            ThirdParty401kCompanyServiceInfo tp401kServiceInfo = (ThirdParty401kCompanyServiceInfo) company.getService(ServiceCode.ThirdParty401k);
            companyCustodialId = tp401kServiceInfo.getCustodialId();
            isSafeHarbor = tp401kServiceInfo.getHasSafeHarbor();
        }

        ProcessResult payrollItemsResult = createPayrollItemMaps(submitPayrollRequest.getPayrollItemList());
        adapterValidationResult.merge(payrollItemsResult);
        processResult.merge(payrollItemsResult);
        if (!processResult.isSuccess()) {
            return processResult;
        }

        createEmployeeMap(submitPayrollRequest.getSubmitEmployeesRequest());

        HashMap<String, QBPaycheck> paychecksMap = new HashMap<String, QBPaycheck>();
        List<QBPaycheck> paychecksToUpdate = new ArrayList<QBPaycheck>();
        List<QBPaycheck> paychecksToAdd = new ArrayList<QBPaycheck>();
        List<QBPaycheck> paychecksToVoid = new ArrayList<QBPaycheck>();
        List<QBPaycheck> paychecksToDelete = new ArrayList<QBPaycheck>();

        QBPaychecks paycheckList = submitPayrollRequest.getPaycheckList();
        if (paycheckList != null) {

            // perform caching for performance
            // -- all cloud employees (used in prepareNewPaychecks, preparePaycheckUpdates
            DomainEntitySet<Employee> cloudEmployees = company.getCloudEmployees();
            // -- most likely set of paychecks (used in looking for existing paychecks w/same ID), look back of 15 days
            //    to catch most weekly and bi-weekly employees
            SpcfCalendar fromDate = PSPDate.getPSPTime();
            fromDate.addDays(-15);
            DomainEntitySet<Paycheck> candidateMatches = Paycheck.findCompanyPaychecksFrom(company, fromDate);

            //todo_rhn: verify deleted scenarios can occur and if so, filter out paychecks
            // filter paychecks that reference non-existent employees
            // scenario:
            //   1. customer creates employee in QB
            //   2. customer creates paycheck for EE in QB
            //   3. customer hard deletes EE from QB
            //   4. customer sends cloud - paycheck included, referenced EE does not have info necessary to create
            //ArrayList<QBPaycheck> filteredPaychecks = filterUnprocessablePaychecks(paycheckList.getPaycheck());
            //createFilteredQBPaycheckEvents(filteredPaychecks);            
            if (companyIsTax401K) {
                // filter paychecks; exclude paychecks w/paydate < 1/1/2011
                ArrayList<QBPaycheck> filteredQbPaychecks = filterQBPaychecks(submitPayrollRequest.getPaycheckList().getPaycheck(), 20110000);

                if (!filteredQbPaychecks.isEmpty()) {
                    ProcessResult invalidCheckDateCompanyPR = createFilteredQbPaychecksEvents(filteredQbPaychecks);
                    adapterValidationResult.merge(invalidCheckDateCompanyPR);
                    processResult.merge(invalidCheckDateCompanyPR);
                }
            }

            for (QBPaycheck qbPaycheck : submitPayrollRequest.getPaycheckList().getPaycheck()) {
                ProcessResult dupePaycheckId = new ProcessResult();
                if (paychecksMap.containsKey(qbPaycheck.getPaycheckID())) {
                    dupePaycheckId.getMessages().DuplicatePaycheckId(EntityName.Paycheck,
                                                                    qbPaycheck.getPaycheckID(),
                                                                    qbPaycheck.getPaycheckID(),
                                                                    company.getSourceSystemCd().name(),
                                                                    company.getSourceCompanyId());
                    adapterValidationResult.merge(dupePaycheckId);
                    processResult.merge(dupePaycheckId);
                    return processResult;
                }
                paychecksMap.put(qbPaycheck.getPaycheckID(), qbPaycheck);

                HashMap<String, Paycheck> paychecksByType = findMatchingPaychecks(qbPaycheck);
                Paycheck ofxPaycheck = paychecksByType.get("OFX");
                Paycheck diyPaycheck = paychecksByType.get("DIY");
                
                // scenario: manual (non-DD) paycheck converted to dd paycheck by user
                // (PSP should only retain dd paycheck and should augment the dd info w/the cloud info)
                if (diyPaycheck != null && ofxPaycheck != null) {
                    PayrollServices.payrollManager.deletePaycheck(company.getSourceSystemCd(),
                                                                  company.getSourceCompanyId(),
                                                                  diyPaycheck.getSourcePaycheckId(),
                                                                  transmissionId);
                    diyPaycheck = null;
                }

                // find the update target
                if (ofxPaycheck != null) {
                    qbPaycheck.setPspPaycheckId(ofxPaycheck.getSourcePaycheckId());
                } else if (diyPaycheck != null) {
                    qbPaycheck.setPspPaycheckId(diyPaycheck.getSourcePaycheckId());
                    if (qbPaycheck.isOFXPaycheck()) {
                        logger.warn("QBPaycheck received with OFX ID but no paycheck exists (QBDT sending QBDTWS w/out successful OFX send?): " + qbPaycheck);
                    }
                }

                // add/mod/void -- add or update the paycheck in PSP.  for voids, after add/update, then void paycheck
                // delete -- QBDT sends very limited info for paychecks w/DELETE operation; therefore, do not perform
                //           any update/add and just soft delete paycheck
                boolean paycheckExistsInPSP = ofxPaycheck != null || diyPaycheck != null;
                Paycheck pspPaycheck = ofxPaycheck != null ? ofxPaycheck : diyPaycheck;

                if (shouldAddPaycheck(paycheckExistsInPSP, qbPaycheck)) {
                    paychecksToAdd.add(qbPaycheck);
                    // cache a flag indicating this is a new paycheck to avoid having core spawn threads that do
                    // company event management for existing paychecks
                    Application.getSessionCache().addNonHibernateObject(company.getSourceCompanyId() + ":" + qbPaycheck.getPspPaycheckId(), new Boolean(true));                    
                } else if (shouldUpdatePaycheck(qbPaycheck)) {
                    paychecksToUpdate.add(qbPaycheck);
                }

                switch (qbPaycheck.getOperation()) {
                    case VOID:
                        if (paycheckExistsInPSP) {
                            paychecksToVoid.add(qbPaycheck);
                        }
                        break;
                    case DELETE:
                        if (paycheckExistsInPSP && pspPaycheck.getStatus() == PaycheckStatusCode.Active)
                            paychecksToDelete.add(qbPaycheck);
                        break;
                    default:
                }
            }
        }

        ProcessResult<Collection<PayrollRunDTO>> newPaychecksResult = prepareNewPaychecks(paychecksToAdd);
        processResult.merge(newPaychecksResult);

        ProcessResult<Map<PayrollRun, ArrayList<PaycheckDTO>>> existingPaychecksResult = preparePaycheckUpdates(paychecksToUpdate);
        processResult.merge(existingPaychecksResult);

        if (!processResult.isSuccess())
            return processResult;

        // call core to persist data and execute business logic
        if (companyIsTax401K) {
            // for companies on Assisted, only update a CompanyPayrollItems whose PayrollItemCode has been set to
            // a 401k specific value  i.e. they are assigned a PayrollItemCode starting with Tp401kXxx
            filterTo401KPayrollItems(companyPayrollItemDTOMap); 
        }

        processResult.merge(submitCompanyPayrollItems(companyPayrollItemDTOMap.values()));
        processResult.merge(submitQBDTPaycheckInfoUpdates(existingPaychecksResult.getResult()));

        // for Assisted companies, only perform validation to generate 401k specific error events
        // (paycheck data handling performed by QBDT adapter)
        Application.setProcessValidatesOnly(companyIsTax401K);

        processResult.merge(submitNewPaychecks(newPaychecksResult.getResult()));
        processResult.merge(submitExistingPaychecks(existingPaychecksResult.getResult()));
        processResult.merge(voidPaychecks(paychecksToVoid));
        processResult.merge(deletePaychecks(paychecksToDelete));

        if (processResult.isSuccess() && companyIsTax401K && requestHasPaychecks()) {
            Application.setProcessValidatesOnly(false);
            // advance the company status - this would ordinarily happen is PayrollSubmit401k but that is 'turned off' above
            if (company.getCompanyService(ServiceCode.ThirdParty401k).getStatusCd() == ServiceSubStatusCode.PendingFirstPayroll) {
                CompanyService tp401kInfo = company.getCompanyService(ServiceCode.ThirdParty401k);
                ServiceSubStatusCode nextServiceSubStatusCd = tp401kInfo.getNextValidServiceStatus(ServiceSubStatusCode.PendingFirstPayroll);
                tp401kInfo.updateCompanyServiceStatus(nextServiceSubStatusCd);
            }
            if (company.getCompanyService(ServiceCode.Cloud).getStatusCd() == ServiceSubStatusCode.PendingFirstPayroll) {
                CompanyService cloudService = company.getCompanyService(ServiceCode.ThirdParty401k);
                ServiceSubStatusCode nextServiceSubStatusCd = cloudService.getNextValidServiceStatus(ServiceSubStatusCode.PendingFirstPayroll);
                cloudService.updateCompanyServiceStatus(nextServiceSubStatusCd);
            }
        }

        logger.info("finished processing SubmitPayrollRequest for PSID: " + submitPayrollRequest.getPSID() + " (success = " + processResult.isSuccess() + ") in " + timer.stop().getElapsedTimeString());
        return processResult;
    }

    /**
     * Search for paychecks on both the Paycheck.SourcePaycheckId and QbdtPaycheckInfo.ListId
     * @param qbPaycheck
     * @return
     */
    private HashMap<String, Paycheck> findMatchingPaychecks(QBPaycheck qbPaycheck) {
        DomainEntitySet<Paycheck> paychecks = new DomainEntitySet<Paycheck>();

        Paycheck diyPaycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
        if (diyPaycheck != null && !paychecks.contains(diyPaycheck)) {
            paychecks.add(diyPaycheck);
        }

        if (qbPaycheck.isOFXPaycheck()) {
            Paycheck ofxPaycheck = Paycheck.findPaycheck(company, qbPaycheck.getOfxPaycheckID());
            if (ofxPaycheck != null && !paychecks.contains(ofxPaycheck)) {
                paychecks.add(ofxPaycheck);
            }
        }

        Expression<Paycheck> query = new Query<Paycheck>().Where(
                Paycheck.PayrollRun().Company().equalTo(company)
                .And(Paycheck.Status().in(PaycheckStatusCode.Active, PaycheckStatusCode.Inactive))
                .And(Paycheck.QbdtPaycheckInfo().ListId().equalTo(qbPaycheck.getPaycheckID())))
                .OrderBy(Paycheck.CreatedDate());
        for (Paycheck paycheck : Application.<Paycheck>find(Paycheck.class, query)) {
            if (!paychecks.contains(paycheck)) {
                paychecks.add(paycheck);
            }
        }

        if (paychecks.size() > 2) {
            logger.warn("searching for target of QBPaycheck: " + qbPaycheck + " - found: " + paychecks);
        }

        HashMap<String,Paycheck> paychecksByType = new HashMap<String, Paycheck>(2);
        for (Paycheck paycheck : paychecks) {
            // cache the paycheck
            NaturalKey naturalKey = paycheck.getNaturalKey();
            if (Application.getSessionCache().getPrimaryKey(naturalKey) == null) {
                Application.getSessionCache().addPrimaryKey(paycheck.getNaturalKey(), paycheck.getId());
            }

            // classify
            if (paycheck.isDIYPaycheck()) {
                paychecksByType.put("DIY", paycheck);
            } else {
                paychecksByType.put("OFX", paycheck);
            }
        }

        return paychecksByType;
    }

//    private ArrayList<QBPaycheck> filterUnprocessablePaychecks(List<QBPaycheck> pQBPaychecks) {
//        ArrayList<QBPaycheck> filteredQBPaychecks = new ArrayList<QBPaycheck>();
//
//        for (Iterator<QBPaycheck> iterator = pQBPaychecks.iterator(); iterator.hasNext();) {
//            QBPaycheck qbPaycheck = iterator.next();
//
//            if (findEmployee(qbPaycheck) == null) {
//                filteredQBPaychecks.add(qbPaycheck);
//                iterator.remove();
//                continue;
//            }
//
//            ProcessResult<Boolean> allItemsExistPR = allPayrollItemsExist(qbPaycheck.getEarningItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//            allItemsExistPR = allPayrollItemsExist(qbPaycheck.getAdjNetPayItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//            allItemsExistPR = allPayrollItemsExist(qbPaycheck.getPreTaxItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//            allItemsExistPR = allPayrollItemsExist(qbPaycheck.getTaxItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//            allItemsExistPR = allPayrollItemsExist(qbPaycheck.getNonTaxCompanyItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//            allItemsExistPR = allPayrollItemsExist(qbPaycheck.getTaxCompanyItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//            allItemsExistPR = allPayrollItemsExist(qbPaycheck.getDDItems());
//            if (!allItemsExistPR.getResult()) {
//                // merge the result messages
//                continue;
//            }
//
//        }
//
//        return filteredQBPaychecks;
//    }

    private ProcessResult<Boolean> allPayrollItemsExist(List<QBPaycheckLineItem> pLineItems) {
        ProcessResult<Boolean> result = new ProcessResult<Boolean>();
        result.setResult(true);

        for (QBPaycheckLineItem qbPaycheckLineItem : pLineItems) {
            if (qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId()) == null) {
                result.setResult(false);
                break;
            }
        }

        return result;
    }

    private ProcessResult createEmployeeMap(SubmitEmployeesRequest submitEmployeesRequest) {
        if (submitEmployeesRequest != null && submitEmployeesRequest.getEmployees() != null) {
            for (QBEmployee qbEmployee : submitEmployeesRequest.getEmployees().getEmployee()) {
                qbEmployeeMap.put(qbEmployee.getSourceEmployeeId(), qbEmployee);
                if (qbEmployee.isOFXEmployee()) {
                    qbEmployeeMap.put(qbEmployee.getOfxEmployeeId(), qbEmployee);
                }
            }
        }
        return new ProcessResult();
    }

    private ProcessResult createFilteredQbPaychecksEvents(ArrayList<QBPaycheck> pFilteredQbPaychecks) {
        ProcessResult processResult = new ProcessResult();

        processResult.getMessages().QBDT401kAssistedInvalidCheckDateCompanyMsg(EntityName.Paycheck, null);

        ProcessResult invalidCheckDatePR;
        for (QBPaycheck qbPaycheck : pFilteredQbPaychecks) {

            //Only create customer warning messages for pay check adds
            if (!qbPaycheck.getOperation().equals(QBPaycheckOperationEnum.ADD)) {
                processResult.getMessages().clear();
                continue;
            }

            invalidCheckDatePR = new ProcessResult();
            invalidCheckDatePR.getMessages().QBDT401kAssistedInvalidCheckDateCheckMsg(EntityName.Paycheck, qbPaycheck.getPaycheckID());

            SpcfCalendar checkDate = SpcfCalendar.createInstance(qbPaycheck.getPayDate().getYear(),
                    qbPaycheck.getPayDate().getMonth(), qbPaycheck.getPayDate().getDay());

            String empFullName = "Unknown";
            String sourceEmpId = qbPaycheck.getEmployeeID();

            QBEmployee qbEmployee = findQBEmployee(sourceEmpId);
            if (qbEmployee != null) {
                empFullName = qbEmployee.getFullName();
            } else {
                Employee employee = Employee.findEmployee(company, sourceEmpId);
                if (employee != null) {
                    empFullName = employee.getFullName();    
                }
            }

            CompanyEvent.createInvalidPaycheckInformationEvents(company,
                                                                sourceEmpId,
                                                                empFullName,                                                                
                                                                qbPaycheck.getPaycheckID(),
                                                                checkDate,
                                                                qbPaycheck.getNetPay().toString(),
                                                                transmissionId,
                                                                invalidCheckDatePR);
        }

        return processResult;
    }

    private String findPSPSourceEmployeeId(QBPaycheck pQBPaycheck) {
        if (pQBPaycheck == null) return null;
        return findPSPSourceEmployeeId(pQBPaycheck.getEmployeeID());
    }

    private String findPSPSourceEmployeeId(String pRequestSourceEmployeId) {
        String pspSourceEmployeeId = null;

        Employee ee = findEmployee(pRequestSourceEmployeId);
        if (ee != null) {
            pspSourceEmployeeId = ee.getSourceEmployeeId();
        }

        return pspSourceEmployeeId;
    }

    private QBEmployee findQBEmployee(QBPaycheck pQBPaycheck) {
        if (pQBPaycheck == null) return null;
        return findQBEmployee(pQBPaycheck.getEmployeeID());
    }

    private QBEmployee findQBEmployee(String pSourceEmployeeId) {
        if (pSourceEmployeeId == null)
            return null;

        return qbEmployeeMap.get(pSourceEmployeeId);
    }

    private Employee findEmployee(QBPaycheck pQBPaycheck) {
        if (pQBPaycheck == null) return null;
        return findEmployee(pQBPaycheck.getEmployeeID());
    }

    private Employee findEmployee(String pSourceEmployeeId) {
        Employee employee = null;

        QBEmployee qbEmployee = findQBEmployee(pSourceEmployeeId);
        if (qbEmployee != null) {
            employee = Employee.findEmployee(company, qbEmployee.getPspEmployeeId());
        }

        // paycheck for an already deleted employee?
        if (employee == null) {
            employee = Employee.findEmployee(company, pSourceEmployeeId);
        }

        return employee;
    }

    private ArrayList<QBPaycheck> filterQBPaychecks(List<QBPaycheck> pQBPaychecks, int pFilterDate) {
        ArrayList<QBPaycheck> filteredQBPaychecks = new ArrayList<QBPaycheck>();

        for (Iterator<QBPaycheck> iterator = pQBPaychecks.iterator(); iterator.hasNext();) {
            QBPaycheck qbPaycheck = iterator.next();

            QBDate payDate = qbPaycheck.getPayDate();
            if (payDate == null || (payDate.getYear() * 10000 + payDate.getMonth() * 100 + payDate.getDay() < pFilterDate)) {
                filteredQBPaychecks.add(qbPaycheck);
                iterator.remove();
            }
        }

        return filteredQBPaychecks;
    }

    private void filterTo401KPayrollItems(HashMap<String,CompanyPayrollItemDTO> pPayrollItemMap) {
        for (Iterator<CompanyPayrollItemDTO> iterator = pPayrollItemMap.values().iterator(); iterator.hasNext();) {
            CompanyPayrollItemDTO companyPayrollItemDTO = iterator.next();
            if (companyPayrollItemDTO.getPayrollItemCode() == null) {
                iterator.remove();
                continue;
            }

            switch (companyPayrollItemDTO.getPayrollItemCode()) {
                case Tp401kLoanPayment:
                case Tp401kEmployeeDeferral:
                case Tp401kProfitSharing:
                case Tp401kRoth:
                case Tp401kEmployerMatch:
                case Tp401kSafeHarbor:
                    break;
                default:
                    iterator.remove();
                    break;
            }
        }
    }

    private boolean shouldAddPaycheck(boolean pPaycheckExistsInPSP, QBPaycheck pQBPaycheck) {
        boolean bIsVoid = pQBPaycheck.getOperation() == QBPaycheckOperationEnum.VOID;
        boolean bIsDelete = pQBPaycheck.getOperation() == QBPaycheckOperationEnum.DELETE;
        if (!pPaycheckExistsInPSP && !bIsVoid && !bIsDelete) {
            return true;
        } else {
            return false;
        }
    }

    private boolean shouldUpdatePaycheck(QBPaycheck pQBPaycheck) {
        boolean bIsVoid = pQBPaycheck.getOperation() == QBPaycheckOperationEnum.VOID;
        boolean bIsDelete = pQBPaycheck.getOperation() == QBPaycheckOperationEnum.DELETE;
        if (!bIsVoid && !bIsDelete) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * processing payroll items
     * - create new PayrollItems where they don't already exist
     * - create map of {id,payrollitem}
     *
     * @param pPayrollItems
     * @return 'true' inside of the process result if a 401K payroll item is detected, false otherwise
     */
    private ProcessResult createPayrollItemMaps(QBPayrollItems pPayrollItems) {
        ProcessResult processResult = new ProcessResult();
        boolean found401KPayrollItem = false;

        if (pPayrollItems == null)
            pPayrollItems = new QBPayrollItems();

        DTOFactory dtoFactory = new DTOFactory();
        for (QBPayrollItem qbPayrollItem : pPayrollItems.getPayrollItem()) {
            if (qbPayrollItem.getID() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayrollItem, "", "ID");
                return processResult;
            }
            if (qbPayrollItem.getName() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayrollItem, "", "Name");
                return processResult;
            }
            if (qbPayrollItem.getDetailTypeId() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayrollItem, qbPayrollItem.getID(), "DetailTypeId");
                return processResult;
            }
            if (qbPayrollItem.getTaxTrackingTypeId() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayrollItem, qbPayrollItem.getID(), "TaxTrackingTypeId");
                return processResult;
            }
            if (qbPayrollItem.getPayrollItemCategory() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayrollItem, qbPayrollItem.getID(), "PayrollItemCategory");
                return processResult;
            }
            if (is401KPayrollItem(qbPayrollItem)) {
                found401KPayrollItem = true;
            }

            // map creates association between 'native' payroll item ID to payroll item.  the native id is internal ID in QB that exists for DIY, DD, Assisted, etc.
            // map creates 2nd association for 'OFX paycheck id' when present (i.e. for Assisted + 401k processing)
            qbPayrollItemMap.put(qbPayrollItem.getID(), qbPayrollItem);
            if (qbPayrollItem.isOFXPayrollItem()) {
                qbPayrollItemMap.put(qbPayrollItem.getOfxPayrollId(), qbPayrollItem);
            }

            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForPayrollItemIds(company, qbPayrollItem.getPspPayrollItemId(), qbPayrollItem.getID());
            CompanyPayrollItemDTO companyPayrollItemDTO = null;
            if (companyPayrollItem != null) {
                companyPayrollItemDTO = dtoFactory.create(companyPayrollItem);
            } else {
                companyPayrollItemDTO = QBPayrollTranslator.createCoreDTO(qbPayrollItem);
            }

            // map must use 'PSP payroll item id' b/c this should be the OFX id for Assisted companies, the native
            // item ID for DIY, DD companies
            companyPayrollItemDTOMap.put(qbPayrollItem.getPspPayrollItemId(), companyPayrollItemDTO);
        }

        if (!found401KPayrollItem && shouldExecute401KValidations(company, PSPDate.getPSPTime()) && requestHasPaychecks()) {
            processResult.getMessages().No401KPayrollItems(EntityName.SourceSystemTransmission, transmissionId);
        }

        return processResult;
    }

    /**
     * If the paycheck doesn't exist in PSP it will not be attached to an existing payroll run
     * (PayrollRun is not a concept in QBDT and so there is no source payroll run identifier passed to PSP)
     * a new payroll run is created for all paychecks on a given paycheck date and then submitted
     */
    private ProcessResult<Collection<PayrollRunDTO>> prepareNewPaychecks(List<QBPaycheck> pPaychecks) {
        ProcessResult<Collection<PayrollRunDTO>> processResult = new ProcessResult<Collection<PayrollRunDTO>>();

        HashMap<QBDate, PayrollRunDTO> payDatePayrollMap = new HashMap<QBDate, PayrollRunDTO>();

        for (QBPaycheck qbPaycheck : pPaychecks) {
            currentPaycheck = qbPaycheck;
            currentEmployee = findEmployee(qbPaycheck);
            if (currentEmployee == null) {
                processResult.getMessages().EmployeeDoesNotExist(EntityName.Employee,
                                                                 qbPaycheck.getEmployeeID(),
                                                                 company.getSourceSystemCd().name(),
                                                                 company.getSourceCompanyId(),
                                                                 qbPaycheck.getEmployeeID());
                return processResult;
            }

            ProcessResult<PaycheckDTO> paycheckValidationResult = QBPayrollTranslator.createCoreDTO(qbPaycheck, findPSPSourceEmployeeId(qbPaycheck));
            if (!paycheckValidationResult.isSuccess()) {
                processResult.merge(paycheckValidationResult);
                return processResult;
            }
            PaycheckDTO paycheckDTO = paycheckValidationResult.getResult();

            PaycheckProcessor paycheckProcessor = new PaycheckProcessor(paycheckDTO, qbPaycheck);
            ProcessResult paycheckResult = paycheckProcessor.process();
            processResult.merge(paycheckResult);
            if (!processResult.isSuccess())
                return processResult;

            PayrollRunDTO payrollRunDTO = payDatePayrollMap.get(qbPaycheck.getPayDate());
            if (payrollRunDTO == null) {
                payrollRunDTO = QBPayrollTranslator.createCorePayrollDTO(null, qbPaycheck);
                payDatePayrollMap.put(qbPaycheck.getPayDate(), payrollRunDTO);
            }
            payrollRunDTO.getPaychecks().add(paycheckDTO);
        }

        processResult.setResult(payDatePayrollMap.values());
        return processResult;
    }


    private boolean is401KPaycheckLineItem(QBPaycheckLineItem pPaycheckLineItem) {
        if (pPaycheckLineItem == null)
            return false;

        if (pPaycheckLineItem.getPayrollItemId() == null)
            return false;

        QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(pPaycheckLineItem.getPayrollItemId());
        return is401KPayrollItem(qbPayrollItem);
    }

    private boolean is401KPayrollItem(QBPayrollItem pPayrollItem) {
        if (pPayrollItem == null || pPayrollItem.getAgencyNumber() == null)
            return false;

        //todo_rhn: replace null custodialId check with service check?  what happens to 401K offload if we try to offload before cust has custodialId?
        if (companyCustodialId == null)
            return false;

        if (!companyIsOn401KService)
            return false;

        String agencyNumber = pPayrollItem.getAgencyNumber().toUpperCase();
        agencyNumber = agencyNumber.replace(" ", "").replace("(", "").replace(")", "");
        return (agencyNumber.equalsIgnoreCase("401K"));
    }

    private ProcessResult<Map<PayrollRun, ArrayList<PaycheckDTO>>> preparePaycheckUpdates(List<QBPaycheck> pQBPaychecks) {

        ProcessResult<Map<PayrollRun, ArrayList<PaycheckDTO>>> processResult = new ProcessResult<Map<PayrollRun, ArrayList<PaycheckDTO>>>();

        HashMap<PayrollRun, ArrayList<PaycheckDTO>> payrollRunPaychecksMap = new HashMap<PayrollRun, ArrayList<PaycheckDTO>>();
        for (QBPaycheck qbPaycheck : pQBPaychecks) {
            currentPaycheck = qbPaycheck;
            currentEmployee = findEmployee(qbPaycheck);
            if (currentEmployee == null) {
                processResult.getMessages().EmployeeDoesNotExist(EntityName.Employee,
                                                                 qbPaycheck.getEmployeeID(),
                                                                 company.getSourceSystemCd().name(),
                                                                 company.getSourceCompanyId(),
                                                                 qbPaycheck.getEmployeeID());
                return processResult;
            }

            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPspPaycheckId());
            DomainEntitySet<PaycheckSplit> ddSplits = paycheck.getPaycheckSplits();

            if (company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                if (isNonRecalledPaycheck(paycheck) && hasUpdatedDirectDepositTransactions(qbPaycheck.getDDItems(), ddSplits)) {
                    ProcessResult ddModificationResult = new ProcessResult();
                    if (!paycheck.getPayrollRun().isOffloaded()) {
                        ddModificationResult.getMessages()
                            .DirectDepositPaycheckModificationBeforeOffload(EntityName.PayCheck, qbPaycheck.getPspPaycheckId());
                    } else {
                        ddModificationResult.getMessages()
                           .DirectDepositPaycheckModificationAfterOffload(EntityName.PayCheck, qbPaycheck.getPspPaycheckId());
                    }

                    CompanyEvent.createInvalidPaycheckInformationEvents(company,
                                                                        currentEmployee.getSourceEmployeeId(),
                                                                        paycheck,
                                                                        transmissionId,
                                                                        ddModificationResult);
                    processResult.merge(ddModificationResult);
                }
            } 

//todo_rhn add for 1.10.1 -- add EagerLoad of Paycheck.QbdtPaycheckInfo to aid performance            
//            if (company.hasService(ServiceCode.Tax) && !company.isCompanyOnService(ServiceCode.Tax) && paycheck.isTaxPaycheck()) {
//                // note: the above could be simplified but is an attempt at optimization until the paycheck.isTaxPaycheck() call is optimized
//                //todo_rhn: eager load the QBDTPayhceckInfo association for use in isTaxPaycheck
//                // Assisted OFX paychecks (even after the company has left service) cannot be modified
//                // only need to perform this check once company has left service; while company is on service company
//                // data is locked in QB and this processor will not update the paycheck information
//                if (isNonRecalledPaycheck(paycheck)) {
//                    ProcessResult taxModificationResult = new ProcessResult();
//                    taxModificationResult.getMessages().TaxPaycheckModified(EntityName.PayCheck, qbPaycheck.getPspPaycheckId());
//
//                    CompanyEvent.createInvalidPaycheckInformationEvents(company,
//                                                                        currentEmployee.getSourceEmployeeId(),
//                                                                        paycheck,
//                                                                        transmissionId,
//                                                                        taxModificationResult);
//                    processResult.merge(taxModificationResult);
//                }
//            }

            // important note:
            //      the DTO returned will not populate the txn collections
            //      the updatePayroll process will *not* update DD transactions; it will replace any existing
            //      non-DD txns with those attached in processNonDDPaycheckLineItems
            ProcessResult<PaycheckDTO> paycheckValidationResult = QBPayrollTranslator.createCoreDTO(qbPaycheck, findPSPSourceEmployeeId(qbPaycheck));
            if (!paycheckValidationResult.isSuccess()) {
                processResult.merge(paycheckValidationResult);
                return processResult;
            }
            PaycheckDTO paycheckDTO = paycheckValidationResult.getResult();

            if (paycheck.getQbdtPaycheckInfo() != null) {
                paycheckDTO.setQBDTPaycheckInfoDTO(PayrollServices.dtoFactory.create(paycheck.getQbdtPaycheckInfo()));
            } else {
                paycheckDTO.setQBDTPaycheckInfoDTO(new QBDTPaycheckInfoDTO());
            }
            paycheckDTO.getQBDTPaycheckInfoDTO().setListId(qbPaycheck.getPaycheckID());

            //TODO: check if before TOK offload of paycheck
            PaycheckProcessor paycheckProcessor = new PaycheckProcessor(paycheckDTO, qbPaycheck);
            ProcessResult paycheckResult = paycheckProcessor.process();
            processResult.merge(paycheckResult);
            if (!processResult.isSuccess())
                return processResult;

            ArrayList<PaycheckDTO> updatedPaychecks = payrollRunPaychecksMap.get(paycheck.getPayrollRun());
            if (updatedPaychecks == null) {
                updatedPaychecks = new ArrayList<PaycheckDTO>();
                payrollRunPaychecksMap.put(paycheck.getPayrollRun(), updatedPaychecks);
            }
            updatedPaychecks.add(paycheckDTO);
        }

        processResult.setResult(payrollRunPaychecksMap);
        return processResult;
    }

    private ProcessResult submitNewPaychecks(Collection<PayrollRunDTO> newPaycheckPayrollRunDTOs) {
        ProcessResult processResult = new ProcessResult();

        for (PayrollRunDTO payrollRunDTO : newPaycheckPayrollRunDTOs) {
            ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(),
                                                                              company.getSourceCompanyId(),
                                                                              payrollRunDTO,
                                                                              transmissionId);
            processResult.merge(payrollRunProcessResult);

            if (payrollRunProcessResult.isSuccess() && companyIsOn401KService) {
                //hack:
                //  1. Assisted processing should never add new paychecks (should have been added via OFX)
                //TODO_RHN_401K add check to verify submitNewPaychecks is never called for Assisted (all new paychecks must be added via OFX first)
                if (payrollRunProcessResult.getResult() == null) {
                    logger.warn("Skipping processing creation of 401K Paycheck b/c SubmitPayroll did not return a PayrollRun in result");
                    continue;
                }

                for (Paycheck paycheck : payrollRunProcessResult.getResult().getPaycheckCollection()) {
                    ThirdParty401kPaycheck.addTP401K(paycheck);
                }
            }
        }

        return processResult;
    }

    private PayrollRun getPayrollRun(Company company, PayrollRunDTO payrollRunDTO) {
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getTransmissionId());
        return payrollRun;
    }

    private ProcessResult submitQBDTPaycheckInfoUpdates(Map<PayrollRun, ArrayList<PaycheckDTO>> payrollRunPaychecksMap) {
        ProcessResult processResult = new ProcessResult();
        for (PayrollRun payrollRun : payrollRunPaychecksMap.keySet()) {
            PayrollRunDTO payrollRunDTO = PayrollServices.dtoFactory.create(payrollRun);
            payrollRunDTO.getPaychecks().addAll(payrollRunPaychecksMap.get(payrollRun));
            processResult.merge(PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(),
                                                                                   company.getSourceCompanyId(),
                                                                                   payrollRunDTO, true));
        }
        return processResult;
    }

    private ProcessResult submitExistingPaychecks(Map<PayrollRun, ArrayList<PaycheckDTO>> payrollRunPaychecksMap) {
        ProcessResult processResult = new ProcessResult();

        for (PayrollRun payrollRun : payrollRunPaychecksMap.keySet()) {
            ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.updatePayroll(company.getSourceSystemCd(),
                                                                             company.getSourceCompanyId(),
                                                                             payrollRun,
                                                                             payrollRunPaychecksMap.get(payrollRun),
                                                                             transmissionId);

            processResult.merge(payrollRunProcessResult);
            if (payrollRunProcessResult.isSuccess() && companyIsOn401KService) {
                //hack:
                //  1. retrieve payroll run from process result if DIY/DD to get in-memory updates
                //  2. Assisted processing does not call process() (validates only) - so payroll run not in process result and not updated by UpdatePayrollCore process
                if (!companyIsTax401K) {
                    payrollRun = payrollRunProcessResult.getResult();
                }

                for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                    ThirdParty401kPaycheck.update401K(paycheck);
                }
            }
        }

        return processResult;
    }

    private ProcessResult submitCompanyPayrollItems(Collection<CompanyPayrollItemDTO> companyPayrollItemDTOs) {
        ProcessResult processResult = new ProcessResult();

        for (CompanyPayrollItemDTO companyPayrollItemDTO : companyPayrollItemDTOs) {
            if (companyPayrollItemDTO.getPayrollItemCode() == null) {
                String sourceId = companyPayrollItemDTO.getSourcePayrollItemId();
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(sourceId);
                // tax items are not categories for PSP as they are represented by laws in PSP (and not payroll items)
                if (qbPayrollItem.getPayrollItemCategory() != QBPayrollItemCategory.TAX_ITEM) {
                    // this case should never occur as these values are assigned when dto is created
                    logger.warn("skipping save of payroll item b/c no payroll item code assigned: " + qbPayrollItem);
                }
                continue;
            }

            processResult.merge(PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(company.getSourceSystemCd(),
                                                                                             company.getSourceCompanyId(),
                                                                                             companyPayrollItemDTO));
        }

        return processResult;
    }

    private ProcessResult voidPaychecks(List<QBPaycheck> paychecksToVoid) {
        ProcessResult processResult = new ProcessResult();

        if (companyIsOn401KService) {
            // update 401k paycheck status and remove from pending offload queue if necessary
            if (paychecksToVoid.size() > 0) {
                List<Paycheck> voids = new ArrayList<Paycheck>();
                for (QBPaycheck qbPaycheck : paychecksToVoid) {
                    voids.add(Paycheck.findPaycheck(company, qbPaycheck.getPspPaycheckId()));
                }

                ThirdParty401kPaycheck.void401K(voids);
            }
        }

        /*
          dirty little hack to get the process validation events to be generated for Assisted+401k companies
          since OFX is received first, paychecks will be voided during that call (which will not call 401k process)
          PayrollServices.payrollManager.voidPayroll(...) ignores already voided paychecks
        */
        if (companyIsTax401K) {
            if (paychecksToVoid.size() > 0) {
                List<Paycheck> voids = new ArrayList<Paycheck>();
                for (QBPaycheck qbPaycheck : paychecksToVoid) {
                    voids.add(Paycheck.findPaycheck(company, qbPaycheck.getPspPaycheckId()));
                }
                VoidPayroll401k voidPayroll401k = new VoidPayroll401k(company, voids, transmissionId);
                processResult.merge(voidPayroll401k.validate());
                if (processResult.isSuccess()) {
                    processResult.merge(voidPayroll401k.process());
                }
            }
            return processResult;
        }

        // DIY + DD companies
        HashMap<PayrollRun, ArrayList<String>> payrollRunPaycheckIdsMap = new HashMap<PayrollRun, ArrayList<String>>();
        for (QBPaycheck qbPaycheck : paychecksToVoid) {
            currentPaycheck = qbPaycheck;
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPspPaycheckId());
            if (paycheck == null) {
                logger.error("logic error -- attempt to void a paycheck that can't be found in PSP -- : " + qbPaycheck);
                continue;
            }

            // skip paychecks that were already voided from DD OFX processing
            if (paycheck.getStatus() == PaycheckStatusCode.Inactive) {
                continue;
            }

            if (paycheck.getPayrollRun().isOffloaded()) {
                processResult.getMessages().DirectDepositPaychecksVoidedAfterOffload(EntityName.PayCheck, qbPaycheck.getPspPaycheckId());
                adapterValidationResult.getMessages().DirectDepositPaychecksVoidedAfterOffload(EntityName.PayCheck, qbPaycheck.getPspPaycheckId());
            }

            ArrayList<String> payrollPaychecksToVoid = payrollRunPaycheckIdsMap.get(paycheck.getPayrollRun());
            if (payrollPaychecksToVoid == null) {
                payrollPaychecksToVoid = new ArrayList<String>();
            }
            payrollPaychecksToVoid.add(qbPaycheck.getPspPaycheckId());
            payrollRunPaycheckIdsMap.put(paycheck.getPayrollRun(), payrollPaychecksToVoid);
        }

        for (PayrollRun payrollRun : payrollRunPaycheckIdsMap.keySet()) {
            VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
            voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            voidPayrollDTO.setPaycheckIdList(payrollRunPaycheckIdsMap.get(payrollRun));
            ProcessResult voidPaychecksResult = PayrollServices.payrollManager.voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO, transmissionId);
            processResult.merge(voidPaychecksResult);
            if (!voidPaychecksResult.isSuccess())
                return processResult;
        }

        return processResult;
    }

    private ProcessResult deletePaychecks(List<QBPaycheck> paychecksToDelete) {
        ProcessResult processResult = new ProcessResult();
        for (QBPaycheck qbPaycheck : paychecksToDelete) {
            ProcessResult<PayrollRun> deletedPaychecks = PayrollServices.payrollManager.deletePaycheck(company.getSourceSystemCd(), company.getSourceCompanyId(), qbPaycheck.getPaycheckID(), transmissionId);
            processResult.merge(deletedPaychecks);

            if (!processResult.isSuccess()) {
                return processResult;
            }

            if (companyIsOn401KService) {
                if (deletedPaychecks.getResult() == null) {
                    // TODO: Fix PayrollServices.payrollManager.deletePaycheck to return a paycheck
                    Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
                    PayrollRun payrollRun = new PayrollRun();
                    payrollRun.setFundingModel(company.getFundingModel().getFundingModelCd());
                    payrollRun.addPaycheck(paycheck);
                    deletedPaychecks.setResult(payrollRun);
                }

                for (Paycheck paycheck : deletedPaychecks.getResult().getPaycheckCollection()) {
                    ThirdParty401kPaycheck.delete401K(paycheck);
                }
            }
        }

        return processResult;
    }

    private boolean isNonRecalledPaycheck(Paycheck paycheck) {
        return paycheck.getStatus() == PaycheckStatusCode.Active && !paycheck.isVoided();
    }

    /**
     * Compare by verifying same number of transactions and then verifying
     * that transaction properties - bank routing #, account number, txn amount - have not changed
     *
     * @param qbDDItems
     * @param pspDdSplits
     * @return
     */
    private boolean hasUpdatedDirectDepositTransactions(List<QBDirectDepositItem> qbDDItems, DomainEntitySet<PaycheckSplit> pspDdSplits) {
        // should never occur
        if (qbDDItems == null && pspDdSplits == null)
            return false;

        if ((qbDDItems == null && pspDdSplits != null) || (qbDDItems != null && pspDdSplits == null))
            return true;

        boolean directDepositItemsUpdated = (pspDdSplits.size() != qbDDItems.size());
        if (!directDepositItemsUpdated) {
            Map<String, BigDecimal> bankAccountAmountMap = new HashMap<String, BigDecimal>(pspDdSplits.size());
            for (PaycheckSplit ddSplit : pspDdSplits) {
                BankAccount bankAccount = ddSplit.getEmployeeBankAccount().getBankAccount();
                String key = bankAccount.getRoutingNumber() + ":" + bankAccount.getAccountNumber();
                bankAccountAmountMap.put(key, SpcfUtils.convertToBigDecimal(ddSplit.getPaycheckSplitAmount()));
            }

            for (QBDirectDepositItem qbDirectDepositItem : qbDDItems) {
                String key = qbDirectDepositItem.getBankRoutingNumber() + ":" + qbDirectDepositItem.getBankAccountNumber();
                BigDecimal amt = bankAccountAmountMap.get(key);
                if (amt == null) {
                    directDepositItemsUpdated = true;
                    break;
                } else if (!amt.equals(qbDirectDepositItem.getCurrent())) {
                    directDepositItemsUpdated = true;
                    break;
                } else {
                    bankAccountAmountMap.remove(key);
                }
            }

            directDepositItemsUpdated = directDepositItemsUpdated || bankAccountAmountMap.size() > 0;
        }
        return directDepositItemsUpdated;
    }

    private boolean hasUpdatedKeyPaycheckInfo(QBPaycheck pQBPaycheck, Paycheck pPaycheck) {
        if (!pPaycheck.getGrossAmount().equals(pQBPaycheck.getGrossPay())) {
            return true;
        }

        if (!pPaycheck.getNetAmount().equals(pQBPaycheck.getNetPay())) {
            return true;
        }

        return false;
    }

    static boolean shouldExecute401KValidations(Company pCompany, SpcfCalendar pDate) {
        if (pDate == null)
            return false;

        if (!pCompany.isCompanyOnService(ServiceCode.ThirdParty401k))
            return false;

        return CompanyService.wasCompanyOnServiceForDate(pCompany, ServiceCode.ThirdParty401k, pDate);
    }

    /**
     *
     * @return
     */
    public ProcessResult getAdapterValidationResult() {
        return adapterValidationResult;
    }

    private class PaycheckProcessor {
        private QBPaycheck qbPaycheck;
        private PaycheckDTO paycheckDTO;
        private Employee employee;
        private long payStubOrder = 0;

        public PaycheckProcessor(PaycheckDTO pPaycheckDTO, QBPaycheck pQBPaycheck) {
            paycheckDTO = pPaycheckDTO;
            qbPaycheck = pQBPaycheck;
            employee = findEmployee(qbPaycheck.getEmployeeID());
        }

        public ProcessResult<PaycheckDTO> process() {
            ProcessResult<PaycheckDTO> processResult = new ProcessResult<PaycheckDTO>();

            employee = findEmployee(paycheckDTO.getEmployeeId());
            if (employee == null) {
                processResult.getMessages().EmployeeDoesNotExist(EntityName.Employee, paycheckDTO.getEmployeeId(), company.getSourceSystemCd().name(), company.getSourceCompanyId(), paycheckDTO.getEmployeeId());
                return processResult;
            }

            // EarningItems => Compensation { OtherCompensation }
            ProcessResult<List<CompensationTransactionDTO>> compensationTxns = createCompensationTxns(qbPaycheck.getEarningItems());
            processResult.merge(compensationTxns);
            if (!processResult.isSuccess()) return processResult;
            paycheckDTO.getCompensationTransactions().addAll(compensationTxns.getResult());

            // PreTaxItems => Deductions { Tp401kEmployeeDeferral, OtherPreTaxDeduction }
            ProcessResult<List<DeductionTransactionDTO>> preTaxDeductions = createPreTaxDeductions(qbPaycheck.getPreTaxItems());
            processResult.merge(preTaxDeductions);
            if (!processResult.isSuccess()) return processResult;
            paycheckDTO.getDeductionTransactions().addAll(preTaxDeductions.getResult());

            // AdjNetPayItems => Deductions { Tp401kRoth, Tp401kLoanPayment, OtherPostTaxDeduction }
            ProcessResult<List<DeductionTransactionDTO>> postTaxDeductions = createPostTaxDeductions(qbPaycheck.getAdjNetPayItems());
            processResult.merge(postTaxDeductions);
            if (!processResult.isSuccess()) return processResult;
            paycheckDTO.getDeductionTransactions().addAll(postTaxDeductions.getResult());

            // NonTaxCompanyItems => EmployerContributions { Tp401kSafeHarbor, Tp401kProfitSharing, NonTaxableEmployerContribution }
            ProcessResult<List<EmployerContributionTransactionDTO>> employerNonTaxContributions = createNonTaxERContributions(qbPaycheck.getNonTaxCompanyItems());
            processResult.merge(employerNonTaxContributions);
            if (!processResult.isSuccess()) return processResult;
            paycheckDTO.getEmployerContributionTransactions().addAll(employerNonTaxContributions.getResult());

            // TaxCompanyItems => EmployerContributions { TaxableEmployerContribution }
            ProcessResult<List<EmployerContributionTransactionDTO>> employerTaxableContributions = createTaxableERContributions(qbPaycheck.getTaxCompanyItems());
            processResult.merge(employerTaxableContributions);
            if (!processResult.isSuccess()) return processResult;
            paycheckDTO.getEmployerContributionTransactions().addAll(employerTaxableContributions.getResult());

            // TaxItems => Liabilities
            ProcessResult<List<LiabilityTransactionDTO>> liabilityTxns = createLiabilityTxns(qbPaycheck.getTaxItems());
            processResult.merge(liabilityTxns);
            if (!processResult.isSuccess()) return processResult;
            paycheckDTO.getLiabilityTransactions().addAll(liabilityTxns.getResult());

            return processResult;
        }

        private ProcessResult<List<LiabilityTransactionDTO>> createLiabilityTxns(List<QBPaycheckLineTaxItem> pPaycheckLineItems) {
            ProcessResult<List<LiabilityTransactionDTO>> processResult = new ProcessResult<List<LiabilityTransactionDTO>>();
            List<LiabilityTransactionDTO> liabilityTxns = new ArrayList<LiabilityTransactionDTO>();

            for (QBPaycheckLineTaxItem qbPaycheckLineItem : pPaycheckLineItems) {
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId());
                if (qbPayrollItem == null) {
                    processResult.getMessages().PaycheckPayrollItemDoesNotExistInTransmission(EntityName.Paycheck, currentPaycheck.getPaycheckID(), qbPaycheckLineItem.getPayrollItemId());
                    processResult.setResult(liabilityTxns);
                    return processResult;
                }

                // tax items are not associated with PayrollItemCodes (this differs from other line item processing)                
                LiabilityTransactionDTO liabilityDTO = new LiabilityTransactionDTO();
                //TODO: is TotalWages the YTD or WageBase ?
                liabilityDTO.setLiabilityTotalWages(qbPaycheckLineItem.getWageBase());
                liabilityDTO.setLiabilityTaxableWages(qbPaycheckLineItem.getIncomeSubjectToTax());
                liabilityDTO.setLiabilityAmount(qbPaycheckLineItem.getCurrent().multiply(NegativeOne));
                liabilityDTO.setLawId(qbPayrollItem.getDetailTypeId());
                if (qbPaycheckLineItem.getYTD() != null) {
                    liabilityDTO.setLiabilityAmountYTD(qbPaycheckLineItem.getYTD().multiply(NegativeOne));
                }

                liabilityTxns.add(liabilityDTO);
            }

            processResult.setResult(liabilityTxns);
            return processResult;

        }

        // EarningItems => Compensation { OtherCompensation }
        public ProcessResult<List<CompensationTransactionDTO>> createCompensationTxns(List<QBEarningItem> pPaycheckLineItems) {
            ProcessResult<List<CompensationTransactionDTO>> processResult = new ProcessResult<List<CompensationTransactionDTO>>();
            List<CompensationTransactionDTO> compensationTxns = new ArrayList<CompensationTransactionDTO>();

            for (QBEarningItem qbPaycheckLineItem : pPaycheckLineItems) {
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId());
                if (qbPayrollItem == null) {
                    processResult.getMessages().PaycheckPayrollItemDoesNotExistInTransmission(EntityName.Paycheck, currentPaycheck.getPaycheckID(), qbPaycheckLineItem.getPayrollItemId());
                    processResult.setResult(compensationTxns);
                    return processResult;
                }
                PayrollItemCode payrollItemCode = qbPayrollItem.getPayrollItemCode(PayrollItemCode.Compensation);

                CompanyPayrollItemDTO payrollItemDTO = companyPayrollItemDTOMap.get(qbPayrollItem.getPspPayrollItemId());
                payrollItemDTO.setPayrollItemCode(payrollItemCode);

                CompensationTransactionDTO compensationDTO = new CompensationTransactionDTO();
                compensationDTO.setPayStubOrder(++payStubOrder);
                compensationDTO.setSourcePayrollItemId(qbPayrollItem.getPspPayrollItemId());
                compensationDTO.setCompensationAmount(SpcfUtils.convertToSpcfMoney(qbPaycheckLineItem.getCurrent()));
                compensationDTO.setHoursWorked(SpcfUtils.convertToSpcfDecimal(qbPaycheckLineItem.getQty()));
                compensationDTO.setCompensationYTDAmount(SpcfUtils.convertToSpcfMoney(qbPaycheckLineItem.getYTD()));
                compensationTxns.add(compensationDTO);
            }

            processResult.setResult(compensationTxns);
            return processResult;
        }


        // AdjNetPayItems => Deductions { Tp401kRoth, Tp401kLoanPayment, OtherPostTaxDeduction }
        public ProcessResult<List<DeductionTransactionDTO>> createPostTaxDeductions(List<QBPaycheckLineItem> pPaycheckLineItems) {
            ProcessResult<List<DeductionTransactionDTO>> processResult = new ProcessResult<List<DeductionTransactionDTO>>();
            List<DeductionTransactionDTO> deductions = new ArrayList<DeductionTransactionDTO>();

            for (QBPaycheckLineItem qbPaycheckLineItem : pPaycheckLineItems) {
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId());
                if (qbPayrollItem == null) {
                    processResult.getMessages().PaycheckPayrollItemDoesNotExistInTransmission(EntityName.Paycheck, currentPaycheck.getPaycheckID(), qbPaycheckLineItem.getPayrollItemId());
                    processResult.setResult(deductions);
                    return processResult;
                }

                PayrollItemCode payrollItemCode = qbPayrollItem.getPayrollItemCode(PayrollItemCode.OtherPostTaxDeduction);
                if (is401KPaycheckLineItem(qbPaycheckLineItem)) {
                    if (qbPayrollItem.getTaxTrackingTypeId().equals(TAX_TRACKING_TYPE_ROTH)) {
                        payrollItemCode = PayrollItemCode.Tp401kRoth;
                    } else {
                        payrollItemCode = PayrollItemCode.Tp401kLoanPayment;
                    }
                }

                CompanyPayrollItemDTO payrollItemDTO = companyPayrollItemDTOMap.get(qbPayrollItem.getPspPayrollItemId());
                payrollItemDTO.setPayrollItemCode(payrollItemCode);

                DeductionTransactionDTO deductionDTO = new DeductionTransactionDTO();
                deductionDTO.setPayStubOrder(++payStubOrder);
                deductionDTO.setSourcePayrollItemId(qbPayrollItem.getPspPayrollItemId());
                deductionDTO.setDeductionAmount(qbPaycheckLineItem.getCurrent().multiply(NegativeOne));
                deductionDTO.setDeductionYTDAmount(qbPaycheckLineItem.getYTD().multiply(NegativeOne));
                deductions.add(deductionDTO);
            }

            processResult.setResult(deductions);
            return processResult;
        }

        // PreTaxItems => Deductions { Tp401kEmployeeDeferral, OtherPreTaxDeduction }
        public ProcessResult<List<DeductionTransactionDTO>> createPreTaxDeductions(List<QBPaycheckLineItem> pPaycheckLineItems) {
            ProcessResult<List<DeductionTransactionDTO>> processResult = new ProcessResult<List<DeductionTransactionDTO>>();
            List<DeductionTransactionDTO> deductions = new ArrayList<DeductionTransactionDTO>();

            for (QBPaycheckLineItem qbPaycheckLineItem : pPaycheckLineItems) {
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId());
                if (qbPayrollItem == null) {
                    processResult.getMessages().PaycheckPayrollItemDoesNotExistInTransmission(EntityName.Paycheck, currentPaycheck.getPaycheckID(), qbPaycheckLineItem.getPayrollItemId());
                    processResult.setResult(deductions);
                    return processResult;
                }

                PayrollItemCode payrollItemCode = qbPayrollItem.getPayrollItemCode(PayrollItemCode.OtherPreTaxDeduction);
                if (qbPayrollItem.getTaxTrackingTypeId().equals(TAX_TRACKING_TYPE_401K)) {
                    if (is401KPaycheckLineItem(qbPaycheckLineItem)) {
                        payrollItemCode = PayrollItemCode.Tp401kEmployeeDeferral;
                    } else if (shouldExecute401KValidations(qbPaycheck)) {
                        String employeeName = employee.getFirstMiddleLastName();
                        QBProcessingMessage processingMessage = ErrorMessageList.agencyNumberMissingFor401kTaxTrackingType(employeeName);
                        Message msg = new Message();
                        msg.GetMessageInfo().Level = MessageInfo.MessageLevel.WARNING;
                        msg.GetMessageInfo().Message = processingMessage.getMessage();
                        msg.GetMessageInfo().MessageCode = Integer.toString(processingMessage.getCode());
                        msg.GetMessageInfo().EntityName = EntityName.PayCheck;
                        msg.GetMessageInfo().SourceId = qbPaycheck.getPspPaycheckId();
                        processResult.getMessages().add(msg);

                        SpcfCalendar checkDate = SpcfCalendar.createInstance(currentPaycheck.getPayDate().getYear(),
                                currentPaycheck.getPayDate().getMonth(), currentPaycheck.getPayDate().getDay());

                        // TODO: unify/move payroll event creation but deal w/fact core and adapter creating events
                        CompanyEvent.createInvalidPaycheckInformationEvents(company,
                                                                            currentEmployee.getSourceEmployeeId(),
                                                                            currentPaycheck.getPspPaycheckId(),
                                                                            checkDate,
                                                                            currentPaycheck.getNetPay().toString(),
                                                                            transmissionId, 
                                                                            processResult);
                    }
                }

                CompanyPayrollItemDTO payrollItemDTO = companyPayrollItemDTOMap.get(qbPayrollItem.getPspPayrollItemId());
                payrollItemDTO.setPayrollItemCode(payrollItemCode);

                DeductionTransactionDTO deductionDTO = new DeductionTransactionDTO();
                deductionDTO.setPayStubOrder(++payStubOrder);
                deductionDTO.setSourcePayrollItemId(qbPayrollItem.getPspPayrollItemId());
                deductionDTO.setDeductionAmount(qbPaycheckLineItem.getCurrent().multiply(NegativeOne));
                deductionDTO.setDeductionYTDAmount(qbPaycheckLineItem.getYTD().multiply(NegativeOne));
                deductions.add(deductionDTO);
            }

            processResult.setResult(deductions);
            return processResult;
        }

        // NonTaxCompanyItems => EmployerContributions { Tp401kSafeHarbor, Tp401kProfitSharing, OtherNonTaxableEmployerContribution }
        public ProcessResult<List<EmployerContributionTransactionDTO>> createNonTaxERContributions(List<QBPaycheckLineItem> pPaycheckLineItems) {
            ProcessResult<List<EmployerContributionTransactionDTO>> processResult = new ProcessResult<List<EmployerContributionTransactionDTO>>();
            List<EmployerContributionTransactionDTO> contributions = new ArrayList<EmployerContributionTransactionDTO>();

            for (QBPaycheckLineItem qbPaycheckLineItem : pPaycheckLineItems) {
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId());
                if (qbPayrollItem == null) {
                    processResult.getMessages().PaycheckPayrollItemDoesNotExistInTransmission(EntityName.Paycheck, currentPaycheck.getPaycheckID(), qbPaycheckLineItem.getPayrollItemId());
                    processResult.setResult(contributions);
                    return processResult;
                }

                PayrollItemCode payrollItemCode = qbPayrollItem.getPayrollItemCode(PayrollItemCode.OtherNonTaxableEmployerContribution);
                if (is401KPaycheckLineItem(qbPaycheckLineItem)) {
                    if (isSafeHarbor) {
                        payrollItemCode = PayrollItemCode.Tp401kSafeHarbor;
                    } else {
                        payrollItemCode = PayrollItemCode.Tp401kProfitSharing;
                    }
                }

                CompanyPayrollItemDTO payrollItemDTO = companyPayrollItemDTOMap.get(qbPayrollItem.getPspPayrollItemId());
                payrollItemDTO.setPayrollItemCode(payrollItemCode);

                EmployerContributionTransactionDTO employerContributionDTO = new EmployerContributionTransactionDTO();
                employerContributionDTO.setPayStubOrder(++payStubOrder);
                employerContributionDTO.setContributionAmount(qbPaycheckLineItem.getCurrent().multiply(NegativeOne));
                employerContributionDTO.setContributionYTDAmount(qbPaycheckLineItem.getYTD().multiply(NegativeOne));
                employerContributionDTO.setSourcePayrollItemId(qbPayrollItem.getPspPayrollItemId());
                employerContributionDTO.setTotalWagesAmount(new BigDecimal(0));
                employerContributionDTO.setTaxableWagesAmount(new BigDecimal(0));
                contributions.add(employerContributionDTO);
            }

            processResult.setResult(contributions);
            return processResult;
        }

        private ProcessResult<List<EmployerContributionTransactionDTO>> createTaxableERContributions(List<QBPaycheckLineTaxCompanyItem> pPaycheckLineItems) {
            ProcessResult<List<EmployerContributionTransactionDTO>> processResult = new ProcessResult<List<EmployerContributionTransactionDTO>>();
            List<EmployerContributionTransactionDTO> contributions = new ArrayList<EmployerContributionTransactionDTO>();

            for (QBPaycheckLineTaxCompanyItem qbPaycheckLineItem : pPaycheckLineItems) {
                QBPayrollItem qbPayrollItem = qbPayrollItemMap.get(qbPaycheckLineItem.getPayrollItemId());
                if (qbPayrollItem == null) {
                    processResult.getMessages().PaycheckPayrollItemDoesNotExistInTransmission(EntityName.Paycheck, currentPaycheck.getPaycheckID(), qbPaycheckLineItem.getPayrollItemId());
                    processResult.setResult(contributions);
                    return processResult;
                }

                CompanyPayrollItemDTO payrollItemDTO = companyPayrollItemDTOMap.get(qbPayrollItem.getPspPayrollItemId());                
                PayrollItemCode payrollItemCode = qbPayrollItem.getPayrollItemCode(PayrollItemCode.OtherTaxableEmployerContribution);
                payrollItemDTO.setPayrollItemCode(payrollItemCode);

                EmployerContributionTransactionDTO employerContributionDTO = new EmployerContributionTransactionDTO();
                employerContributionDTO.setPayStubOrder(++payStubOrder);
                employerContributionDTO.setContributionAmount(qbPaycheckLineItem.getCurrent().multiply(NegativeOne));
                employerContributionDTO.setContributionYTDAmount(qbPaycheckLineItem.getYTD().multiply(NegativeOne));
                employerContributionDTO.setSourcePayrollItemId(qbPayrollItem.getPspPayrollItemId());
                employerContributionDTO.setTotalWagesAmount(qbPaycheckLineItem.getWageBase());
                employerContributionDTO.setTaxableWagesAmount(qbPaycheckLineItem.getIncomeSubjectToTax());
                contributions.add(employerContributionDTO);
            }

            processResult.setResult(contributions);
            return processResult;
        }

        private boolean shouldExecute401KValidations(QBPaycheck pQBPaycheck) {
            if (pQBPaycheck == null)
                return false;

            DateDTO payDateDTO = QBPayrollTranslator.createCoreDTO(pQBPaycheck.getPayDate());
            SpcfCalendar payDateCal = DateDTO.convertToSpcfCalendar(payDateDTO);
            return QBDTWSSubmitPayrollRequestProcess.shouldExecute401KValidations(company, payDateCal);
        }
    }
}
