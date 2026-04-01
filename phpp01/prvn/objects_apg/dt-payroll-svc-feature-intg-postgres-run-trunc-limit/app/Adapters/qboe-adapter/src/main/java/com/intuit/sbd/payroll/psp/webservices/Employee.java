package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystem;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.employeeadd.EmployeeAdd;
import intuit.osp.pse.dd.wsapi.xsd.employeeaddrs.EmployeeAddRs;
import intuit.osp.pse.dd.wsapi.xsd.employeedeactivate.EmployeeDeactivate;
import intuit.osp.pse.dd.wsapi.xsd.employeedeactivaters.EmployeeDeactivateRs;
import intuit.osp.pse.dd.wsapi.xsd.employeequery.EmployeeQuery;
import intuit.osp.pse.dd.wsapi.xsd.employeequeryrs.EmployeeQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.employeeret.EmployeeRet;
import intuit.osp.pse.dd.wsapi.xsd.employeeupdate.EmployeeUpdate;
import intuit.osp.pse.dd.wsapi.xsd.employeeupdaters.EmployeeUpdateRs;
import intuit.osp.pse.dd.wsapi.xsd.responsestatus.ResponseStatus;
import org.w3c.dom.Element;

import java.util.List;

/**
 * This is the <code>Employee</code> web service object that allows for
 * adding, updating, deactivating an employee
 * 
 * @author Sean Barenz
 */
public class Employee extends WS {
    private static SpcfLogger logger = Application.getLogger(Employee.class);
	private static final String EMPLOYEE_EXISTS_AND_ACTIVE = "163";
	private static final intuit.osp.pse.dd.wsapi.xsd.employeeret.ObjectFactory
          employeeRetFactory = new intuit.osp.pse.dd.wsapi.xsd.employeeret.ObjectFactory();
    DDCommon ddCommon = new DDCommon();

	WSServerContext context = null;

	EmployeeAddRs employeeAddRs = null;

	EmployeeUpdateRs employeeUpdateRs = null;

	EmployeeDeactivateRs employeeDeactivateRs = null;

	/**
	 * This is the Employee Add method for the Employee web service
	 * 
	 * @param requestDoc
	 *            Request document from the client in XML form
	 * @return Returns the output to the client as an XML document
	 * @throws WSException
	 *             Whenever an exception occurs
	 */
	public Element add(Element requestDoc) throws WSException {
		Element returnDoc;
        String[] expectedErrorCodes = {"137", "138", "101", "145", "5001", "125", "169", "177", "163"};

		try {
			initializeWebMethod("Employee", "add");

			EmployeeAdd employeeAdd = (EmployeeAdd) context.translateInputElement(requestDoc);

			// Create the employee domain object
			com.intuit.sbd.payroll.psp.domain.Employee domainEmployee = null;
			EmployeeDTO employeeDTO = populateEmployeeDTO(employeeAdd);

			// Run the employee through the process flow
			ProcessResult<com.intuit.sbd.payroll.psp.domain.Employee> result =
                                    PayrollServices.employeeManager.addEmployee(
                                            SourceSystemCode.valueOf(employeeAdd.getSourceSystemCd()),
                                            employeeAdd.getCompanyID(), employeeDTO);
			domainEmployee = result.getResult();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany =
                Company.findCompany(employeeAdd.getCompanyID(),
                                                          SourceSystemCode.valueOf(employeeAdd.getSourceSystemCd()));

			// Give response back to the client
			employeeAddRs = (EmployeeAddRs) context.getOutputDTO();
			EmployeeRet employeeRet = employeeAddRs.getEmployeeRet();

			if (result.isSuccess()) {
				build_EmployeeRet(domainEmployee, employeeRet);
				employeeAddRs.setEmployeeRet(employeeRet);
			} else {
                DDCommon.replacePSPError(result, "1101", "177", domainCompany);
				// Return an employee object if one exists. If the employee
				// already exists
				// return the employee object with ther response
				Message message = result.getMessages().get(0);
				if (message.getMessageCode().equals(EMPLOYEE_EXISTS_AND_ACTIVE)) {
					build_EmployeeRet(domainEmployee, employeeRet);
					employeeAddRs.setEmployeeRet(employeeRet);
				} else {
					employeeAddRs.setEmployeeRet(null);
				}
			}

            PayrollServices.commitUnitOfWork();
			employeeAddRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
			returnDoc = context.translateOutputDTO();
		} catch (WSValidationException e) {
			PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw e;
		} catch (Exception e) {
			PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw new WSException(DDCommon.pse_Error, e);
		} finally {
            PayrollServices.rollbackUnitOfWork();
        }
        
        return returnDoc;
	}

