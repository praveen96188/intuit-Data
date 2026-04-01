package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.EmployeeTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTOAssistedValidator;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMP;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EmployeeWagePlan;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 11, 2010
 * Time: 1:48:42 PM
 */
public class EmployeeProcessor {
    private Company mCompany;
    private AssistedConnectionInformation mConnectionInformation;
    private CredentialType mCredentialType;
    private boolean mIsAssistedRequest;
    private Set<String> mMigratedEmployees = new HashSet<String>();
    private Set<String> employeeIdSet = new HashSet<String>();
    private List<EmployeeDTO> newEmpMap = new ArrayList<EmployeeDTO>();
    private List<EmployeeDTO> modEmpMap = new ArrayList<EmployeeDTO>();


    private List<EmployeeDTO> mEmployeeUpdates = new ArrayList<EmployeeDTO>();
    private Map<String, String> mEmployeeUpdateIds = new HashMap<String, String>();
    private int numOfEmployeeIdsNotSet = 0;



    public List<EmployeeDTO> getEmployeeUpdates() {
        return mEmployeeUpdates;
    }

    public Map<String, String> getEmployeeUpdateIds() {
        return mEmployeeUpdateIds;
    }

    public EmployeeProcessor(Company pCompany, AssistedConnectionInformation pAssistedConnectionInformation, CredentialType pCredentialType) {
        mCompany = pCompany;
        mConnectionInformation = pAssistedConnectionInformation;
        mCredentialType = pCredentialType;
        mIsAssistedRequest = mConnectionInformation.isAssistedRequest();
    }

    private boolean ignoreForCredType() {
        return mCredentialType != CredentialType.Pin && (mCompany.isCompanyOnService(ServiceCode.Tax) || (mCompany.isCompanyOnService(ServiceCode.DirectDeposit) && (!mCompany.onUsageBilling())));
    }

    public ProcessResult processEmployees(List<IEMP> pEmployees) {
        ProcessResult processResult = new ProcessResult();

        if (ignoreForCredType()) {
            return processResult;
        }

        if (pEmployees.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);

            int loadAllEmployeesCutoff = SystemParameter.findIntValue(SystemParameter.Code.PRELOAD_EMPLOYEE_COUNT, 300);

            // eager load employees
            List<String> employeeIdList = new ArrayList<String>();
            for (IEMP pEmployee : pEmployees) {
                if (pEmployee.getIEMPID() != null && !pEmployee.getIEMPID().equals(Employee.DEFAULT_QB_EMPLOYEE_ID)) {
                    employeeIdList.add(pEmployee.getIEMPID());
                }
                mCompany.usedEmployeeId(pEmployee.getIEMPID());
                if (employeeIdList.size() > loadAllEmployeesCutoff) {
                    // if there are more than the preload employee count system parameter mods load all of the employees
                    com.intuit.sbd.payroll.psp.domain.Employee.eagerlyLoadEmployees(mCompany, null);
                    employeeIdList = new ArrayList<String>();
                    numOfEmployeeIdsNotSet = pEmployees.size() - loadAllEmployeesCutoff;
                    break;
                }
            }
            if (employeeIdList.size() > 0) {
                com.intuit.sbd.payroll.psp.domain.Employee.eagerlyLoadEmployees(mCompany, employeeIdList);
            }
        }

        for (IEMP iEmployee : pEmployees) {
            processEmployee(iEmployee);

        }



