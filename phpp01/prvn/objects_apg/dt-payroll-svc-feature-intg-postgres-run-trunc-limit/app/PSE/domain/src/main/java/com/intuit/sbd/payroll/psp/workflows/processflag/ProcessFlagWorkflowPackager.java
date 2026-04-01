package com.intuit.sbd.payroll.psp.workflows.processflag;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.workflows.ByteSet;
import com.intuit.sbd.payroll.psp.workflows.processflag.employee.EmployeeProcessFlagWorkflows;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

public class ProcessFlagWorkflowPackager {
    public static final SpcfLogger logger = Application.getLogger(ProcessFlagWorkflowPackager.class);
    public static int DEFAULT_FLAG_SIZE = 16;

    protected ByteSet byteSet = null;

    public ProcessFlagWorkflowPackager() {
        this(DEFAULT_FLAG_SIZE);
    }

    public ProcessFlagWorkflowPackager(int flagSize) {

        this(null,flagSize);
    }

    public ProcessFlagWorkflowPackager(String workFlowsFlag, int flagSize) {
        byteSet = new ByteSet(flagSize);
        initializeByteSet(workFlowsFlag);
    }

    public ProcessFlagWorkflowPackager(String workFlowsFlag) {
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
    public void setWorkflowState(EmployeeProcessFlagWorkflows workflows, ProcessFlagWorkflowState workflowState)  {

        //set the workflowstate value returned by workflowState.getValue() to the index returned by workflows.getValue()
        setWorkflowState(workflows.getValue(), workflowState.getValue());
    }

    public int getWorkflowStateValue(EmployeeProcessFlagWorkflows index) {
        return getWorkflowStateValue(index.getValue());
    }

    /***
     * return the Workflow state for the given workflow index
     * @param index
     * @return
     */
    public ProcessFlagWorkflowState getWorkflowState(EmployeeProcessFlagWorkflows index)  {

        return ProcessFlagWorkflowState.workflowState(getWorkflowStateValue(index.getValue()));
    }

    /**
     * return the Process flag
     * @return
     */
    public String getWorkFlowsFlagString() {
        return this.byteSet.getAllBytes();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WorkFlowsFlag {");
        for (EmployeeProcessFlagWorkflows workflows : EmployeeProcessFlagWorkflows.values()) {
            sb.append(workflows).append("=").append(getWorkflowState(workflows)).append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