	/**
	 * Web service method for updating an existing employee
	 * 
	 * @param requestDoc
	 *            XML document containing the client request
	 * @return XML output containing the response
	 * @throws WSException
	 *             Thrown if there is an excpetion of any sort
	 */
	public Element update(Element requestDoc) throws WSException {
		Element returnDoc;
        String[] expectedErrorCodes = {"137", "138", "101", "145", "5001", "125", "169", "177", "168", "178"};

		try {
			initializeWebMethod("Employee", "update");
			EmployeeUpdate employeeUpdate = (EmployeeUpdate) context
					.translateInputElement(requestDoc);

			// Create the employee domain object
			com.intuit.sbd.payroll.psp.domain.Employee domainEmployee = null;
			EmployeeDTO employeeDTO = populateEmployeeDTO(employeeUpdate);

			// Run the employee through the process flow
			ProcessResult<com.intuit.sbd.payroll.psp.domain.Employee> result =
                    PayrollServices.employeeManager.updateEmployee(
                            SourceSystemCode.valueOf(employeeUpdate.getSourceSystemCd()),
                            employeeUpdate.getCompanyID(), employeeDTO);
			domainEmployee = result.getResult();

			// Give response back to the client
			employeeUpdateRs = (EmployeeUpdateRs) context.getOutputDTO();
			EmployeeRet employeeRet = employeeUpdateRs.getEmployeeRet();

			if (result.isSuccess()) {
				build_EmployeeRet(domainEmployee, employeeRet);
				employeeUpdateRs.setEmployeeRet(employeeRet);
			} else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        employeeUpdate.getCompanyID(), SourceSystemCode.valueOf(employeeUpdate.getSourceSystemCd()));
                DDCommon.replacePSPError(result, "1101", "177", domainCompany);

                employeeUpdateRs.setEmployeeRet(null);
			}

