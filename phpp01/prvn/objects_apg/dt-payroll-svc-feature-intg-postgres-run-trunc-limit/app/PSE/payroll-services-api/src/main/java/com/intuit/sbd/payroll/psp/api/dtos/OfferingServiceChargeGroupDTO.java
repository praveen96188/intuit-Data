package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Feb 15, 2008
 * Time: 3:08:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class OfferingServiceChargeGroupDTO {
    private String id;             // SPCF unique Id of the entity
    private String offeringId;     // SPCF unique Id of the parent offering
    private String name;           // Common name of the offering
    private String description;    // Long description of the offering
    private OfferingServiceChargeType appliesTo;      // e.g. PerTransmission, PerPaycheck

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(String pOfferingId) {
        offeringId = pOfferingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public OfferingServiceChargeType getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(OfferingServiceChargeType pAppliesTo) {
        appliesTo = pAppliesTo;
    }


    /**
     * Perform validations common to more than one operation.
     * @return a ProcessResult
     */
    public ProcessResult validateCommon()
    {
        ProcessResult result = new ProcessResult();

        // Name non-null and non-empty	5002 Required 'Name' input is missing or blank
        if (getName()==null || getName().length()==0) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "Name");
        }

        // AppliesTo non-null and non-empty	5002 Required 'AppliesTo' input is missing or blank
        if (getAppliesTo() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "AppliesTo");
        }

        return result;
    }
}
