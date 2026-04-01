	package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import org.apache.commons.lang.StringUtils;

    /**
 * Hand-written business logic
 */
public class Individual extends BaseIndividual {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Individual()
	{
		super();
	}

    @Override
    public void setEmail(String pEmail) {
        //If Email is changed update the email and reset HasInvalidEmail flag to false
        if ( !StringUtils.equals(getEmail(), pEmail) ) {
            setHasInvalidEmail(false);
        }
        super.setEmail(pEmail);
    }

    /**
     * Obtain an individuals full name formatted as Last Name, First Name Middle NAme
     *  (i.e. Doe, John K)
     * @return The full name formatted
     */
    public String getFullName() {

        String fullName = null;
        String lastName = getLastName();
        lastName = (lastName == null) ? "" : lastName;
        fullName = lastName;

        String firstName = getFirstName();
        firstName = (firstName == null) ? "" : firstName.trim();
        if (!fullName.equals("") && !firstName.equals("")) {
           fullName+=", ";
        }
        fullName += firstName;

        String middleName = getMiddleName();
        middleName = (middleName == null) ? "" : middleName;
        fullName += " " + middleName;

        return fullName.trim();
    }

    public String getFirstMiddleLastName() {
        String fullName = "";

        String firstName = getFirstName();
        firstName = (firstName == null) ? "" : firstName.trim();
        fullName += firstName;
        if (firstName.length() > 0)
            fullName += " ";

        String middleName = getMiddleName();
        middleName = (middleName == null) ? "" : middleName.trim();
        fullName += middleName;
        if (middleName.length() > 0)
            fullName += " ";

        String lastName = getLastName();
        lastName = (lastName == null) ? "" : lastName.trim();
        fullName += lastName;

        return fullName;
    }

    /**
     * Validates Contact data entity.
     *
     * @param pIndividual Individual data entity to be validated
     * @param pOwnerEntityName Owner Name
     * @param pOwnerEntityId Owner GSEQ
     * @return ProcessResult
     */
 public ProcessResult validateIndividual(EntityName pOwnerEntityName,
                                         String pOwnerEntityId)
    {
        ProcessResult processResult = new ProcessResult();

        if (this == null) {
            return processResult;
        }

        if ((getFirstName()== null) ||
                !(Validator.isValidLength(getFirstName(), 1, 80))) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "FirstName");
        }

        if ((getLastName()==null) ||
               !(Validator.isValidLength(getLastName(), 1, 80))) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "LastName");
        }

        if (!Validator.isValidLength(getMiddleName(), 0, 80)) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "MiddleName");
        }

        if (!Validator.isValidEmail(getEmail())) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "Email");
        }

        if (!Validator.isValidLength(getPhone(), 0, 20)) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "Phone");
        }

        processResult
                .merge(getMailingAddress().validateAddress(pOwnerEntityName, pOwnerEntityId));

        return processResult;
    }

    public String toString() {
        return "Individual  Name: " + getFullName();
    }
}