        for (EmployeeDTO employeeDTO : modEmpMap) {
            if (employeeDTO.isAccrualOnlyMod()) {
                PayrollServices.employeeManager.updateEmployeeAccrual(mCompany.getSourceSystemCd(),
                                                                      mCompany.getSourceCompanyId(),
                                                                      employeeDTO);

            } else {
                processResult.merge(PayrollServices.employeeManager.updateEmployee(mCompany.getSourceSystemCd(),
                                                                                   mCompany.getSourceCompanyId(),
                                                                                   employeeDTO));
            }
            if (employeeDTO.getOriginalSourceId() != null) {
                Application.getHibernateSession().flush();
            }

            if (!processResult.isSuccess()) {
                return processResult;
            }
        }
        for (EmployeeDTO employeeDTO : newEmpMap) {
            processResult.merge(PayrollServices.employeeManager.addEmployee(mCompany.getSourceSystemCd(),
                                                                            mCompany.getSourceCompanyId(),
                                                                            employeeDTO, null,
                                                                            mEmployeeUpdateIds.containsKey(employeeDTO.getEmployeeId())));

            if (!processResult.isSuccess()) {
                return processResult;
            }
        }
        modEmpMap.clear();
        newEmpMap.clear();
        return processResult;
    }

    private void processEmployee(IEMP pIEmployee) {
        Employee employeeWrapper = new Employee(pIEmployee);

        com.intuit.sbd.payroll.psp.domain.Employee domainEmployee = null;

        if (employeeWrapper.getListId() != null) {
            // it is possible for QBDT to renumber the employee ids. If we already have the employee we will just update the source id
            domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployeeByQBListId(mCompany, employeeWrapper.getListId());
            // If the source employee id in the OFX and the one in the DB do not match, check if there is any employee
            // with the same emp ID as the one in the OFX before we proceed with the renumbering.
            if(domainEmployee != null && domainEmployee.getSourceEmployeeId().equalsIgnoreCase(employeeWrapper.getSourceId())){
                updateEmployee(employeeWrapper, domainEmployee, true);
            }else if(domainEmployee != null){
                com.intuit.sbd.payroll.psp.domain.Employee employeeById = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(mCompany, employeeWrapper.getSourceId());
                if(employeeById == null){
                    updateEmployee(employeeWrapper, domainEmployee, true);
                }else{
                    EmployeeDTO employeeDTO =  renumberEmployee(employeeWrapper.getSourceId(), employeeById);
                    modEmpMap.add(employeeDTO);
                    updateEmployee(employeeWrapper, domainEmployee, false);
                }
            }else{
                domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(mCompany, employeeWrapper.getSourceId());
                if(domainEmployee != null && domainEmployee.getSourceEmployeeId().equalsIgnoreCase(employeeWrapper.getSourceId())){
                    updateEmployee(employeeWrapper, domainEmployee, true);
                } else{
                    domainEmployee = null;
                }
            }
        }else {
            // We will search by source id only if the list id is not present
            domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(mCompany, employeeWrapper.getSourceId());
            if(domainEmployee != null){
                updateEmployee(employeeWrapper, domainEmployee, true);
            }
        }
        
        if(mIsAssistedRequest && domainEmployee != null && (domainEmployee.getQbdtEmployeeInfo() == null || !domainEmployee.getQbdtEmployeeInfo().getIsAssisted())) {
            // migrated employee
            mMigratedEmployees.add(domainEmployee.getSourceEmployeeId());
            // add new id if it is changing
            if(!domainEmployee.getSourceEmployeeId().equals(employeeWrapper.getSourceId())) {
                mMigratedEmployees.add(employeeWrapper.getSourceId());
            }
        }

        if (domainEmployee == null) {
            //pr.merge(
            addEmployee(employeeWrapper);
        }
    }


    private EmployeeDTO renumberEmployee(String pEmployeeId, com.intuit.sbd.payroll.psp.domain.Employee pEmpById) {
        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(pEmpById, false);

        // Since all employees were loaded and we were not able to set the employee id for all of them, set the
        // next employee id to the difference of employees that were loaded and maximum
        // Else we have loaded all the employee IDs from the OFX and we need not do any hack
        if (numOfEmployeeIdsNotSet > 0) {
            employeeDTO.setEmployeeId(String.valueOf(Integer.parseInt(mCompany.getNextEmployeeId()) + numOfEmployeeIdsNotSet));
            numOfEmployeeIdsNotSet = 0;
        } else {
            employeeDTO.setEmployeeId(mCompany.getNextEmployeeId());
        }

        mCompany.usedEmployeeId(employeeDTO.getEmployeeId());
        mEmployeeUpdateIds.put(pEmployeeId, employeeDTO.getEmployeeId());

        NaturalKey naturalKey = new NaturalKey(Employee.class, mCompany.getSourceCompanyId(), pEmployeeId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
        if (primaryKey != null) {
            Application.getSessionCache().removePrimaryKey(naturalKey);
        }
        return  employeeDTO;

    }

    private void addEmployee(Employee pEmployeeWrapper) {
        if (employeeIdSet.contains(pEmployeeWrapper.getSourceId())) {
            return;
        }

        employeeIdSet.add(pEmployeeWrapper.getSourceId());

        EmployeeDTO employeeDTO = new EmployeeDTO();
        EmployeeTranslator.populateEmployeeDTO(pEmployeeWrapper, employeeDTO, mIsAssistedRequest);

        EmployeeDTOAssistedValidator employeeDTOAssistedValidator = new EmployeeDTOAssistedValidator(mCompany);
        employeeDTO.setValidator(employeeDTOAssistedValidator);
        newEmpMap.add(employeeDTO);
    }

    private void updateEmployee(Employee pEmployeeWrapper,
                                com.intuit.sbd.payroll.psp.domain.Employee pDomainEmployee, boolean insertAtEnd) {
        EmployeeDTO employeeDTO;
        if (pEmployeeWrapper.isAccrualOnlyMod()) {
            employeeDTO = new EmployeeDTO();
            EmployeeTranslator.populateEmployeeDTO(pEmployeeWrapper, employeeDTO, mIsAssistedRequest);

            employeeDTO.setExistingEmployeeGuid(pDomainEmployee.getId().toString());
        } else {
            if (employeeIdSet.contains(pEmployeeWrapper.getSourceId())) {
                // we already processed this employee
                return;
            }

            employeeIdSet.add(pEmployeeWrapper.getSourceId());

            employeeDTO = PayrollServices.dtoFactory.create(pDomainEmployee, false);
            EmployeeTranslator.populateEmployeeDTO(pEmployeeWrapper, employeeDTO, mIsAssistedRequest);

            if (pEmployeeWrapper.getEmployeeWagePlans() != null
                    && pEmployeeWrapper.getEmployeeWagePlans().size() == 0
                    && pDomainEmployee.getEmployeeWagePlanCollection() != null
                    && pDomainEmployee.getEmployeeWagePlanCollection().size() > 0) {
                DomainEntitySet<EmployeeWagePlan> employeeWagePlans = pDomainEmployee.getEmployeeWagePlanCollection();
                for (EmployeeWagePlan employeeWagePlan : employeeWagePlans) {
                    employeeDTO.getWagePlanDTOs().add(PayrollServices.dtoFactory.create(employeeWagePlan));
                }
            }

            employeeDTO.setExistingEmployeeGuid(pDomainEmployee.getId().toString());
            employeeDTO.setValidator(new EmployeeDTOAssistedValidator(mCompany));
        }

        if(employeeDTO.getOriginalSourceId() != null &&mEmployeeUpdateIds.keySet().contains(employeeDTO.getOriginalSourceId())){
            for(EmployeeDTO empDTO : modEmpMap){
                // e.g. employeeDTO: originalId -> newEmpId = 2 ->3
                // updatedIds contains 1->2. We need to put DTO before the match.
                if(employeeDTO.getOriginalSourceId().equalsIgnoreCase(empDTO.getOriginalSourceId())){
                    int index = modEmpMap.indexOf(empDTO);
                    modEmpMap.remove(index);
                    break;
                }
            }
        }
        if(mEmployeeUpdateIds.values().contains(employeeDTO.getOriginalSourceId())){
            for(EmployeeDTO empDTO : modEmpMap){
                // e.g. employeeDTO: originalId -> newEmpId = 2 ->3
                // updatedIds contains 1->2. We need to put DTO before the match.
                if(empDTO.getEmployeeId().equalsIgnoreCase(employeeDTO.getOriginalSourceId())){
                    int index = modEmpMap.indexOf(empDTO);
                    modEmpMap.add(index, employeeDTO);
                    break;
                }
            }
        }else{
            modEmpMap.add(employeeDTO);
        }
        mEmployeeUpdateIds.put(employeeDTO.getOriginalSourceId(), employeeDTO.getEmployeeId());

    }

    public ProcessResult deleteEmployees(List<String> pEmployeeIds) {
        ProcessResult processResult = new ProcessResult();

        if (ignoreForCredType()) {
            return processResult;
        }

        if (pEmployeeIds.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);
        }

        for (String employeeId : pEmployeeIds) {
            com.intuit.sbd.payroll.psp.domain.Employee employee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(mCompany, employeeId);

            if (employee != null) {
                EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
                EmployeeDTOAssistedValidator employeeDTOAssistedValidator = new EmployeeDTOAssistedValidator(mCompany);
                employeeDTO.setValidator(employeeDTOAssistedValidator);
                employeeDTO.getQBDTEmployeeInfoDTO().setIsDeleted(true);


                processResult.merge(PayrollServices.employeeManager.updateEmployee(mCompany.getSourceSystemCd(),
                                                                                   mCompany.getSourceCompanyId(),
                                                                                   employeeDTO));
                if (!processResult.isSuccess()) {
                    return processResult;
                }
            }
        }

        return processResult;
    }

    public Set<String> getMigratedEmployees() {
        return mMigratedEmployees;
    }
}
