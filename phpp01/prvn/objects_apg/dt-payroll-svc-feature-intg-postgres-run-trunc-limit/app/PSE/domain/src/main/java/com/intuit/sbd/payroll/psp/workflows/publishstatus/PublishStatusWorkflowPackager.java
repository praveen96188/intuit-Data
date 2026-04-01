package com.intuit.sbd.payroll.psp.workflows.publishstatus;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.workflows.ByteSet;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.employee.EmployeePublishStatusWorkflows;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.payee.PayeePublishStatusWorkflows;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

public class PublishStatusWorkflowPackager {

    public static final SpcfLogger logger = Application.getLogger(PublishStatusWorkflowPackager.class);
    public static int DEFAULT_FLAG_SIZE = 16;

    protected ByteSet byteSet = null;

    public PublishStatusWorkflowPackager() {
        this(DEFAULT_FLAG_SIZE);
    }

    public PublishStatusWorkflowPackager(int flagSize) {

        this(null,flagSize);
    }

    public PublishStatusWorkflowPackager(String workFlowsFlag, int flagSize) {
        byteSet = new ByteSet(flagSize);
        initializeByteSet(workFlowsFlag);
    }

    public PublishStatusWorkflowPackager(String workFlowsFlag) {
        this(workFlowsFlag, DEFAULT_FLAG_SIZE);
    }

    /**
     * Initialize the byteSet using the workFlowsFlag returned from the database
     * @param workFlowsFlag
     */
    private void initializeByteSet(String workFlowsFlag)  {

        if(StringUtils.isBlank(workFlowsFlag))
        {
            return;
        }
        char[] charArray = workFlowsFlag.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            int value = Character.getNumericValue(charArray[i]);
            byteSet.set(i, value);
        }

    }

    private void setWorkflowState(int index, int value) {
        byteSet.set(index, value);
    }

    private int getWorkflowStateValue(int index) {
        return byteSet.get(index);
    }
    /**
     * Set the workflow state to the given workflow
     * @param workflows
     * @param workflowState
     */
    public void setWorkflowState(CompanyPublishStatusWorkflows workflows, PublishStatusWorkflowState workflowState)  {

        //set the workflowstate value returned by workflowState.getValue() to the index returned by workflows.getValue()
        setWorkflowState(workflows.getValue(), workflowState.getValue());
    }

    public int getWorkflowStateValue(CompanyPublishStatusWorkflows index) {
        return getWorkflowStateValue(index.getValue());
    }

    /***
     * return the Workflow state for the given workflow index
     * @param index
     * @return
     */
    public PublishStatusWorkflowState getWorkflowState(CompanyPublishStatusWorkflows index)  {

        return PublishStatusWorkflowState.workflowState(getWorkflowStateValue(index.getValue()));
    }

    /**
     * Set the workflow state to the given workflow
     * @param workflows
     * @param workflowState
     */
    public void setWorkflowState(EmployeePublishStatusWorkflows workflows, PublishStatusWorkflowState workflowState)  {

        //set the workflowstate value returned by workflowState.getValue() to the index returned by workflows.getValue()
        setWorkflowState(workflows.getValue(), workflowState.getValue());
    }

    public int getWorkflowStateValue(EmployeePublishStatusWorkflows index) {
        return getWorkflowStateValue(index.getValue());
    }

    /***
     * return the Workflow state for the given workflow index
     * @param index
     * @return
     */
    public PublishStatusWorkflowState getWorkflowState(EmployeePublishStatusWorkflows index)  {

        return PublishStatusWorkflowState.workflowState(getWorkflowStateValue(index.getValue()));
    }

    /**
     * Set the workflow state to the given workflow
     * @param workflows
     * @param workflowState
     */
    public void setWorkflowState(PayeePublishStatusWorkflows workflows, PublishStatusWorkflowState workflowState)  {

        //set the workflowstate value returned by workflowState.getValue() to the index returned by workflows.getValue()
        setWorkflowState(workflows.getValue(), workflowState.getValue());
    }

    public int getWorkflowStateValue(PayeePublishStatusWorkflows index) {
        return getWorkflowStateValue(index.getValue());
    }

    /***
     * return the Workflow state for the given workflow index
     * @param index
     * @return
     */
    public PublishStatusWorkflowState getWorkflowState(PayeePublishStatusWorkflows index)  {

        return PublishStatusWorkflowState.workflowState(getWorkflowStateValue(index.getValue()));
    }

    /**
     * return the OII flag
     * @return
     */
    public String getWorkFlowsFlagString() {
        return this.byteSet.getAllBytes();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WorkFlowsFlag {");
        for (CompanyPublishStatusWorkflows workflows : CompanyPublishStatusWorkflows.values()) {
            sb.append(workflows).append("=").append(getWorkflowState(workflows)).append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}