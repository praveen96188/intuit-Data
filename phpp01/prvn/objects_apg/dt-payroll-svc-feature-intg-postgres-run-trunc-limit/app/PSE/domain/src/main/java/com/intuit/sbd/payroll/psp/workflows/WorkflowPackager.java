package com.intuit.sbd.payroll.psp.workflows;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * Binds the workflow with the workflow state
 *
 */
public class WorkflowPackager {

    public static final SpcfLogger logger = Application.getLogger(WorkflowPackager.class);
    public static int DEFAULT_OII_FLAG_SIZE = 16;

    private ByteSet byteSet = null;

    public WorkflowPackager() {
        this(DEFAULT_OII_FLAG_SIZE);
    }

    public WorkflowPackager(int flagSize) {

        this(null,flagSize);
    }

    public WorkflowPackager(String workFlowsFlag, int flagSize) {
        byteSet = new ByteSet(flagSize);
        initializeByteSet(workFlowsFlag);
    }

    public WorkflowPackager(String workFlowsFlag) {
        this(workFlowsFlag,DEFAULT_OII_FLAG_SIZE);
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

    /**
     * Set the workflow state to the given workflow
     * @param workflows
     * @param workflowState
     */
    public void setWorkflowState(Workflows workflows, WorkflowState workflowState)  {

        //set the workflowstate value returned by workflowState.getValue() to the index returned by workflows.getValue()
        byteSet.set(workflows.getValue(), workflowState.getValue());
    }

    public int getWorkflowStateValue(Workflows index) {
        return byteSet.get(index.getValue());
    }

    /***
     * return the Workflow state for the given workflow index
     * @param index
     * @return
     */
    public WorkflowState getWorkflowState(Workflows index)  {

        return WorkflowState.workflowState(byteSet.get(index.getValue()));
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
        for (Workflows workflows : Workflows.values()) {
            sb.append(workflows).append("=").append(getWorkflowState(workflows)).append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