			PayrollServices.commitUnitOfWork();
			employeeUpdateRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
			returnDoc = context.translateOutputDTO();
		} catch (WSValidationException e) {
			PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw e;
		} catch (Exception e) {
			PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw new WSException(DDCommon.pse_Error, e);
		} finally {
            PayrollServices.rollbackUnitOfWork();
        }
		return returnDoc;
	}

    /**
     * Web service method for finding all employees for a particular company
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element query(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169"};
        EmployeeQueryRs employeeQueryRs;
        initializeWebMethod("Employee", "query");
        employeeQueryRs = (EmployeeQueryRs) context.getOutputDTO();
        try {
            EmployeeQuery employeeQuery =
                    (EmployeeQuery) context.translateInputElement(requestDocument);
            String companyId = employeeQuery.getCompanyID();
            String sourceSystemCd = employeeQuery.getSourceSystemCd();
            ProcessResult validationResult = new ProcessResult();
            // Validate whether the company exists or not
            com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, companyId, sourceSystemCd, companyId);
            } else {

                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Employee> employees =
                        com.intuit.sbd.payroll.psp.domain.Employee.findEmployees(company);
                List<EmployeeRet> employeeRetList = employeeQueryRs.getEmployeeRet();
                populateEmployeeRetList(employeeRetList, employees);
            }
            ResponseStatus responseStatus = DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes);
            employeeQueryRs.setResponseStatus(responseStatus);
            returnDoc = context.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException wsve) {
            PayrollServices.rollbackUnitOfWork();
			logger.error(wsve.getMessage(), wsve.getCause());
			throw wsve;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    } 

    /**
	 * Web service method for deactivating an existing employee
	 * 
	 * @param requestDoc
	 *            XML document containing the client request
	 * @return XML output containing the response
	 * @throws WSException
	 *             Thrown if there is an excpetion of any sort
	 */
	public Element deactivate(Element requestDoc) throws WSException {
		Element returnDoc;
        String[] expectedErrorCodes = {"138", "137", "145", "125", "169", "168", "215", "177"};

		try {
			initializeWebMethod("Employee", "deactivate");
			EmployeeDeactivate employeeDeactivate = (EmployeeDeactivate) context
					.translateInputElement(requestDoc);

			// Run the employee through the process flow
			ProcessResult<com.intuit.sbd.payroll.psp.domain.Employee> result =
                    PayrollServices.employeeManager.deactivateEmployee(
                            SourceSystemCode.valueOf(employeeDeactivate.getSourceSystemCd()),
                            employeeDeactivate.getCompanyID(), employeeDeactivate.getEmployeeID(), null);
			com.intuit.sbd.payroll.psp.domain.Employee domainEmployee = result.getResult();

			// Give response back to the client
			employeeDeactivateRs = (EmployeeDeactivateRs) context.getOutputDTO();
			EmployeeRet employeeRet = employeeDeactivateRs.getEmployeeRet();

			if (result.isSuccess()) {
				build_EmployeeRet(domainEmployee, employeeRet);
				employeeDeactivateRs.setEmployeeRet(employeeRet);
			} else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        employeeDeactivate.getCompanyID(),
                        SourceSystemCode.valueOf(employeeDeactivate.getSourceSystemCd()));
                DDCommon.replacePSPError(result, "1101", "177", domainCompany);
                employeeDeactivateRs.setEmployeeRet(null);
			}
			PayrollServices.commitUnitOfWork();
			employeeDeactivateRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
			returnDoc = context.translateOutputDTO();
		} catch (WSValidationException e) {
			PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw e;
		} catch (Exception e) {
			PayrollServices.rollbackUnitOfWork();
			logger.error(e.getMessage(), e.getCause());
			throw new WSException(DDCommon.pse_Error, e);
		} finally {
            PayrollServices.rollbackUnitOfWork();
        }
		return returnDoc;
	}

	/**
	 * Private internal method to intialize all major web methods 
	 * 
	 * @param pEntity
	 *            Entity such as employee
	 * @param pMethod
	 *            method to call on that entity
	 * @throws WSException
	 *             Thrown if unable to intialize
	 */
	private void initializeWebMethod(String pEntity, String pMethod) throws WSException {
		context = new WSServerContext(pEntity, pMethod);
		PayrollServices.beginUnitOfWork();
	}

    private void populateEmployeeRetList(
            List<EmployeeRet> employeeRetList,
            DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Employee> employees) throws Exception {

        for (com.intuit.sbd.payroll.psp.domain.Employee employee :employees) {
            EmployeeRet employeeRet = employeeRetFactory.createEmployeeRet();
            build_EmployeeRet(employee, employeeRet);
            employeeRetList.add(employeeRet);
        }
    }

    /**
	 * Instantiates an Employee DO based on the incoming XML passed from the
	 * user
	 * 
	 * @param pEmployeeAdd
	 *            The EmployeeAdd object that contains the XML input from the
	 *            user
	 */
	private EmployeeDTO populateEmployeeDTO(EmployeeAdd pEmployeeAdd) {
		if (pEmployeeAdd == null) {
			String args[] = { "EmployeeAdd", "build_CompanyAddRs" };
			throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
		}
		intuit.osp.pse.dd.wsapi.xsd.employee.Employee employee = pEmployeeAdd.getEmployee();
		return populateEmployeeDTO(employee);
	}

	/**
	 * Instantiates an Employee DO based on the incoming XML passed from the
	 * user
	 * 
	 * user
	 * 
	 * @param pEmployeeUpdate
	 *            EmployeeUpdate processs
	 */
	private EmployeeDTO populateEmployeeDTO(EmployeeUpdate pEmployeeUpdate) {
		if (pEmployeeUpdate == null) {
			String args[] = { "EmployeeAdd", "build_CompanyAddRs" };
			throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
		}
		intuit.osp.pse.dd.wsapi.xsd.employee.Employee employee = pEmployeeUpdate.getEmployee();
		return populateEmployeeDTO(employee);
	}

	/**
	 * Main method to initalize domain object
	 * 
	 * @param pDTOEmployee
	 *            The DTO version of the Employee
	 */
	private EmployeeDTO populateEmployeeDTO(
			intuit.osp.pse.dd.wsapi.xsd.employee.Employee pDTOEmployee) {
		// Populate the Employee Object
		EmployeeDTO employeeDTO = new EmployeeDTO();
		employeeDTO.setFirstName(pDTOEmployee.getFirstName());
		employeeDTO.setLastName(pDTOEmployee.getLastName());
		employeeDTO.setMiddleName(pDTOEmployee.getMiddleName());
		employeeDTO.setSocialSecurityNumber(pDTOEmployee.getSocialSecurityNumber());
		employeeDTO.setEmployeeId(pDTOEmployee.getEmployeeID());

		return employeeDTO;
	}

	/**
	 * Builds the return object that will be tranlated into an XML document that
	 * is received by the client
	 * 
	 * @param pEmployee
	 *            The domain object containing the employee data to populate
	 * @param pEmployeeRet
	 *            The return object to be populated
	 * @return EmployeeRet containing the data to be tranlated
	 * @throws Exception
	 *             Whenever an exception occurs during the build Employee
	 *             process
	 */
	private EmployeeRet build_EmployeeRet(com.intuit.sbd.payroll.psp.domain.Employee pEmployee,
			EmployeeRet pEmployeeRet) throws Exception {

		if (pEmployee != null) {
			Company company = pEmployee.getCompany();
			pEmployeeRet.setCompanyID(company.getSourceCompanyId());
			pEmployeeRet.setSourceSystemCd(company.getSourceSystemCd().toString());
			pEmployeeRet.setEmployeeInfo(DDCommon.build_EmployeeInfo(pEmployee));

		} else {
			String args[] = { "EmployeeBO", "build_EmployeeRet" };
			throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
		}
		return pEmployeeRet;
	}
}
